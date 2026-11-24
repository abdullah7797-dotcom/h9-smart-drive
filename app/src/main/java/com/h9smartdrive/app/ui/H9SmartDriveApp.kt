@file:OptIn(ExperimentalMaterial3Api::class)

package com.h9smartdrive.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.h9smartdrive.app.data.vehicle.DataSource
import com.h9smartdrive.app.data.vehicle.Reading
import com.h9smartdrive.app.data.vehicle.TireState
import com.h9smartdrive.app.data.vehicle.VehicleSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val NightBackground = Color(0xFF0D1117)
private val NightSurface = Color(0xFF161D27)
private val Accent = Color(0xFF60B5E8)
private val Good = Color(0xFF34D399)
private val WarningColor = Color(0xFFF59E0B)
private val Critical = Color(0xFFEF4444)
private val DayBackground = Color(0xFFF1F5F9)
private val DaySurface = Color.White

private val DarkColors = darkColorScheme(
    primary = Accent,
    background = NightBackground,
    surface = NightSurface,
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = Critical,
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF157EBA),
    background = DayBackground,
    surface = DaySurface,
    onBackground = Color(0xFF172033),
    onSurface = Color(0xFF172033),
    onSurfaceVariant = Color(0xFF526074),
    error = Critical,
)

private enum class Screen(val ar: String, val en: String, val icon: ImageVector) {
    HOME("الرئيسية", "Home", Icons.Rounded.Home),
    MAPS("الخرائط", "Maps", Icons.Rounded.Map),
    RADIO("الراديو", "Radio", Icons.Rounded.Radio),
    MEDIA("الوسائط", "Media", Icons.Rounded.LibraryMusic),
    VEHICLE("المركبة", "Vehicle", Icons.Rounded.DirectionsCar),
    OFFROAD("البر", "Off-road", Icons.Rounded.Explore),
    APPS("التطبيقات", "Apps", Icons.Rounded.Apps),
    SETTINGS("الإعدادات", "Settings", Icons.Rounded.Settings),
}

@Composable
fun H9SmartDriveApp(viewModel: H9ViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val arabic = state.settings.language == "ar"
    var screen by remember { mutableStateOf(Screen.HOME) }

    MaterialTheme(colorScheme = if (state.settings.darkMode) DarkColors else LightColors) {
        CompositionLocalProvider(
            LocalLayoutDirection provides if (arabic) LayoutDirection.Rtl else LayoutDirection.Ltr,
        ) {
            Scaffold(containerColor = MaterialTheme.colorScheme.background) { insets ->
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(insets)
                        .padding(12.dp),
                ) {
                    NavigationRail(screen, arabic, onSelect = { screen = it })
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.fillMaxSize()) {
                        TopBar(
                            screen = screen,
                            arabic = arabic,
                            vehicle = state.vehicle,
                        )
                        Spacer(Modifier.height(12.dp))
                        Box(Modifier.fillMaxSize()) {
                            when (screen) {
                                Screen.HOME -> DashboardScreen(state.vehicle, arabic)
                                Screen.VEHICLE -> VehicleScreen(state.vehicle, arabic)
                                Screen.OFFROAD -> OffRoadScreen(state.vehicle, arabic)
                                Screen.SETTINGS -> SettingsScreen(state, arabic, viewModel)
                                Screen.APPS -> AppsScreen(arabic)
                                Screen.MAPS -> LauncherScreen(screen, arabic, state.vehicle)
                                Screen.RADIO -> LauncherScreen(screen, arabic, state.vehicle)
                                Screen.MEDIA -> LauncherScreen(screen, arabic, state.vehicle)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationRail(current: Screen, arabic: Boolean, onSelect: (Screen) -> Unit) {
    Card(
        modifier = Modifier.width(112.dp).fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(Accent),
                contentAlignment = Alignment.Center,
            ) {
                Text("H9", color = NightBackground, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Screen.entries.forEach { item ->
                val selected = current == item
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) Accent.copy(alpha = .16f) else Color.Transparent)
                        .clickable { onSelect(item) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(23.dp),
                    )
                    Text(
                        if (arabic) item.ar else item.en,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(screen: Screen, arabic: Boolean, vehicle: VehicleSnapshot) {
    val time = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }
    Row(
        Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                if (arabic) screen.ar else screen.en,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                if (arabic) "H9 Smart Drive · تجربة قيادة متصلة" else "H9 Smart Drive · Connected driving",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (vehicle.demoMode) StatusPill(
                text = if (arabic) "وضع تجريبي" else "DEMO MODE",
                color = WarningColor,
                icon = Icons.Rounded.Warning,
            )
            StatusPill(
                text = if (vehicle.demoMode) (if (arabic) "محاكاة محلية" else "Local simulation") else (if (arabic) "فحص الاتصال" else "Checking data"),
                color = if (vehicle.demoMode) Good else Accent,
                icon = Icons.Rounded.Sensors,
            )
            Text(time, fontSize = 24.sp, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color, icon: ImageVector) {
    Row(
        Modifier.clip(CircleShape).background(color.copy(alpha = .13f)).border(1.dp, color.copy(alpha = .35f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(15.dp))
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DashboardScreen(vehicle: VehicleSnapshot, arabic: Boolean) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1.25f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SpeedCard(vehicle, arabic, Modifier.weight(1f))
            Row(Modifier.weight(.52f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    if (arabic) "حرارة المحرك" else "Engine temp",
                    display(vehicle.engineTempC, "°C"),
                    Icons.Rounded.Thermostat,
                    Good,
                    Modifier.weight(1f),
                    vehicle.engineTempC.source,
                    arabic,
                )
                MetricCard(
                    if (arabic) "جهد البطارية" else "Battery",
                    display(vehicle.batteryVoltage, "V", 1),
                    Icons.Rounded.BatteryChargingFull,
                    Accent,
                    Modifier.weight(1f),
                    vehicle.batteryVoltage.source,
                    arabic,
                )
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TireCard(vehicle.tires, arabic, Modifier.weight(1f))
            FuelCard(vehicle, arabic, Modifier.weight(.62f))
        }
        Column(Modifier.weight(.9f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MediaCard(arabic, Modifier.weight(.72f))
            QuickStatusCard(vehicle, arabic, Modifier.weight(1f))
        }
    }
}

@Composable
private fun H9Card(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        content = { Box(Modifier.fillMaxSize().padding(20.dp)) { content() } },
    )
}

@Composable
private fun SpeedCard(vehicle: VehicleSnapshot, arabic: Boolean, modifier: Modifier) {
    H9Card(modifier) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                SpeedGauge(vehicle.speedKph.value ?: 0.0)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        vehicle.speedKph.value?.toInt()?.toString() ?: "—",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-3).sp,
                    )
                    Text("km/h", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                }
            }
            Column(Modifier.width(116.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (arabic) "السرعة" else "SPEED", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                Spacer(Modifier.height(18.dp))
                Text(vehicle.gear.value ?: "—", color = Accent, fontSize = 44.sp, fontWeight = FontWeight.Bold)
                Text(display(vehicle.rpm, " RPM"), fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                SourceLabel(vehicle.speedKph.source, arabic)
            }
        }
    }
}

@Composable
private fun SpeedGauge(speed: Double) {
    val track = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .16f)
    Canvas(Modifier.fillMaxSize().padding(8.dp)) {
        val stroke = size.minDimension * .045f
        val arcSize = size.minDimension - stroke
        val topLeft = Offset((size.width - arcSize) / 2f, (size.height - arcSize) / 2f)
        drawArc(track, 135f, 270f, false, topLeft, Size(arcSize, arcSize), style = Stroke(stroke, cap = StrokeCap.Round))
        drawArc(Accent, 135f, (speed.coerceIn(0.0, 200.0) / 200f * 270f).toFloat(), false, topLeft, Size(arcSize, arcSize), style = Stroke(stroke, cap = StrokeCap.Round))
    }
}

@Composable
private fun TireCard(tires: TireState, arabic: Boolean, modifier: Modifier) {
    H9Card(modifier) {
        Column(Modifier.fillMaxSize()) {
            SectionHeader(if (arabic) "ضغط الإطارات" else "Tire pressure", Icons.Rounded.DirectionsCar)
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.SpaceAround) {
                    TireReading(if (arabic) "أمامي يسار" else "Front left", tires.frontLeftPsi)
                    TireReading(if (arabic) "خلفي يسار" else "Rear left", tires.rearLeftPsi)
                }
                CarTopView(Modifier.width(92.dp).fillMaxHeight().padding(vertical = 10.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.End) {
                    TireReading(if (arabic) "أمامي يمين" else "Front right", tires.frontRightPsi, Alignment.End)
                    TireReading(if (arabic) "خلفي يمين" else "Rear right", tires.rearRightPsi, Alignment.End)
                }
            }
        }
    }
}

@Composable
private fun TireReading(label: String, reading: Reading<Double>, alignment: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = alignment) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        Text(display(reading, " PSI", 1), fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
    }
}

@Composable
private fun CarTopView(modifier: Modifier) {
    val body = MaterialTheme.colorScheme.onSurface.copy(alpha = .88f)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(body.copy(alpha = .12f), Offset(w * .2f, h * .08f), Size(w * .6f, h * .84f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .25f))
        drawRoundRect(body, Offset(w * .28f, h * .12f), Size(w * .44f, h * .76f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .18f), style = Stroke(2.dp.toPx()))
        drawLine(body.copy(alpha = .5f), Offset(w * .3f, h * .35f), Offset(w * .7f, h * .35f), 2.dp.toPx())
        drawLine(body.copy(alpha = .5f), Offset(w * .3f, h * .65f), Offset(w * .7f, h * .65f), 2.dp.toPx())
        listOf(.2f to .22f, .8f to .22f, .2f to .72f, .8f to .72f).forEach { (x, y) ->
            drawRoundRect(Good, Offset(w * x - 4.dp.toPx(), h * y), Size(8.dp.toPx(), h * .14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
        }
    }
}

@Composable
private fun FuelCard(vehicle: VehicleSnapshot, arabic: Boolean, modifier: Modifier) {
    H9Card(modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            SectionHeader(if (arabic) "الوقود والمدى" else "Fuel & range", Icons.Rounded.LocalGasStation)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(vehicle.fuelPercent.value?.toString() ?: "—", fontSize = 42.sp, fontWeight = FontWeight.Light)
                Text("%", color = Accent, fontSize = 18.sp, modifier = Modifier.padding(bottom = 7.dp))
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (arabic) "المدى المتوقع" else "Estimated range", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    Text(display(vehicle.rangeKm, " km"), fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            ProgressTrack((vehicle.fuelPercent.value ?: 0) / 100f, Accent)
        }
    }
}

@Composable
private fun MediaCard(arabic: Boolean, modifier: Modifier) {
    H9Card(modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            SectionHeader(if (arabic) "الراديو والوسائط" else "Radio & media", Icons.Rounded.Radio)
            Column {
                Text("98.7", fontSize = 38.sp, fontWeight = FontWeight.Light)
                Text("FM · RADIO", color = Accent, fontSize = 12.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                Text(if (arabic) "افتح التطبيق الأصلي للتحكم الآمن" else "Open the native app for safe control", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun QuickStatusCard(vehicle: VehicleSnapshot, arabic: Boolean, modifier: Modifier) {
    H9Card(modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader(if (arabic) "حالة المركبة" else "Vehicle status", Icons.Rounded.CheckCircle)
            StatusRow(if (arabic) "وضع القيادة" else "Drive mode", vehicle.driveMode.value ?: "—", Accent)
            StatusRow(if (arabic) "نظام الدفع" else "Drivetrain", vehicle.fourWheelDrive.value ?: "—", Good)
            StatusRow(if (arabic) "الحرارة الخارجية" else "Outside", display(vehicle.outsideTempC, "°C", 1), MaterialTheme.colorScheme.onSurface)
            StatusRow(if (arabic) "عداد المسافة" else "Odometer", display(vehicle.odometerKm, " km", 1), MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun VehicleScreen(vehicle: VehicleSnapshot, arabic: Boolean) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TireCard(vehicle.tires, arabic, Modifier.weight(1.1f))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(if (arabic) "عدد دورات المحرك" else "Engine RPM", display(vehicle.rpm, " RPM"), Icons.Rounded.Speed, Accent, Modifier.weight(1f), vehicle.rpm.source, arabic)
            MetricCard(if (arabic) "حرارة المحرك" else "Engine temperature", display(vehicle.engineTempC, "°C"), Icons.Rounded.Thermostat, Good, Modifier.weight(1f), vehicle.engineTempC.source, arabic)
            MetricCard(if (arabic) "البطارية" else "Battery voltage", display(vehicle.batteryVoltage, " V", 1), Icons.Rounded.BatteryChargingFull, Accent, Modifier.weight(1f), vehicle.batteryVoltage.source, arabic)
        }
        H9Card(Modifier.weight(.9f)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionHeader(if (arabic) "معلومات القيادة" else "Drive information", Icons.Rounded.DirectionsCar)
                StatusRow(if (arabic) "ناقل الحركة" else "Gear", vehicle.gear.value ?: "—", Accent)
                StatusRow(if (arabic) "وضع القيادة" else "Mode", vehicle.driveMode.value ?: "—", Good)
                StatusRow(if (arabic) "الدفع الرباعي" else "4WD", vehicle.fourWheelDrive.value ?: "—", Good)
                StatusRow(if (arabic) "الوقود" else "Fuel", display(vehicle.fuelPercent, "%"), MaterialTheme.colorScheme.onSurface)
                StatusRow(if (arabic) "المدى" else "Range", display(vehicle.rangeKm, " km"), MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.weight(1f))
                Text(if (arabic) "القراءات غير المتاحة لا تُستبدل بقيم تقديرية." else "Unavailable readings are never replaced with estimates.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun OffRoadScreen(vehicle: VehicleSnapshot, arabic: Boolean) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        H9Card(Modifier.weight(1.05f)) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                SectionHeader(if (arabic) "البوصلة" else "Compass", Icons.Rounded.Explore)
                Compass(vehicle.headingDegrees.value ?: 0f, Modifier.weight(1f).fillMaxWidth())
                Text("${vehicle.headingDegrees.value?.toInt() ?: 0}° · NW", color = Accent, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(if (arabic) "الارتفاع" else "Altitude", display(vehicle.altitudeMeters, " m", 0), Icons.Rounded.Navigation, Accent, Modifier.weight(1f), vehicle.altitudeMeters.source, arabic)
            H9Card(Modifier.weight(1f)) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    SectionHeader(if (arabic) "الموقع" else "Location", Icons.Rounded.LocationOn)
                    Text("${vehicle.latitude.value?.let { "%.4f".format(it) } ?: "—"}, ${vehicle.longitude.value?.let { "%.4f".format(it) } ?: "—"}", fontSize = 18.sp)
                    SourceLabel(vehicle.latitude.source, arabic)
                }
            }
            H9Card(Modifier.weight(1f)) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    SectionHeader(if (arabic) "الميلان" else "Tilt", Icons.Rounded.Explore)
                    Text("—°  /  —°", fontSize = 30.sp, fontWeight = FontWeight.Light)
                    Text(if (arabic) "غير متاح حتى توفر مستشعر مُعاير" else "Unavailable until a calibrated sensor is found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
        }
        Column(Modifier.weight(.95f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TireCard(vehicle.tires, arabic, Modifier.weight(1.2f))
            H9Card(Modifier.weight(.8f)) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(if (arabic) "المسار" else "Trail", Icons.Rounded.Route)
                    ActionRow(if (arabic) "حفظ نقطة الانطلاق" else "Save start point")
                    ActionRow(if (arabic) "حفظ موقع المخيم" else "Save camp")
                    Text(if (arabic) "ليست بديلًا عن أدوات السلامة أو الملاحة الأصلية." else "Not a replacement for safety equipment or native navigation.", color = WarningColor, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun Compass(heading: Float, modifier: Modifier) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Canvas(modifier.padding(18.dp)) {
        val radius = size.minDimension * .42f
        val c = center
        drawCircle(onSurface.copy(alpha = .1f), radius)
        drawCircle(onSurface.copy(alpha = .35f), radius, style = Stroke(2.dp.toPx()))
        for (i in 0 until 24) {
            val angle = Math.toRadians((i * 15 - 90).toDouble())
            val inner = if (i % 6 == 0) radius * .78f else radius * .88f
            drawLine(
                onSurface.copy(alpha = if (i % 6 == 0) .8f else .25f),
                Offset(c.x + cos(angle).toFloat() * inner, c.y + sin(angle).toFloat() * inner),
                Offset(c.x + cos(angle).toFloat() * radius, c.y + sin(angle).toFloat() * radius),
                if (i % 6 == 0) 3.dp.toPx() else 1.dp.toPx(),
            )
        }
        val angle = Math.toRadians((heading - 90).toDouble())
        drawLine(Accent, c, Offset(c.x + cos(angle).toFloat() * radius * .66f, c.y + sin(angle).toFloat() * radius * .66f), 5.dp.toPx(), StrokeCap.Round)
        drawCircle(Accent, 7.dp.toPx(), c)
    }
}

@Composable
private fun AppsScreen(arabic: Boolean) {
    val context = LocalContext.current
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AppTile(if (arabic) "الخرائط" else "Maps", Icons.Rounded.Map, Accent, Modifier.weight(1f)) { launchMaps(context) }
        AppTile(if (arabic) "الراديو" else "Radio", Icons.Rounded.Radio, WarningColor, Modifier.weight(1f)) { launchCategory(context, Intent.CATEGORY_APP_MUSIC) }
        AppTile(if (arabic) "الوسائط" else "Media", Icons.Rounded.LibraryMusic, Good, Modifier.weight(1f)) { launchCategory(context, Intent.CATEGORY_APP_MUSIC) }
        AppTile(if (arabic) "تطبيقات السيارة" else "Car apps", Icons.Rounded.Apps, Color(0xFFB69CF6), Modifier.weight(1f)) { showUnavailable(context, arabic) }
    }
}

@Composable
private fun AppTile(title: String, icon: ImageVector, color: Color, modifier: Modifier, action: () -> Unit) {
    Card(
        modifier.clickable(onClick = action),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = .15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(34.dp))
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LauncherScreen(screen: Screen, arabic: Boolean, vehicle: VehicleSnapshot) {
    val context = LocalContext.current
    H9Card(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
            Box(Modifier.size(140.dp).clip(CircleShape).background(Accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                Icon(screen.icon, null, tint = Accent, modifier = Modifier.size(72.dp))
            }
            Column(Modifier.widthIn(max = 520.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(if (arabic) screen.ar else screen.en, fontSize = 38.sp, fontWeight = FontWeight.Light)
                Text(
                    if (arabic) "سيحاول H9 Smart Drive فتح التطبيق الأصلي المتوافق، دون تعديل التطبيق أو تجاوز صلاحيات النظام."
                    else "H9 Smart Drive will open a compatible native app without modifying it or bypassing system permissions.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                )
                Card(
                    Modifier.clickable {
                        if (screen == Screen.MAPS) launchMaps(context) else launchCategory(context, Intent.CATEGORY_APP_MUSIC)
                    },
                    colors = CardDefaults.cardColors(containerColor = Accent),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(Modifier.padding(horizontal = 22.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (arabic) "فتح التطبيق الأصلي" else "Open native app", color = NightBackground, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, tint = NightBackground)
                    }
                }
                if (!vehicle.demoMode) Text(if (arabic) "سيتم تسجيل نتيجة الاكتشاف في صفحة التشخيص." else "Discovery results are recorded in Diagnostics.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SettingsScreen(state: H9UiState, arabic: Boolean, viewModel: H9ViewModel) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        H9Card(Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SectionHeader(if (arabic) "المظهر واللغة" else "Appearance & language", Icons.Rounded.Settings)
                ToggleRow(if (arabic) "الوضع الليلي" else "Dark mode", if (arabic) "ألوان هادئة للقيادة الليلية" else "Low-glare night palette", state.settings.darkMode, viewModel::setDarkMode)
                ToggleRow(if (arabic) "اللغة العربية" else "Arabic language", if (arabic) "تبديل اتجاه الواجهة RTL" else "Switch layout direction to RTL", arabic) { viewModel.setLanguage(if (it) "ar" else "en") }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f))
                Text(if (arabic) "نمط لوحة القيادة" else "Dashboard style", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("classic", "modern", "minimal", "offroad").forEach { style ->
                        ChoiceChip(style, state.settings.dashboardStyle == style) { viewModel.setDashboardStyle(style) }
                    }
                }
            }
        }
        H9Card(Modifier.weight(.9f)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectionHeader(if (arabic) "مصادر البيانات" else "Data sources", Icons.Rounded.Sensors)
                ToggleRow(
                    if (arabic) "الوضع التجريبي" else "Demo mode",
                    if (arabic) "بيانات محاكاة مع شارة واضحة" else "Simulated data with a visible badge",
                    state.settings.demoMode,
                    viewModel::setDemoMode,
                )
                DataSource.entries.filter { it != DataSource.UNAVAILABLE && it != DataSource.DEMO }.forEach { source ->
                    val status = state.vehicle.providerStatus[source]
                    StatusRow(
                        if (arabic) source.labelAr else source.labelEn,
                        when {
                            status?.connected == true -> if (arabic) "متصل" else "Connected"
                            status?.available == true -> if (arabic) "مكتشف" else "Detected"
                            else -> if (arabic) "غير متاح" else "Unavailable"
                        },
                        if (status?.connected == true) Good else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        H9Card(Modifier.weight(1.1f)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(if (arabic) "تشخيص النظام" else "System diagnostics", Icons.Rounded.BugReport)
                state.diagnostics?.let { d ->
                    DiagnosticRow("Android", "${d.androidVersion} · SDK ${d.sdk}")
                    DiagnosticRow(if (arabic) "الجهاز" else "Device", "${d.manufacturer} ${d.model}")
                    DiagnosticRow(if (arabic) "الشاشة" else "Display", d.screen)
                    DiagnosticRow("Android Automotive", yesNo(d.automotive, arabic))
                    DiagnosticRow("GPS", yesNo(d.gps, arabic))
                    DiagnosticRow(if (arabic) "صلاحية الموقع" else "Location permission", yesNo(d.locationPermission, arabic))
                    DiagnosticRow(if (arabic) "حزمة GWM" else "GWM package", yesNo(d.gwmPackageVisible, arabic))
                    DiagnosticRow(if (arabic) "تطبيق خرائط" else "Maps handler", yesNo(d.mapsHandler, arabic))
                }
                Spacer(Modifier.weight(1f))
                Text(if (arabic) "التقرير لا يتضمن VIN أو إحداثيات أو معرفات شخصية." else "The report excludes VIN, coordinates, and personal identifiers.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier, source: DataSource, arabic: Boolean) {
    H9Card(modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(color.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Light)
            SourceLabel(source, arabic)
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(9.dp))
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun SourceLabel(source: DataSource, arabic: Boolean) {
    Text(
        (if (arabic) "المصدر: " else "Source: ") + if (arabic) source.labelAr else source.labelEn,
        color = if (source == DataSource.DEMO) WarningColor else MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 9.sp,
    )
}

@Composable
private fun StatusRow(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(7.dp))
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, modifier = Modifier.width(112.dp))
        Text(value, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label.replaceFirstChar { it.uppercase() },
        color = if (selected) NightBackground else MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp,
        modifier = Modifier.clip(CircleShape).background(if (selected) Accent else MaterialTheme.colorScheme.onSurface.copy(alpha = .06f)).clickable(onClick = onClick).padding(horizontal = 11.dp, vertical = 8.dp),
    )
}

@Composable
private fun ActionRow(text: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Accent.copy(alpha = .08f)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.LocationOn, null, tint = Accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun ProgressTrack(progress: Float, color: Color) {
    Box(Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = .08f))) {
        Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().clip(CircleShape).background(color))
    }
}

private fun <T> display(reading: Reading<T>, suffix: String): String = reading.value?.let { "$it$suffix" } ?: "—"
private fun display(reading: Reading<Double>, suffix: String, decimals: Int): String = reading.value?.let { "%.${decimals}f%s".format(Locale.US, it, suffix) } ?: "—"
private fun yesNo(value: Boolean, arabic: Boolean) = if (value) (if (arabic) "نعم" else "Yes") else (if (arabic) "لا" else "No")

private fun launchMaps(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Riyadh"))
    if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent) else showUnavailable(context, true)
}

private fun launchCategory(context: Context, category: String) {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(category).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent) else showUnavailable(context, true)
}

private fun showUnavailable(context: Context, arabic: Boolean) {
    Toast.makeText(context, if (arabic) "لم يُعثر على تطبيق متوافق" else "No compatible app found", Toast.LENGTH_SHORT).show()
}
