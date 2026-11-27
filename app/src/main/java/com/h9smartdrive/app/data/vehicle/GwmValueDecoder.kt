package com.h9smartdrive.app.data.vehicle

internal data class TpmsReadings(
    val pressurePsi: List<Double?> = List(4) { null },
    val temperatureC: List<Double?> = List(4) { null },
)

internal object GwmValueDecoder {
    fun parseTpms(raw: String?): TpmsReadings {
        if (raw == null) return TpmsReadings()
        val values = raw.trim('{', '}', '[', ']').split(',').map { it.trim().toDoubleOrNull() }
        if (values.size < 8) return TpmsReadings()
        val pressure = MutableList<Double?>(4) { null }
        val temperature = MutableList<Double?>(4) { null }
        repeat(4) { wheel ->
            pressure[wheel] = values[wheel * 2]?.let(::pressureToPsi)?.takeIf { it in 7.0..90.0 }
            temperature[wheel] = values[wheel * 2 + 1]?.takeIf { it in -50.0..150.0 }
        }
        return TpmsReadings(pressure, temperature)
    }

    fun pressureToPsi(value: Double): Double = when {
        value in 0.5..5.0 -> value * 14.503_773_8
        value in 20.0..80.0 -> value
        value in 80.0..600.0 -> value * 0.145_037_738
        else -> Double.NaN
    }

    fun driveModeLabel(raw: String): String = when (raw.toIntOrNull()) {
        0 -> "NORMAL"
        1 -> "SPORT"
        2 -> "ECO"
        3 -> "SNOW"
        4 -> "SAND"
        5 -> "MUD"
        11 -> "AWD"
        else -> raw
    }
}
