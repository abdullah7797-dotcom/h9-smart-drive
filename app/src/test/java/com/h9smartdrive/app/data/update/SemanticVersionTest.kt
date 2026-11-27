package com.h9smartdrive.app.data.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticVersionTest {
    @Test
    fun betaFourIsNewerThanBetaThreeDebug() {
        assertTrue(SemanticVersion.isNewer("1.3.0-beta.3-debug", "v1.3.0-beta.4"))
    }

    @Test
    fun stableReleaseIsNewerThanPrerelease() {
        assertTrue(SemanticVersion.isNewer("1.3.0-beta.9", "1.3.0"))
    }

    @Test
    fun olderOrEqualReleaseIsRejected() {
        assertFalse(SemanticVersion.isNewer("1.3.0-beta.4", "1.3.0-beta.4"))
        assertFalse(SemanticVersion.isNewer("1.3.0-beta.4", "1.3.0-beta.3"))
    }
}
