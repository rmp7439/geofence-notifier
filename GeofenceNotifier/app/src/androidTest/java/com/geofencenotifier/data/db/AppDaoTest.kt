package com.geofencenotifier.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.geofencenotifier.core.model.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class AppDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: AppDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.dao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testEventDedupe() = runBlocking {
        dao.insertLocation(Location(id = 1, name = "Loc", latitude = 0.0, longitude = 0.0, radiusMeters = 100f))
        
        val event1 = Event(locationId = 1, transitionType = "ENTER", dedupeKey = "DEDUPE_1", status = "PROCESSING")
        val id1 = dao.insertEventWithJobs(event1, emptyList(), emptyList())
        assertTrue(id1 > 0)
        
        val event2 = Event(locationId = 1, transitionType = "ENTER", dedupeKey = "DEDUPE_1", status = "PROCESSING")
        val id2 = dao.insertEventWithJobs(event2, emptyList(), emptyList())
        assertEquals(-1L, id2)
    }

    @Test
    fun testClaimSmsJob() = runBlocking {
        dao.insertLocation(Location(id = 1, name = "Loc", latitude = 0.0, longitude = 0.0, radiusMeters = 100f))
        val recId = dao.insertRecipient(Recipient(name = "Test", phoneNumber = "123", smsEnabled = true, callEnabled = false))
        val evtId = dao.insertEventWithJobs(
            Event(locationId = 1, transitionType = "ENTER", dedupeKey = "K1", status = "PROCESSING"),
            listOf(SmsJob(eventId = 0, recipientId = recId, renderedMessage = "Hi", status = "PENDING")),
            emptyList()
        )
        val sms = dao.getPendingSmsJobsSync().first()
        
        val rowsClaimed = dao.claimSmsJob(sms.id)
        assertEquals(1, rowsClaimed)
        
        val doubleClaim = dao.claimSmsJob(sms.id)
        assertEquals(0, doubleClaim)
    }
    
    @Test
    fun testStaleRecovery() = runBlocking {
        dao.insertLocation(Location(id = 1, name = "Loc", latitude = 0.0, longitude = 0.0, radiusMeters = 100f))
        val recId = dao.insertRecipient(Recipient(name = "Test", phoneNumber = "123", smsEnabled = true, callEnabled = false))
        val evtId = dao.insertEventWithJobs(
            Event(locationId = 1, transitionType = "ENTER", dedupeKey = "K2", status = "PROCESSING"),
            listOf(SmsJob(eventId = 0, recipientId = recId, renderedMessage = "Hi", status = "PENDING")),
            emptyList()
        )
        val sms = dao.getPendingSmsJobsSync().first()
        
        dao.claimSmsJob(sms.id, startedAt = System.currentTimeMillis() - 300000L)
        dao.recoverStaleSmsJobs(cutoff = System.currentTimeMillis() - 120000L)
        
        val recovered = dao.getPendingSmsJobsSync().first()
        assertEquals("RETRYING", recovered.status)
        assertEquals(1, recovered.attemptCount)
    }
}