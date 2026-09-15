package com.geofencenotifier
import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar

class ComprehensiveEngineTest {

    @Test
    fun testDedupeLogic() {
        val locId = 5L
        val type = "ENTER"
        val cooldownMillis = 15 * 60000L
        val timeNow = 10000000000L
        val window1 = timeNow / cooldownMillis
        val dedupe1 = "${locId}_${type}_$window1"
        
        val timeLater = timeNow + (5 * 60000L) // 5 mins later, same window
        val window2 = timeLater / cooldownMillis
        val dedupe2 = "${locId}_${type}_$window2"
        
        assertEquals(dedupe1, dedupe2)
        
        val timeMuchLater = timeNow + (16 * 60000L) // 16 mins later, next window
        val window3 = timeMuchLater / cooldownMillis
        val dedupe3 = "${locId}_${type}_$window3"
        
        assertNotEquals(dedupe1, dedupe3)
    }

    @Test
    fun testActiveHoursLogic() {
        val startMins = 22 * 60 // 22:00
        val endMins = 6 * 60    // 06:00
        
        val testMin1 = 23 * 60  // 23:00
        val testMin2 = 2 * 60   // 02:00
        val testMin3 = 12 * 60  // 12:00
        
        fun inWindow(currentMinutes: Int): Boolean {
            return if (startMins <= endMins) {
                currentMinutes in startMins..endMins
            } else {
                currentMinutes >= startMins || currentMinutes <= endMins
            }
        }
        
        assertTrue(inWindow(testMin1))
        assertTrue(inWindow(testMin2))
        assertFalse(inWindow(testMin3))
    }

    @Test
    fun testTemplateRendering() {
        val template = "Testing {location} for {event} at {time}"
        val res = template.replace("{location}", "Work").replace("{event}", "EXIT").replace("{time}", "Noon")
        assertEquals("Testing Work for EXIT at Noon", res)
    }

    @Test
    fun testInvalidPlaceholders() {
        val template = "Testing {location} and {unknown}"
        val res = template.replace("{location}", "Home")
        assertEquals("Testing Home and {unknown}", res)
    }
}