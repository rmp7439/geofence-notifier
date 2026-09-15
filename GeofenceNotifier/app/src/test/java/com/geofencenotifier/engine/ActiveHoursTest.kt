package com.geofencenotifier.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ActiveHoursTest {

    fun isInWindow(startStr: String, endStr: String, currentHour: Int, currentMin: Int): Boolean {
        val currentMinutes = currentHour * 60 + currentMin
        val hrRegex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")
        if (!hrRegex.matches(startStr) || !hrRegex.matches(endStr)) return false
        
        val sParts = startStr.split(":")
        val eParts = endStr.split(":")
        val startMins = sParts[0].toInt() * 60 + sParts[1].toInt()
        val endMins = eParts[0].toInt() * 60 + eParts[1].toInt()
        
        return if (startMins <= endMins) {
            currentMinutes in startMins..endMins
        } else {
            currentMinutes >= startMins || currentMinutes <= endMins
        }
    }

    @Test
    fun testNormalWindowInside() {
        assertTrue(isInWindow("08:00", "18:00", 12, 0))
    }

    @Test
    fun testNormalWindowOutside() {
        assertFalse(isInWindow("08:00", "18:00", 19, 0))
    }

    @Test
    fun testOvernightInsideBeforeMidnight() {
        assertTrue(isInWindow("22:00", "06:00", 23, 30))
    }

    @Test
    fun testOvernightInsideAfterMidnight() {
        assertTrue(isInWindow("22:00", "06:00", 2, 0))
    }

    @Test
    fun testOvernightOutside() {
        assertFalse(isInWindow("22:00", "06:00", 12, 0))
    }
    
    @Test
    fun testMalformed() {
        assertFalse(isInWindow("25:00", "06:00", 2, 0))
        assertFalse(isInWindow("22:00", "06:65", 2, 0))
        assertFalse(isInWindow("abcd", "efgh", 2, 0))
        assertFalse(isInWindow("", "", 2, 0))
    }
}
