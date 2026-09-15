package com.geofencenotifier.data.repository
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.*

class AppRepository(val dao: AppDao) {
    val locations = dao.getLocations()
    val recipients = dao.getRecipients()
    val events = dao.getEvents()
    val settings = dao.getSettings()
    val rules = dao.getRules()
    
    suspend fun insertLocation(loc: Location) = dao.insertLocation(loc)
    suspend fun updateLocation(loc: Location) = dao.updateLocation(loc)
    suspend fun deleteLocation(loc: Location) = dao.deleteLocation(loc)

    suspend fun insertRecipient(rec: Recipient) = dao.insertRecipient(rec)
    suspend fun updateRecipient(rec: Recipient) = dao.updateRecipient(rec)
    suspend fun deleteRecipient(rec: Recipient) = dao.deleteRecipient(rec)

    suspend fun insertRule(rule: NotificationRule) = dao.insertRule(rule)
    suspend fun updateRule(rule: NotificationRule) = dao.updateRule(rule)
    suspend fun deleteRule(rule: NotificationRule) = dao.deleteRule(rule)

    suspend fun saveSettings(settings: AppSettings) = dao.insertSettings(settings)
}