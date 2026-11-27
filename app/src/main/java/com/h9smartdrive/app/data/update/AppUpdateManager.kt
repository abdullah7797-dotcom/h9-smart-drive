package com.h9smartdrive.app.data.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.h9smartdrive.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

enum class UpdatePhase {
    IDLE,
    CHECKING,
    UP_TO_DATE,
    AVAILABLE,
    DOWNLOADING,
    READY_TO_INSTALL,
    INSTALL_PERMISSION_REQUIRED,
    FAILED,
}

data class UpdateInfo(
    val tag: String,
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val sha256: String,
    val sizeBytes: Long,
)

data class AppUpdateState(
    val phase: UpdatePhase = UpdatePhase.IDLE,
    val info: UpdateInfo? = null,
    val progressPercent: Int = 0,
    val downloadedApkPath: String? = null,
    val message: String? = null,
) {
    val showUpdateIcon: Boolean
        get() = phase in setOf(
            UpdatePhase.AVAILABLE,
            UpdatePhase.DOWNLOADING,
            UpdatePhase.READY_TO_INSTALL,
            UpdatePhase.INSTALL_PERMISSION_REQUIRED,
        ) || (phase == UpdatePhase.FAILED && info != null)
}

class AppUpdateManager(context: Context) {
    private val appContext = context.applicationContext
    private val _state = MutableStateFlow(AppUpdateState())
    val state: StateFlow<AppUpdateState> = _state.asStateFlow()

    suspend fun checkForUpdate(manual: Boolean = false) = withContext(Dispatchers.IO) {
        val previous = _state.value
        if (previous.phase == UpdatePhase.DOWNLOADING) return@withContext
        _state.value = previous.copy(phase = UpdatePhase.CHECKING, message = null)
        runCatching { fetchNewestRelease() }
            .onSuccess { update ->
                _state.value = if (update == null) {
                    AppUpdateState(
                        phase = UpdatePhase.UP_TO_DATE,
                        message = if (manual) "أنت تستخدم أحدث إصدار" else null,
                    )
                } else {
                    AppUpdateState(phase = UpdatePhase.AVAILABLE, info = update)
                }
            }
            .onFailure { error ->
                _state.value = AppUpdateState(
                    phase = UpdatePhase.FAILED,
                    info = previous.info,
                    message = "تعذر فحص التحديث: ${error.safeName()}",
                )
            }
    }

    suspend fun downloadUpdate() = withContext(Dispatchers.IO) {
        val info = _state.value.info ?: return@withContext
        _state.value = AppUpdateState(phase = UpdatePhase.DOWNLOADING, info = info)
        val directory = File(appContext.getExternalFilesDir(null) ?: appContext.filesDir, "updates")
        if (!directory.exists() && !directory.mkdirs()) {
            fail(info, "تعذر إنشاء مجلد التحديث")
            return@withContext
        }
        val destination = File(directory, "H9-Smart-Drive-${info.versionName}.apk")
        val temporary = File(directory, destination.name + ".part")
        runCatching {
            download(info, temporary)
            val actualHash = temporary.sha256()
            check(actualHash.equals(info.sha256, ignoreCase = true)) { "بصمة الملف لا تطابق إصدار GitHub" }
            if (destination.exists()) destination.delete()
            check(temporary.renameTo(destination)) { "تعذر تجهيز ملف التحديث" }
            validateApk(destination)
            destination
        }.onSuccess { apk ->
            _state.value = AppUpdateState(
                phase = UpdatePhase.READY_TO_INSTALL,
                info = info,
                progressPercent = 100,
                downloadedApkPath = apk.absolutePath,
            )
        }.onFailure { error ->
            temporary.delete()
            fail(info, "فشل تنزيل التحديث: ${error.message ?: error.safeName()}")
        }
    }

    fun installDownloadedUpdate() {
        val current = _state.value
        val info = current.info ?: return
        val file = current.downloadedApkPath?.let(::File)
        if (file == null || !file.isFile) {
            fail(info, "ملف التحديث غير موجود؛ أعد تنزيله")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !appContext.packageManager.canRequestPackageInstalls()) {
            _state.value = current.copy(
                phase = UpdatePhase.INSTALL_PERMISSION_REQUIRED,
                message = "اسمح لـ H9 Smart Drive بتثبيت هذا التحديث ثم اضغط تثبيت مرة أخرى",
            )
            val settingsIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${appContext.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            appContext.startActivity(settingsIntent)
            return
        }
        val uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, APK_MIME_TYPE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        runCatching { appContext.startActivity(intent) }
            .onFailure { fail(info, "تعذر فتح مثبت Android: ${it.safeName()}") }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun fetchNewestRelease(): UpdateInfo? {
        val connection = openConnection(RELEASES_API)
        return try {
            check(connection.responseCode in 200..299) { "GitHub HTTP ${connection.responseCode}" }
            val releases = connection.inputStream.bufferedReader().use { JSONArray(it.readText()) }
            (0 until releases.length()).asSequence()
                .map { releases.getJSONObject(it) }
                .filterNot { it.optBoolean("draft", false) }
                .mapNotNull { release ->
                    val tag = release.optString("tag_name")
                    val version = tag.removePrefix("v")
                    if (!SemanticVersion.isNewer(BuildConfig.VERSION_NAME, version)) return@mapNotNull null
                    val assets = release.optJSONArray("assets") ?: return@mapNotNull null
                    val asset = (0 until assets.length()).asSequence()
                        .map { assets.getJSONObject(it) }
                        .firstOrNull { it.optString("name") == APK_ASSET_NAME }
                        ?: return@mapNotNull null
                    val url = asset.optString("browser_download_url")
                    val digest = asset.optString("digest").removePrefix("sha256:")
                    if (!url.startsWith(RELEASE_DOWNLOAD_PREFIX) || !digest.matches(SHA_256_REGEX)) {
                        return@mapNotNull null
                    }
                    UpdateInfo(
                        tag = tag,
                        versionName = version,
                        releaseNotes = release.optString("body"),
                        downloadUrl = url,
                        sha256 = digest,
                        sizeBytes = asset.optLong("size"),
                    )
                }
                .maxWithOrNull { left, right -> SemanticVersion.compare(left.versionName, right.versionName) }
        } finally {
            connection.disconnect()
        }
    }

    private fun download(info: UpdateInfo, destination: File) {
        val connection = openConnection(info.downloadUrl)
        connection.instanceFollowRedirects = true
        try {
            check(connection.responseCode in 200..299) { "Download HTTP ${connection.responseCode}" }
            val total = connection.contentLengthLong.takeIf { it > 0 } ?: info.sizeBytes
            connection.inputStream.use { input ->
                destination.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var downloaded = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        downloaded += count
                        val progress = if (total > 0) ((downloaded * 100) / total).toInt().coerceIn(0, 99) else 0
                        _state.value = _state.value.copy(progressPercent = progress)
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun validateApk(file: File) {
        val packageManager = appContext.packageManager
        val archive = packageInfo(packageManager, file.absolutePath)
            ?: error("تعذر قراءة هوية APK")
        check(archive.packageName == appContext.packageName) { "حزمة APK لا تطابق التطبيق المثبت" }
        val installed = packageInfo(packageManager, appContext.packageName)
            ?: error("تعذر قراءة هوية التطبيق المثبت")
        check(archive.longVersionCodeCompat() > installed.longVersionCodeCompat()) { "الإصدار المنزّل ليس أحدث" }
        check(archive.signerDigests() == installed.signerDigests()) { "توقيع APK لا يطابق التطبيق المثبت" }
    }

    @Suppress("DEPRECATION")
    private fun packageInfo(packageManager: PackageManager, pathOrPackage: String): PackageInfo? {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }
        return if (pathOrPackage.endsWith(".apk")) {
            packageManager.getPackageArchiveInfo(pathOrPackage, flags)
        } else {
            runCatching { packageManager.getPackageInfo(pathOrPackage, flags) }.getOrNull()
        }
    }

    @Suppress("DEPRECATION")
    private fun PackageInfo.signerDigests(): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            signingInfo?.apkContentsSigners.orEmpty()
        } else {
            signatures.orEmpty()
        }
        return signatures.map { signature ->
            MessageDigest.getInstance("SHA-256").digest(signature.toByteArray()).toHex()
        }.toSet()
    }

    @Suppress("DEPRECATION")
    private fun PackageInfo.longVersionCodeCompat(): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode else versionCode.toLong()

    private fun openConnection(url: String): HttpsURLConnection =
        (URL(url).openConnection() as HttpsURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "H9-Smart-Drive/${BuildConfig.VERSION_NAME}")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
        }

    private fun File.sha256(): String = FileInputStream(this).use { input ->
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
        digest.digest().toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun fail(info: UpdateInfo?, message: String) {
        _state.value = AppUpdateState(phase = UpdatePhase.FAILED, info = info, message = message)
    }

    private fun Throwable.safeName(): String = javaClass.simpleName.ifBlank { "UnknownError" }

    private companion object {
        const val RELEASES_API = "https://api.github.com/repos/abdullah7797-dotcom/h9-smart-drive/releases?per_page=20"
        const val RELEASE_DOWNLOAD_PREFIX = "https://github.com/abdullah7797-dotcom/h9-smart-drive/releases/download/"
        const val APK_ASSET_NAME = "H9-Smart-Drive.apk"
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        const val NETWORK_TIMEOUT_MS = 20_000
        val SHA_256_REGEX = Regex("^[a-fA-F0-9]{64}$")
    }
}

object SemanticVersion {
    fun isNewer(current: String, candidate: String): Boolean = compare(candidate, current) > 0

    fun compare(left: String, right: String): Int {
        val a = parse(left)
        val b = parse(right)
        for (index in 0..2) {
            val compared = a.numbers[index].compareTo(b.numbers[index])
            if (compared != 0) return compared
        }
        if (a.preRelease.isEmpty() && b.preRelease.isNotEmpty()) return 1
        if (a.preRelease.isNotEmpty() && b.preRelease.isEmpty()) return -1
        val length = maxOf(a.preRelease.size, b.preRelease.size)
        for (index in 0 until length) {
            val leftPart = a.preRelease.getOrNull(index) ?: return -1
            val rightPart = b.preRelease.getOrNull(index) ?: return 1
            val leftNumber = leftPart.toIntOrNull()
            val rightNumber = rightPart.toIntOrNull()
            val compared = when {
                leftNumber != null && rightNumber != null -> leftNumber.compareTo(rightNumber)
                leftNumber != null -> -1
                rightNumber != null -> 1
                else -> leftPart.compareTo(rightPart, ignoreCase = true)
            }
            if (compared != 0) return compared
        }
        return 0
    }

    private fun parse(raw: String): ParsedVersion {
        val clean = raw.removePrefix("v").removeSuffix("-debug").substringBefore('+')
        val main = clean.substringBefore('-').split('.')
        val numbers = List(3) { index -> main.getOrNull(index)?.toIntOrNull() ?: 0 }
        val preRelease = clean.substringAfter('-', "").split('.').filter(String::isNotBlank)
        return ParsedVersion(numbers, preRelease)
    }

    private data class ParsedVersion(val numbers: List<Int>, val preRelease: List<String>)
}
