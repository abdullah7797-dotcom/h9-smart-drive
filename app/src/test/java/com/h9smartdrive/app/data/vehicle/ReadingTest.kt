package com.h9smartdrive.app.data.vehicle

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingTest {
    @Test
    fun unavailableReadingIsStaleAndUnavailable() {
        val reading = Reading<Double>()
        assertFalse(reading.isAvailable())
        assertTrue(reading.isStale(now = 10_000))
    }

    @Test
    fun freshReadingIsAvailableAndNotStale() {
        val reading = Reading(
            value = 80.0,
            source = DataSource.GPS,
            updatedAtMillis = 9_000,
            staleAfterMillis = 2_000,
        )
        assertTrue(reading.isAvailable())
        assertFalse(reading.isStale(now = 10_000))
    }

    @Test
    fun readingBecomesStaleAfterThreshold() {
        val reading = Reading(
            value = 2_100,
            source = DataSource.GWM,
            updatedAtMillis = 1_000,
            staleAfterMillis = 2_000,
        )
        assertTrue(reading.isStale(now = 3_001))
    }
}
