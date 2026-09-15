package com.geofencenotifier.data.repository
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.*

class AppRepository(val dao: AppDao) {
    val locations = dao.getLocations()
    val recipients = dao.getRecipients()
    val rules = dao.getRules()
    val events = dao.getEvents()
    val settings = dao.getSettings()

    suspend fun insertLocation(loc: Location) = dao.insertLocation(loc)
    suspend fun insertRecipient(rec: Recipient) = dao.insertRecipient(rec)
    suspend fun insertRule(rule: NotificationRule) = dao.insertRule(rule)
}