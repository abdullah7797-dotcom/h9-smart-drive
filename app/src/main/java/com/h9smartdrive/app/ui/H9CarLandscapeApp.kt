package com.h9smartdrive.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.TireRepair
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.h9smartdrive.app.data.vehicle.DataSource
import com.h9smartdrive.app.data.update.AppUpdateState
import com.h9smartdrive.app.data.update.UpdatePhase
import com.h9smartdrive.app.data.vehicle.Reading
import com.h9smartdrive.app.data.vehicle.TireState
import com.h9smartdrive.app.data.vehicle.VehicleSnapshot
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val LightScheme = lightColorScheme(
    primary = Color(0xFF176783),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA4DBF6),
    onPrimaryContainer = Color(0xFF003548),
    secondary = Color(0xFF4D5F92),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCE2FF),
    onSecondaryContainer = Color(0xFF18264E),
    tertiary = Color(0xFF56648E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCE2FF),
    onTertiaryContainer = Color(0xFF17264D),
    background = Color(0xFFF6F8FC),
    onBackground = Color(0xFF17202A),
    surface = Color(0xFFF6F8FC),
    onSurface = Color(0xFF17202A),
    surfaceVariant = Color(0xFFE2E8ED),
    onSurfaceVariant = Color(0xFF414A51),
    outline = Color(0xFF717A82),
    error = Color(0xFFBA1A1A),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF8FCFED),
    onPrimary = Color(0xFF003548),
    primaryContainer = Color(0xFF0A4E67),
    onPrimaryContainer = Color(0xFFC2E8FA),
    secondary = Color(0xFFBAC6FA),
    onSecondary = Color(0xFF22315B),
    secondaryContainer = Color(0xFF394873),
    onSecondaryContainer = Color(0xFFDCE2FF),
    tertiary = Color(0xFFBEC7F5),
    onTertiary = Color(0xFF28335D),
    tertiaryContainer = Color(0xFF3E4974),
    onTertiaryContainer = Color(0xFFDCE2FF),
    background = Color(0xFF101417),
    onBackground = Color(0xFFE1E7EC),
    surface = Color(0xFF101417),
    onSurface = Color(0xFFE1E7EC),
    surfaceVariant = Color(0xFF41484D),
    onSurfaceVariant = Color(0xFFC1C8CE),
    outline = Color(0xFF8B9298),
    error = Color(0xFFFFB4AB),
)

private enum class CarScreen(
    val ar: String,
    val en: String,
    val icon: ImageVector,
) {
    HOME("الرئيسية", "Home", Icons.Rounded.Home),
    MAPS("الخرائط", "Maps", Icons.Rounded.Map),
    RADIO("الراديو", "Radio", Icons.Rounded.Radio),
    MEDIA("الوسائط", "Media", Icons.Rounded.LibraryMusic),
    VEHICLE("المركبة", "Vehicle", Icons.Rounded.DirectionsCar),
    OFFROAD("الطرق الوعرة", "Off-road", Icons.Rounded.Explore),
    APPS("التطبيقات", "Apps", Icons.Rounded.Apps),
    SETTINGS("الإعدادات", "Settings", Icons.Rounded.Settings),
}

@Composable
fun H9SmartDriveApp(viewModel: H9ViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val arabic = state.settings.language == "ar"
    var selected by remember { mutableStateOf(CarScreen.HOME) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (state.settings.darkMode) DarkScheme else LightScheme,
        typography = Typography(),
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides if (arabic) LayoutDirection.Rtl else LayoutDirection.Ltr,
        ) {
            Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
                BoxWithConstraints(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .systemBarsPadding()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    val shellWidth = maxWidth.coerceAtMost(1600.dp)
                    val shellHeight = maxHeight.coerceAtMost(900.dp)
                    Row(
                        Modifier
                            .width(shellWidth)
                            .height(shellHeight)
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CarNavigationRail(selected, arabic) { selected = it }
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            CarHeader(
                                screen = selected,
                                vehicle = state.vehicle,
                                update = state.update,
                                arabic = arabic,
                                onUpdateClick = { showUpdateDialog = true },
                            )
                            Box(Modifier.fillMaxWidth().weight(1f)) {
                                when (selected) {
                                    CarScreen.HOME -> HomeLandscape(state.vehicle, arabic)
                                    CarScreen.OFFROAD -> OffRoadLandscape(state.vehicle, arabic)
                                    CarScreen.VEHICLE -> VehicleLandscape(state.vehicle, arabic)
                                    CarScreen.MAPS -> LauncherLandscape(CarScreen.MAPS, arabic)
                                    CarScreen.RADIO -> LauncherLandscape(CarScreen.RADIO, arabic)
                                    CarScreen.MEDIA -> LauncherLandscape(CarScreen.MEDIA, arabic)
                                    CarScreen.APPS -> AppsLandscape(arabic)
                                    CarScreen.SETTINGS -> SettingsLandscape(state, arabic, viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showUpdateDialog) {
            UpdateDialog(
                update = state.update,
                arabic = arabic,
                onDismiss = { showUpdateDialog = false },
                onDownload = viewModel::downloadUpdate,
                onInstall = viewModel::installUpdate,
                onRetry = viewModel::checkForUpdate,
            )
        }
    }
}

@Composable
private fun CarNavigationRail(current: CarScreen, arabic: Boolean, onSelect: (CarScreen) -> Unit) {
    Surface(
        modifier = Modifier.width(82.dp).fillMaxHeight(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("H9", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }
            CarScreen.entries.forEach { item ->
                val active = item == current
                Surface(
                    modifier = Modifier
                        .size(54.dp)
                        .clickable { onSelect(item) },
                    shape = RoundedCornerShape(18.dp),
                    color = if (active) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (active) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            item.icon,
                            contentDescription = if (arabic) item.ar else item.en,
                            modifier = Modifier.size(25.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarHeader(
    screen: CarScreen,
    vehicle: VehicleSnapshot,
    update: AppUpdateState,
    arabic: Boolean,
    onUpdateClick: () -> Unit,
) {
    val time = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    Row(
        Modifier.fillMaxWidth().height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = if (screen == CarScreen.HOME) {
                    if (arabic) "مرحبًا بك في H9" else "Welcome to H9"
                } else if (arabic) screen.ar else screen.en,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.semantics { heading() },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                if (arabic) "قيادة آمنة · $time" else "Drive safely · $time",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            HeaderChip(Icons.Rounded.Cloud, temperature(vehicle, arabic))
            HeaderChip(Icons.Rounded.Wifi, connectionLabel(vehicle, arabic))
            if (update.showUpdateIcon) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ) {
                    IconButton(onClick = onUpdateClick) {
                        Icon(
                            Icons.Rounded.SystemUpdate,
                            if (arabic) "تحديث التطبيق متاح" else "App update available",
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.Notifications, if (arabic) "التنبيهات" else "Notifications")
                }
            }
        }
    }
}

@Composable
private fun HeaderChip(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            Modifier.heightIn(min = 48.dp).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, null, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun UpdateDialog(
    update: AppUpdateState,
    arabic: Boolean,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onRetry: () -> Unit,
) {
    val info = update.info
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.SystemUpdate, null) },
        title = {
            Text(
                if (arabic) "تحديث H9 Smart Drive" else "Update H9 Smart Drive",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (info != null) {
                    Text(
                        if (arabic) "الإصدار ${info.versionName} · ${fileSize(info.sizeBytes, arabic)}"
                        else "Version ${info.versionName} · ${fileSize(info.sizeBytes, arabic)}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (info.releaseNotes.isNotBlank()) {
                        Text(
                            info.releaseNotes.take(900),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 9,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                when (update.phase) {
                    UpdatePhase.DOWNLOADING -> {
                        LinearProgressIndicator(
                            progress = { update.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            if (arabic) "جارٍ التنزيل والتحقق… ${update.progressPercent}%"
                            else "Downloading and verifying… ${update.progressPercent}%",
                        )
                    }
                    UpdatePhase.READY_TO_INSTALL -> Text(
                        if (arabic) "اكتمل التنزيل وتطابقت بصمة الملف والتوقيع."
                        else "Download, checksum, and signature verification passed.",
                    )
                    UpdatePhase.INSTALL_PERMISSION_REQUIRED -> Text(
                        update.message ?: if (arabic) "اسمح بالتثبيت ثم حاول مرة أخرى" else "Allow installation, then try again",
                        color = MaterialTheme.colorScheme.error,
                    )
                    UpdatePhase.FAILED -> Text(
                        update.message ?: if (arabic) "تعذر إكمال التحديث" else "The update could not be completed",
                        color = MaterialTheme.colorScheme.error,
                    )
                    UpdatePhase.CHECKING -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(Modifier.size(28.dp))
                        Text(if (arabic) "جارٍ فحص GitHub…" else "Checking GitHub…")
                    }
                    else -> Unit
                }
            }
        },
        confirmButton = {
            when (update.phase) {
                UpdatePhase.AVAILABLE -> Button(onClick = onDownload) {
                    Icon(Icons.Rounded.Download, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (arabic) "تنزيل التحديث" else "Download update")
                }
                UpdatePhase.READY_TO_INSTALL,
                UpdatePhase.INSTALL_PERMISSION_REQUIRED -> Button(onClick = onInstall) {
                    Icon(Icons.Rounded.SystemUpdate, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (arabic) "تثبيت" else "Install")
                }
                UpdatePhase.FAILED -> Button(onClick = onRetry) {
                    Text(if (arabic) "إعادة المحاولة" else "Try again")
                }
                else -> Unit
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (arabic) "لاحقاً" else "Later")
            }
        },
    )
}

@Composable
private fun HomeLandscape(vehicle: VehicleSnapshot, arabic: Boolean) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SpeedHero(vehicle, arabic, Modifier.weight(1.34f))
        VehicleStatusPane(vehicle, arabic, Modifier.weight(.92f))
        JourneyPane(vehicle, arabic, Modifier.weight(.74f))
    }
}

@Composable
private fun SpeedHero(vehicle: VehicleSnapshot, arabic: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (arabic) "السرعة" else "Speed", style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                Text(sourceName(vehicle.speedKph.source, arabic), style = MaterialTheme.typography.labelLarge)
            }
            Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.Bottom) {
                Text(
                    number(vehicle.speedKph.value, arabic),
                    fontSize = 112.sp,
                    lineHeight = 112.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(if (arabic) "كم/س" else "km/h", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 14.dp))
            }
            LinearProgressIndicator(
                progress = { ((vehicle.speedKph.value ?: 0.0) / 200.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                HeroStat(vehicle.gear.value ?: "—", if (arabic) "الناقل" else "Gear")
                HeroStat(number(vehicle.rpm.value, arabic), if (arabic) "دورة/د" else "RPM")
                HeroStat(vehicle.driveMode.value ?: "—", if (arabic) "الوضع" else "Mode")
            }
        }
    }
}

@Composable
private fun VehicleStatusPane(vehicle: VehicleSnapshot, arabic: Boolean, modifier: Modifier = Modifier) {
    InfoPane(modifier, if (arabic) "حالة المركبة" else "Vehicle status") {
        MetricRow(Icons.Rounded.LocalGasStation, if (arabic) "الوقود" else "Fuel", valueWithUnit(vehicle.fuelPercent, "%", arabic), valueWithUnit(vehicle.rangeKm, if (arabic) " كم" else " km", arabic))
        MetricRow(Icons.Rounded.Thermostat, if (arabic) "المحرك" else "Engine", valueWithUnit(vehicle.engineTempC, "°", arabic), if (arabic) "طبيعي" else "Normal")
        MetricRow(Icons.Rounded.BatteryChargingFull, if (arabic) "البطارية" else "Battery", valueWithUnit(vehicle.batteryVoltage, "V", arabic, 1), if (arabic) "مستقرة" else "Stable")
        MetricRow(Icons.Rounded.TireRepair, if (arabic) "الإطارات" else "Tires", averageTire(vehicle.tires, arabic), if (arabic) "متوازنة" else "Balanced")
        Spacer(Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 54.dp).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(if (arabic) "المركبة جاهزة" else "Vehicle ready", fontWeight = FontWeight.SemiBold)
                Icon(Icons.Rounded.CheckCircle, null)
            }
        }
    }
}

@Composable
private fun JourneyPane(vehicle: VehicleSnapshot, arabic: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    InfoPane(modifier, if (arabic) "الرحلة" else "Journey") {
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ) {
            Column(
                Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                Icon(Icons.Rounded.Route, null, modifier = Modifier.size(48.dp))
                Text(
                    valueWithUnit(vehicle.currentTripKm, if (arabic) " كم" else " km", arabic, 1),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    if (arabic) {
                        "متوسط ${valueWithUnit(vehicle.averageFuelConsumptionL100Km, " ل/100كم", arabic, 1)}"
                    } else {
                        "Average ${valueWithUnit(vehicle.averageFuelConsumptionL100Km, " L/100km", arabic, 1)}"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        PrimaryAction(Icons.Rounded.Map, if (arabic) "فتح الخريطة" else "Open map") { launchMaps(context, arabic) }
    }
}

@Composable
private fun OffRoadLandscape(vehicle: VehicleSnapshot, arabic: Boolean) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        CompassHero(vehicle, arabic, Modifier.weight(1.24f))
        ReadinessPane(vehicle, arabic, Modifier.weight(.88f))
        WaypointsPane(arabic, Modifier.weight(.78f))
    }
}

@Composable
private fun CompassHero(vehicle: VehicleSnapshot, arabic: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                if (arabic) "البوصلة" else "Compass",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().semantics { heading() },
            )
            Surface(
                modifier = Modifier.size(220.dp),
                shape = RoundedCornerShape(48.dp),
                color = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Rounded.Navigation, null, modifier = Modifier.size(58.dp))
                    Text("${number(vehicle.headingDegrees.value, arabic)}°", fontSize = 42.sp, fontWeight = FontWeight.Bold)
                    Text(directionName(vehicle.headingDegrees.value, arabic), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                HeroStat(valueWithUnit(vehicle.altitudeMeters, if (arabic) " م" else " m", arabic), if (arabic) "الارتفاع" else "Altitude")
                HeroStat(valueWithUnit(vehicle.speedKph, if (arabic) " كم/س" else " km/h", arabic), if (arabic) "السرعة" else "Speed")
                HeroStat(vehicle.fourWheelDrive.value ?: "—", if (arabic) "الدفع" else "Drive")
            }
        }
    }
}

@Composable
private fun ReadinessPane(vehicle: VehicleSnapshot, arabic: Boolean, modifier: Modifier = Modifier) {
    InfoPane(modifier, if (arabic) "جاهزية المركبة" else "Vehicle readiness") {
        MetricRow(Icons.Rounded.TireRepair, if (arabic) "الإطارات" else "Tires", averageTire(vehicle.tires, arabic), if (arabic) "مناسبة" else "Ready")
        MetricRow(Icons.Rounded.Speed, if (arabic) "الميلان" else "Tilt", "—° / —°", if (arabic) "بانتظار المستشعر" else "Sensor pending")
        MetricRow(Icons.Rounded.LocalGasStation, if (arabic) "الوقود" else "Fuel", valueWithUnit(vehicle.fuelPercent, "%", arabic), valueWithUnit(vehicle.rangeKm, if (arabic) " كم" else " km", arabic))
        MetricRow(Icons.Rounded.DirectionsCar, if (arabic) "الدفع" else "Drive", vehicle.fourWheelDrive.value ?: "—", if (arabic) "مفعّل" else "Active")
        Spacer(Modifier.weight(1f))
        Text(
            if (arabic) "معلومات مختصرة لتقليل التشتيت أثناء الحركة" else "Short, glanceable information reduces distraction",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WaypointsPane(arabic: Boolean, modifier: Modifier = Modifier) {
    InfoPane(modifier, if (arabic) "نقاط المسار" else "Waypoints") {
        Waypoint(Icons.Rounded.Flag, if (arabic) "البداية" else "Start", if (arabic) "٠٫٠ كم" else "0.0 km")
        Waypoint(Icons.Rounded.LocationOn, if (arabic) "المخيم" else "Camp", if (arabic) "١٢٫٤ كم" else "12.4 km")
        Waypoint(Icons.Rounded.WaterDrop, if (arabic) "الماء" else "Water", if (arabic) "٢١٫٨ كم" else "21.8 km")
        Spacer(Modifier.weight(1f))
        PrimaryAction(Icons.Rounded.Route, if (arabic) "بدء المسار" else "Start trail") {}
    }
}

@Composable
private fun VehicleLandscape(vehicle: VehicleSnapshot, arabic: Boolean) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoPane(Modifier.weight(1f), if (arabic) "الإطارات" else "Tires", compact = true) {
            TireMetric(if (arabic) "أمامي يسار" else "Front left", vehicle.tires.frontLeftPsi, vehicle.tires.frontLeftTempC, arabic)
            TireMetric(if (arabic) "أمامي يمين" else "Front right", vehicle.tires.frontRightPsi, vehicle.tires.frontRightTempC, arabic)
            TireMetric(if (arabic) "خلفي يسار" else "Rear left", vehicle.tires.rearLeftPsi, vehicle.tires.rearLeftTempC, arabic)
            TireMetric(if (arabic) "خلفي يمين" else "Rear right", vehicle.tires.rearRightPsi, vehicle.tires.rearRightTempC, arabic)
            MetricRow(Icons.Rounded.Speed, if (arabic) "سرعات العجلات" else "Wheel speeds", wheelSpeedSummary(vehicle, arabic), sourceName(vehicle.wheelFrontLeftKph.source, arabic))
        }
        InfoPane(Modifier.weight(1f), if (arabic) "المحرك والطاقة" else "Engine & power", compact = true) {
            MetricRow(Icons.Rounded.Speed, if (arabic) "دورات المحرك" else "Engine RPM", number(vehicle.rpm.value, arabic), sourceName(vehicle.rpm.source, arabic))
            MetricRow(Icons.Rounded.Thermostat, if (arabic) "حرارة المحرك" else "Engine temp", valueWithUnit(vehicle.engineTempC, "°", arabic), sourceName(vehicle.engineTempC.source, arabic))
            MetricRow(Icons.Rounded.Thermostat, if (arabic) "حرارة ناقل الحركة" else "Transmission temp", valueWithUnit(vehicle.transmissionTempC, "°", arabic), sourceName(vehicle.transmissionTempC.source, arabic))
            MetricRow(Icons.Rounded.BatteryChargingFull, if (arabic) "جهد البطارية" else "Battery", valueWithUnit(vehicle.batteryVoltage, "V", arabic, 1), sourceName(vehicle.batteryVoltage.source, arabic))
            MetricRow(Icons.Rounded.LocalGasStation, if (arabic) "الوقود" else "Fuel", valueWithUnit(vehicle.fuelPercent, "%", arabic), valueWithUnit(vehicle.rangeKm, if (arabic) " كم" else " km", arabic))
            MetricRow(Icons.Rounded.LocalGasStation, if (arabic) "الاستهلاك" else "Consumption", valueWithUnit(vehicle.instantFuelConsumptionL100Km, if (arabic) " ل/100كم" else " L/100km", arabic, 1), valueWithUnit(vehicle.averageFuelConsumptionL100Km, if (arabic) " متوسط" else " average", arabic, 1))
            MetricRow(Icons.Rounded.Speed, if (arabic) "عزم المحرك" else "Engine torque", valueWithUnit(vehicle.engineTorqueNm, " Nm", arabic), sourceName(vehicle.engineTorqueNm.source, arabic))
        }
        InfoPane(Modifier.weight(.86f), if (arabic) "نظام القيادة" else "Drivetrain", compact = true) {
            MetricRow(Icons.Rounded.DirectionsCar, if (arabic) "ناقل الحركة" else "Gear", vehicle.gear.value ?: "—", sourceName(vehicle.gear.source, arabic))
            MetricRow(Icons.Rounded.Explore, if (arabic) "وضع القيادة" else "Drive mode", vehicle.driveMode.value ?: "—", sourceName(vehicle.driveMode.source, arabic))
            MetricRow(Icons.Rounded.DirectionsCar, if (arabic) "الدفع الرباعي" else "Four-wheel drive", vehicle.fourWheelDrive.value ?: "—", differentialSummary(vehicle, arabic))
            MetricRow(Icons.Rounded.Route, if (arabic) "مسافة الرحلة" else "Trip distance", valueWithUnit(vehicle.currentTripKm, if (arabic) " كم" else " km", arabic, 1), valueWithUnit(vehicle.accumulatedTripKm, if (arabic) " كم تراكمي" else " km accumulated", arabic, 1))
            MetricRow(Icons.Rounded.Speed, if (arabic) "متوسط السرعة" else "Average speed", valueWithUnit(vehicle.averageSpeedKph, if (arabic) " كم/س" else " km/h", arabic, 1), sourceName(vehicle.averageSpeedKph.source, arabic))
            MetricRow(Icons.Rounded.Navigation, if (arabic) "زاوية المقود" else "Steering angle", valueWithUnit(vehicle.steeringAngleDeg, "°", arabic, 1), sourceName(vehicle.steeringAngleDeg.source, arabic))
            MetricRow(Icons.Rounded.DirectionsCar, if (arabic) "الأبواب" else "Doors", vehicle.doorStatus.value ?: "—", sourceName(vehicle.doorStatus.source, arabic))
        }
    }
}

@Composable
private fun LauncherLandscape(screen: CarScreen, arabic: Boolean) {
    val context = LocalContext.current
    Card(
        Modifier.fillMaxSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            Modifier.fillMaxSize().padding(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(48.dp),
        ) {
            Surface(
                modifier = Modifier.size(230.dp),
                shape = RoundedCornerShape(64.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Box(contentAlignment = Alignment.Center) { Icon(screen.icon, null, modifier = Modifier.size(112.dp)) } }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(if (arabic) screen.ar else screen.en, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold)
                Text(
                    if (arabic) "افتح تطبيق السيارة الأصلي المتوافق للتحكم الآمن دون تجاوز صلاحيات النظام."
                    else "Open the compatible native car app for safe control without bypassing system permissions.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.widthIn(max = 560.dp),
                )
                PrimaryAction(Icons.AutoMirrored.Rounded.OpenInNew, if (arabic) "فتح التطبيق" else "Open app") {
                    if (screen == CarScreen.MAPS) launchMaps(context, arabic)
                    else launchMedia(context, arabic)
                }
            }
        }
    }
}

@Composable
private fun AppsLandscape(arabic: Boolean) {
    val context = LocalContext.current
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        AppPane(Icons.Rounded.Map, if (arabic) "الخرائط" else "Maps", Modifier.weight(1f)) { launchMaps(context, arabic) }
        AppPane(Icons.Rounded.Radio, if (arabic) "الراديو" else "Radio", Modifier.weight(1f)) { launchMedia(context, arabic) }
        AppPane(Icons.Rounded.LibraryMusic, if (arabic) "الوسائط" else "Media", Modifier.weight(1f)) { launchMedia(context, arabic) }
        AppPane(Icons.Rounded.Apps, if (arabic) "تطبيقات السيارة" else "Car apps", Modifier.weight(1f)) { unavailable(context, arabic) }
    }
}

@Composable
private fun AppPane(icon: ImageVector, title: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(38.dp)) } }
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Icon(Icons.AutoMirrored.Rounded.OpenInNew, null)
        }
    }
}

@Composable
private fun SettingsLandscape(state: H9UiState, arabic: Boolean, viewModel: H9ViewModel) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 900.dp) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DiagnosticPane(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    title = if (arabic) "اتصال H9 2025" else "H9 2025 connection",
                    scrollable = true,
                ) { ConnectionSettingsContent(state, arabic, viewModel) }
                DiagnosticPane(
                    modifier = Modifier.weight(1.15f).fillMaxHeight(),
                    title = if (arabic) "تقرير توافق النظام" else "System compatibility report",
                    scrollable = true,
                ) { SystemReportContent(state, arabic) }
            }
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DiagnosticPane(
                    modifier = Modifier.fillMaxWidth(),
                    title = if (arabic) "اتصال H9 2025" else "H9 2025 connection",
                    scrollable = false,
                ) { ConnectionSettingsContent(state, arabic, viewModel) }
                DiagnosticPane(
                    modifier = Modifier.fillMaxWidth(),
                    title = if (arabic) "تقرير توافق النظام" else "System compatibility report",
                    scrollable = false,
                ) { SystemReportContent(state, arabic) }
            }
        }
    }
}

@Composable
private fun ConnectionSettingsContent(state: H9UiState, arabic: Boolean, viewModel: H9ViewModel) {
    val connected = state.vehicle.providerStatus.entries.firstOrNull { it.value.connected }
    val statusTitle = when {
        state.vehicle.demoMode -> if (arabic) "الوضع التجريبي يعمل" else "Demo mode active"
        connected != null -> if (arabic) "متصل عبر ${connected.key.labelAr}" else "Connected through ${connected.key.labelEn}"
        else -> if (arabic) "بانتظار سماح نظام السيارة" else "Waiting for vehicle access"
    }
    val statusDetail = connected?.value?.detail ?: state.vehicle.providerStatus.values
        .firstOrNull { it.available }?.detail ?: if (arabic) "شغّل التقرير لمعرفة منصة الشاشة" else "Use the report to identify the head unit"
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (connected != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
        contentColor = if (connected != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground,
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(statusTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(statusDetail, style = MaterialTheme.typography.bodyMedium)
        }
    }

    val context = LocalContext.current
    val report = state.diagnostics?.asJson(state.vehicle.providerStatus, state.vehicle)
    Button(
        onClick = {
            if (report != null) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("H9 Smart Drive compatibility", report))
                Toast.makeText(context, if (arabic) "تم نسخ تقرير التوافق" else "Compatibility report copied", Toast.LENGTH_SHORT).show()
            }
        },
        enabled = report != null,
        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
        shape = RoundedCornerShape(22.dp),
    ) {
        Icon(Icons.Rounded.ContentCopy, null)
        Spacer(Modifier.width(8.dp))
        Text(if (arabic) "نسخ تقرير التوافق" else "Copy compatibility report", fontWeight = FontWeight.SemiBold)
    }
    FilledTonalButton(
        onClick = {
            if (report != null) {
                val intent = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_SUBJECT, "H9 Smart Drive 2025 compatibility")
                    .putExtra(Intent.EXTRA_TEXT, report)
                runCatching { context.startActivity(Intent.createChooser(intent, null)) }
                    .onFailure { Toast.makeText(context, if (arabic) "لا يوجد تطبيق مشاركة متاح" else "No sharing app is available", Toast.LENGTH_SHORT).show() }
            }
        },
        enabled = report != null,
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Icon(Icons.Rounded.Share, null)
        Spacer(Modifier.width(8.dp))
        Text(if (arabic) "مشاركة التقرير" else "Share report")
    }

    ToggleSetting(Icons.Rounded.Sensors, if (arabic) "الوضع التجريبي" else "Demo mode", if (arabic) "أوقفه لاختبار أرقام السيارة" else "Turn off to test vehicle data", state.settings.demoMode, viewModel::setDemoMode)
    DataSource.entries.filter { it != DataSource.UNAVAILABLE && it != DataSource.DEMO }.forEach { source ->
        val status = state.vehicle.providerStatus[source]
        MetricRow(
            Icons.Rounded.Sensors,
            if (arabic) source.labelAr else source.labelEn,
            when {
                status?.connected == true -> if (arabic) "متصل" else "Connected"
                status?.available == true -> if (arabic) "موجود" else "Detected"
                else -> if (arabic) "غير موجود" else "Not found"
            },
            status?.detail ?: "—",
        )
    }
    Text(if (arabic) "المظهر واللغة" else "Appearance & language", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    ToggleSetting(Icons.Rounded.Settings, if (arabic) "الوضع الليلي" else "Dark mode", if (arabic) "ألوان أقل سطوعًا" else "Lower-glare palette", state.settings.darkMode, viewModel::setDarkMode)
    ToggleSetting(Icons.Rounded.Language, if (arabic) "اللغة العربية" else "Arabic language", if (arabic) "اتجاه واجهة RTL" else "RTL interface", arabic) { viewModel.setLanguage(if (it) "ar" else "en") }
    FilledTonalButton(
        onClick = viewModel::checkForUpdate,
        enabled = state.update.phase != UpdatePhase.CHECKING && state.update.phase != UpdatePhase.DOWNLOADING,
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        if (state.update.phase == UpdatePhase.CHECKING) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Rounded.SystemUpdate, null)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            when {
                state.update.showUpdateIcon -> if (arabic) "يوجد تحديث متاح" else "Update available"
                state.update.phase == UpdatePhase.UP_TO_DATE -> if (arabic) "التطبيق محدّث" else "App is up to date"
                else -> if (arabic) "فحص تحديث التطبيق" else "Check for app update"
            },
        )
    }
}

@Composable
private fun SystemReportContent(state: H9UiState, arabic: Boolean) {
    val d = state.diagnostics
    MetricRow(Icons.Rounded.Apps, "Android", d?.androidVersion ?: "—", "SDK ${d?.sdk ?: "—"} · ${d?.buildDisplay ?: "—"}")
    MetricRow(Icons.Rounded.DirectionsCar, if (arabic) "وحدة الشاشة" else "Head unit", d?.model ?: "—", "${d?.manufacturer ?: "—"} · ${d?.product ?: "—"}")
    MetricRow(Icons.Rounded.Map, if (arabic) "الشاشة" else "Display", d?.screen ?: "—", d?.device ?: "—")
    MetricRow(Icons.Rounded.DirectionsCar, "Android Automotive", yesNo(d?.carApiAvailable == true, arabic), if (arabic) "ميزة Automotive: ${yesNo(d?.automotive == true, arabic)}" else "Automotive feature: ${yesNo(d?.automotive == true, arabic)}")
    MetricRow(Icons.Rounded.LocationOn, "GPS", yesNo(d?.gps == true, arabic), if (arabic) "إذن الموقع: ${yesNo(d?.locationPermission == true, arabic)}" else "Location permission: ${yesNo(d?.locationPermission == true, arabic)}")
    MetricRow(
        Icons.Rounded.CheckCircle,
        if (arabic) "صلاحيات السيارة" else "Vehicle permissions",
        "${d?.grantedVehiclePermissionCount ?: 0}/${d?.vehiclePermissions?.size ?: 0}",
        if (arabic) "تختلف حسب برمجة الوكيل" else "Depends on the OEM firmware",
    )
    Text(if (arabic) "خدمات GWM المكتشفة" else "Detected GWM services", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    val packages = d?.knownPackages.orEmpty()
    packages.forEach { pkg ->
        MetricRow(
            Icons.Rounded.Apps,
            pkg.roleAr,
            if (pkg.installed) if (arabic) "موجود" else "Found" else if (arabic) "غير موجود" else "Missing",
            if (pkg.installed) "${pkg.packageName} · ${pkg.versionName ?: "?"}" else pkg.packageName,
        )
        if (pkg.installed && pkg.services.isNotEmpty()) {
            Text(
                pkg.services.take(3).joinToString("\n") { service ->
                    "• ${service.name.substringAfterLast('.')}" + (service.permission?.let { " · ${it.substringAfterLast('.')}" } ?: "")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Text(
        if (arabic) "التقرير لا يتضمن VIN أو الإحداثيات أو الحسابات الشخصية. انسخه وأرسله لي بعد إيقاف الوضع التجريبي والسيارة متوقفة على P." else "The report excludes VIN, coordinates, and account data. Copy it while parked with demo mode off.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun DiagnosticPane(
    modifier: Modifier,
    title: String,
    scrollable: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val base = if (scrollable) Modifier.fillMaxSize().verticalScroll(rememberScrollState()) else Modifier.fillMaxWidth()
        Column(
            base.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
            content()
        }
    }
}

@Composable
private fun InfoPane(
    modifier: Modifier,
    title: String,
    compact: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .then(if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(if (compact) 16.dp else 22.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 13.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
            content()
        }
    }
}

@Composable
private fun MetricRow(icon: ImageVector, label: String, value: String, note: String) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(46.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) { Box(contentAlignment = Alignment.Center) { Icon(icon, null, modifier = Modifier.size(23.dp)) } }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun HeroStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Waypoint(icon: ImageVector, label: String, distance: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.background) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 58.dp).padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null)
            Spacer(Modifier.width(10.dp))
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text(distance, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PrimaryAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
        shape = RoundedCornerShape(22.dp),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TireMetric(label: String, pressure: Reading<Double>, temperature: Reading<Double>, arabic: Boolean) {
    MetricRow(
        Icons.Rounded.TireRepair,
        label,
        valueWithUnit(pressure, " PSI", arabic, 1),
        valueWithUnit(temperature, "°C", arabic, 0),
    )
}

@Composable
private fun LargeValue(value: String, label: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ToggleSetting(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.background) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 74.dp)
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}

private fun sourceName(source: DataSource, arabic: Boolean): String = if (arabic) source.labelAr else source.labelEn

private fun connectionLabel(vehicle: VehicleSnapshot, arabic: Boolean): String {
    if (vehicle.demoMode) return if (arabic) "تجريبي" else "Demo"
    val live = vehicle.providerStatus.entries.firstOrNull { it.value.connected }?.key
    return if (live != null) {
        if (arabic) "متصل · ${live.labelAr}" else "Connected · ${live.labelEn}"
    } else {
        if (arabic) "غير متصل" else "Offline"
    }
}

private fun number(value: Number?, arabic: Boolean, decimals: Int = 0): String {
    if (value == null) return "—"
    val locale = if (arabic) Locale.forLanguageTag("ar") else Locale.US
    return NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = decimals
        maximumFractionDigits = decimals
    }.format(value)
}

private fun fileSize(bytes: Long, arabic: Boolean): String {
    if (bytes <= 0L) return "—"
    return number(bytes / (1024.0 * 1024.0), arabic, 1) + " MB"
}

private fun <T : Number> valueWithUnit(reading: Reading<T>, unit: String, arabic: Boolean, decimals: Int = 0): String =
    reading.value?.let { number(it, arabic, decimals) + unit } ?: "—"

private fun averageTire(tires: TireState, arabic: Boolean): String {
    val values = listOf(tires.frontLeftPsi.value, tires.frontRightPsi.value, tires.rearLeftPsi.value, tires.rearRightPsi.value).filterNotNull()
    return if (values.isEmpty()) "—" else number(values.average(), arabic, 1) + " PSI"
}

private fun wheelSpeedSummary(vehicle: VehicleSnapshot, arabic: Boolean): String {
    val values = listOf(
        vehicle.wheelFrontLeftKph.value,
        vehicle.wheelFrontRightKph.value,
        vehicle.wheelRearLeftKph.value,
        vehicle.wheelRearRightKph.value,
    )
    return if (values.all { it == null }) "—" else values.joinToString(" / ") { number(it, arabic, 0) }
}

private fun differentialSummary(vehicle: VehicleSnapshot, arabic: Boolean): String {
    val front = vehicle.frontDifferentialLocked.value
    val rear = vehicle.rearDifferentialLocked.value
    if (front == null && rear == null) return sourceName(vehicle.fourWheelDrive.source, arabic)
    val frontText = if (front == true) if (arabic) "أمامي مقفل" else "Front locked" else if (arabic) "أمامي مفتوح" else "Front open"
    val rearText = if (rear == true) if (arabic) "خلفي مقفل" else "Rear locked" else if (arabic) "خلفي مفتوح" else "Rear open"
    return "$frontText · $rearText"
}

private fun temperature(vehicle: VehicleSnapshot, arabic: Boolean): String =
    vehicle.outsideTempC.value?.let { number(it, arabic) + "°" } ?: "—°"

private fun directionName(heading: Float?, arabic: Boolean): String {
    if (heading == null) return "—"
    val normalized = ((heading % 360f) + 360f) % 360f
    return when {
        normalized < 22.5f || normalized >= 337.5f -> if (arabic) "شمال" else "North"
        normalized < 67.5f -> if (arabic) "شمال شرق" else "North-east"
        normalized < 112.5f -> if (arabic) "شرق" else "East"
        normalized < 157.5f -> if (arabic) "جنوب شرق" else "South-east"
        normalized < 202.5f -> if (arabic) "جنوب" else "South"
        normalized < 247.5f -> if (arabic) "جنوب غرب" else "South-west"
        normalized < 292.5f -> if (arabic) "غرب" else "West"
        else -> if (arabic) "شمال غرب" else "North-west"
    }
}

private fun yesNo(value: Boolean, arabic: Boolean): String =
    if (value) if (arabic) "نعم" else "Yes" else if (arabic) "لا" else "No"

private fun launchMaps(context: Context, arabic: Boolean) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Riyadh"))
    if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent) else unavailable(context, arabic)
}

private fun launchMedia(context: Context, arabic: Boolean) {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent) else unavailable(context, arabic)
}

private fun unavailable(context: Context, arabic: Boolean) {
    Toast.makeText(context, if (arabic) "لم يُعثر على تطبيق متوافق" else "No compatible app found", Toast.LENGTH_SHORT).show()
}
