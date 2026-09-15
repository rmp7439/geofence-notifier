package com.geofencenotifier.data.repository
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.*

class AppRepository(val dao: AppDao) {
    val locations = dao.getLocations()
    val recipients = dao.getRecipients()
    val events = dao.getEvents()
    val settings = dao.getSettings()
}