package com.geofencenotifier
import org.junit.Test
import org.junit.Assert.*
import com.geofencenotifier.core.model.*

class EngineTest {

    @Test
    fun testDedupeKeyGeneration() {
        val locationId = 1L
        val transition = "ENTER"
        val cooldownMins = 10
        val cooldownMillis = cooldownMins * 60000L
        val timeWindow = System.currentTimeMillis() / cooldownMillis
        val dedupeKey = "${locationId}_${transition}_$timeWindow"
        
        assertTrue(dedupeKey.startsWith("1_ENTER_"))
    }
    
    @Test
    fun testTemplateRendering() {
        val template = "Alert! {location} triggered {event} at {time}."
        val locationName = "Home"
        val event = "EXIT"
        val time = "12:00 PM"
        
        val rendered = template
            .replace("{location}", locationName)
            .replace("{event}", event)
            .replace("{time}", time)
            
        assertEquals("Alert! Home triggered EXIT at 12:00 PM.", rendered)
    }
    
    @Test
    fun testCooldownWindowLogic() {
        val cooldownMillis = 10 * 60000L
        val t1 = 1600000000000L
        val t2 = t1 + 5000L // 5 seconds later
        val t3 = t1 + (15 * 60000L) // 15 mins later
        
        val w1 = t1 / cooldownMillis
        val w2 = t2 / cooldownMillis
        val w3 = t3 / cooldownMillis
        
        assertEquals(w1, w2)
        assertNotEquals(w1, w3)
    }
}