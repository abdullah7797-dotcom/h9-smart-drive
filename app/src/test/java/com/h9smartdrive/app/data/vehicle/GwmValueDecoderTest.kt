package com.h9smartdrive.app.data.vehicle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GwmValueDecoderTest {
    @Test
    fun parsesObservedGwmTpmsPressureAndTemperaturePairs() {
        val result = GwmValueDecoder.parseTpms(
            "{2.48922,25.0,2.48922,24.0,2.2559,24.0,2.48922,24.0}",
        )

        assertEquals(36.1, result.pressurePsi[0]!!, 0.1)
        assertEquals(32.7, result.pressurePsi[2]!!, 0.1)
        assertEquals(25.0, result.temperatureC[0]!!, 0.0)
        assertEquals(24.0, result.temperatureC[3]!!, 0.0)
    }

    @Test
    fun rejectsIncompleteTpmsPayload() {
        val result = GwmValueDecoder.parseTpms("{2.4,25.0}")
        assertNull(result.pressurePsi[0])
        assertNull(result.temperatureC[0])
    }

    @Test
    fun decodesKnownDriveModesWithoutInventingUnknownLabels() {
        assertEquals("NORMAL", GwmValueDecoder.driveModeLabel("0"))
        assertEquals("SAND", GwmValueDecoder.driveModeLabel("4"))
        assertEquals("AWD", GwmValueDecoder.driveModeLabel("11"))
        assertEquals("27", GwmValueDecoder.driveModeLabel("27"))
    }
}
