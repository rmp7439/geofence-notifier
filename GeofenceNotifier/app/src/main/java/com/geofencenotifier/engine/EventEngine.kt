package com.geofencenotifier.engine
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.Event
import com.geofencenotifier.core.model.SmsJob
import com.geofencenotifier.core.model.CallJob
import android.util.Log

class EventEngine(private val dao: AppDao) {
    suspend fun processTransition(locationId: Long, transition: String) {
        val dedupeKey = "${locationId}_${transition}_${System.currentTimeMillis() / 600000}"
        val existing = dao.getEventByDedupeKey(dedupeKey)
        if (existing == null) {
            val eventId = dao.insertEvent(Event(locationId = locationId, transitionType = transition, dedupeKey = dedupeKey, status = "COMPLETE"))
            dao.insertSmsJob(SmsJob(eventId = eventId, recipientId = 1, renderedMessage = "Geofence $transition", status = "PENDING"))
            dao.insertCallJob(CallJob(eventId = eventId, recipientId = 1, sequenceNumber = 1, status = "PENDING"))
            Log.i("EventEngine", "Successfully created jobs")
        } else {
            Log.i("EventEngine", "Suppressed duplicate event")
        }
    }
}