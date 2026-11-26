package com.h9smartdrive.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.DirectionsCar
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
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.TireRepair
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
                            CarHeader(selected, state.vehicle, arabic)
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
private fun CarHeader(screen: CarScreen, vehicle: VehicleSnapshot, arabic: Boolean) {
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
            HeaderChip(Icons.Rounded.Wifi, if (vehicle.demoMode) if (arabic) "تجريبي" else "Demo" else if (arabic) "متصل" else "Connected")
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
private fun HomeLandscape(vehicle: VehicleSnapshot, arabic: Boolean) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        SpeedHero(vehicle, arabic, Modifier.weight(1.34f))
        VehicleStatusPane(vehicle, arabic, Modifier.weight(.92f))
        JourneyPane(arabic, Modifier.weight(.74f))
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
private fun JourneyPane(arabic: Boolean, modifier: Modifier = Modifier) {
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
                Text(if (arabic) "الرياض" else "Riyadh", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(if (arabic) "١٦ كم · ٢٤ دقيقة" else "16 km · 24 min", style = MaterialTheme.typography.bodyLarge)
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
        InfoPane(Modifier.weight(1f), if (arabic) "الإطارات" else "Tires") {
            TireMetric(if (arabic) "أمامي يسار" else "Front left", vehicle.tires.frontLeftPsi, arabic)
            TireMetric(if (arabic) "أمامي يمين" else "Front right", vehicle.tires.frontRightPsi, arabic)
            TireMetric(if (arabic) "خلفي يسار" else "Rear left", vehicle.tires.rearLeftPsi, arabic)
            TireMetric(if (arabic) "خلفي يمين" else "Rear right", vehicle.tires.rearRightPsi, arabic)
        }
        InfoPane(Modifier.weight(1f), if (arabic) "المحرك والطاقة" else "Engine & power") {
            MetricRow(Icons.Rounded.Speed, if (arabic) "دورات المحرك" else "Engine RPM", number(vehicle.rpm.value, arabic), sourceName(vehicle.rpm.source, arabic))
            MetricRow(Icons.Rounded.Thermostat, if (arabic) "حرارة المحرك" else "Engine temp", valueWithUnit(vehicle.engineTempC, "°", arabic), sourceName(vehicle.engineTempC.source, arabic))
            MetricRow(Icons.Rounded.BatteryChargingFull, if (arabic) "جهد البطارية" else "Battery", valueWithUnit(vehicle.batteryVoltage, "V", arabic, 1), sourceName(vehicle.batteryVoltage.source, arabic))
            MetricRow(Icons.Rounded.LocalGasStation, if (arabic) "الوقود" else "Fuel", valueWithUnit(vehicle.fuelPercent, "%", arabic), valueWithUnit(vehicle.rangeKm, if (arabic) " كم" else " km", arabic))
        }
        InfoPane(Modifier.weight(.86f), if (arabic) "نظام القيادة" else "Drivetrain") {
            LargeValue(vehicle.gear.value ?: "—", if (arabic) "ناقل الحركة" else "Gear")
            LargeValue(vehicle.driveMode.value ?: "—", if (arabic) "وضع القيادة" else "Drive mode")
            LargeValue(vehicle.fourWheelDrive.value ?: "—", if (arabic) "الدفع الرباعي" else "Four-wheel drive")
            Spacer(Modifier.weight(1f))
            Text(
                if (arabic) "القيم غير المتاحة لا تُستبدل بتقديرات." else "Unavailable values are never replaced with estimates.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoPane(Modifier.weight(1f), if (arabic) "المظهر واللغة" else "Appearance & language") {
            ToggleSetting(Icons.Rounded.Settings, if (arabic) "الوضع الليلي" else "Dark mode", if (arabic) "ألوان أقل سطوعًا" else "Lower-glare palette", state.settings.darkMode, viewModel::setDarkMode)
            ToggleSetting(Icons.Rounded.Language, if (arabic) "اللغة العربية" else "Arabic language", if (arabic) "اتجاه واجهة RTL" else "RTL interface", arabic) { viewModel.setLanguage(if (it) "ar" else "en") }
        }
        InfoPane(Modifier.weight(1f), if (arabic) "مصادر البيانات" else "Data sources") {
            ToggleSetting(Icons.Rounded.Sensors, if (arabic) "الوضع التجريبي" else "Demo mode", if (arabic) "بيانات محاكاة واضحة" else "Clearly marked simulated data", state.settings.demoMode, viewModel::setDemoMode)
            DataSource.entries.filter { it != DataSource.UNAVAILABLE }.take(4).forEach { source ->
                val status = state.vehicle.providerStatus[source]
                MetricRow(
                    Icons.Rounded.Sensors,
                    if (arabic) source.labelAr else source.labelEn,
                    if (status?.connected == true) if (arabic) "متصل" else "Connected" else if (arabic) "غير متاح" else "Unavailable",
                    status?.detail ?: "—",
                )
            }
        }
        InfoPane(Modifier.weight(.9f), if (arabic) "معلومات النظام" else "System information") {
            val d = state.diagnostics
            MetricRow(Icons.Rounded.Apps, "Android", d?.androidVersion ?: "—", "SDK ${d?.sdk ?: "—"}")
            MetricRow(Icons.Rounded.DirectionsCar, if (arabic) "الجهاز" else "Device", d?.model ?: "—", d?.manufacturer ?: "—")
            MetricRow(Icons.Rounded.Map, if (arabic) "الشاشة" else "Display", d?.screen ?: "—", "")
            MetricRow(Icons.Rounded.LocationOn, "GPS", yesNo(d?.gps == true, arabic), if (arabic) "الموقع" else "Location")
            Spacer(Modifier.weight(1f))
            Text(
                if (arabic) "لا يتضمن التقرير معرفات شخصية أو رقم الهيكل." else "The report excludes personal identifiers and VIN.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoPane(modifier: Modifier, title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            Modifier.fillMaxSize().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
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
private fun TireMetric(label: String, reading: Reading<Double>, arabic: Boolean) {
    MetricRow(Icons.Rounded.TireRepair, label, valueWithUnit(reading, " PSI", arabic, 1), sourceName(reading.source, arabic))
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

private fun number(value: Number?, arabic: Boolean, decimals: Int = 0): String {
    if (value == null) return "—"
    val locale = if (arabic) Locale.forLanguageTag("ar") else Locale.US
    return NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = decimals
        maximumFractionDigits = decimals
    }.format(value)
}

private fun <T : Number> valueWithUnit(reading: Reading<T>, unit: String, arabic: Boolean, decimals: Int = 0): String =
    reading.value?.let { number(it, arabic, decimals) + unit } ?: "—"

private fun averageTire(tires: TireState, arabic: Boolean): String {
    val values = listOf(tires.frontLeftPsi.value, tires.frontRightPsi.value, tires.rearLeftPsi.value, tires.rearRightPsi.value).filterNotNull()
    return if (values.isEmpty()) "—" else number(values.average(), arabic, 1) + " PSI"
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
