package com.geofencenotifier.engine
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.Event
import com.geofencenotifier.core.model.SmsJob
import com.geofencenotifier.core.model.CallJob

class EventEngine(private val dao: AppDao) {
    suspend fun processTransition(locationId: Long, transition: String) {
        val dedupeKey = "${locationId}_${transition}_${System.currentTimeMillis() / 600000}"
        if (dao.getEventByDedupeKey(dedupeKey) != null) return
        
        val eventId = dao.insertEvent(Event(locationId = locationId, transitionType = transition, dedupeKey = dedupeKey, status = "IN_PROGRESS"))
        val rules = dao.getRulesByLocationIdSync(locationId)
        
        rules.forEach { rule ->
            val recipient = dao.getRecipientSync(rule.recipientId) ?: return@forEach
            val msg = rule.messageTemplate.replace("{location}", locationId.toString()).replace("{event}", transition)
            
            if (recipient.smsEnabled) {
                dao.insertSmsJob(SmsJob(eventId = eventId, recipientId = recipient.id, renderedMessage = msg, status = "PENDING"))
            }
            if (recipient.callEnabled) {
                for (i in 1..recipient.callCount) {
                    dao.insertCallJob(CallJob(eventId = eventId, recipientId = recipient.id, sequenceNumber = i, status = "PENDING"))
                }
            }
        }
        dao.updateEventStatus(eventId, "COMPLETE")
    }
}