package com.geofencenotifier.engine
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.Event
import android.util.Log

class EventEngine(private val dao: AppDao) {
    suspend fun processTransition(locationId: Long, transition: String) {
        val dedupeKey = "${locationId}_${transition}_${System.currentTimeMillis() / 600000}"
        val existing = dao.getEventByDedupeKey(dedupeKey)
        if (existing == null) {
            dao.insertEvent(Event(locationId = locationId, transitionType = transition, dedupeKey = dedupeKey, status = "PENDING"))
            Log.i("EventEngine", "Event processed and queued")
        }
    }
}