package com.example.criminalintent

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CrimeDatabaseTest {
    @Test fun recordsSurviveReopenAndCanBeUpdatedAndDeleted() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "test-${UUID.randomUUID()}.db"
        fun open() = Room.databaseBuilder(context, CrimeDatabase::class.java, name).build()
        var database = open()
        try {
            val crime = Crime(title = "Empty coffee pot", dateMillis = 123456789L,
                solved = false, photoName = "test.jpg")
            database.crimeDao().save(crime)
            database.close()
            database = open()
            assertEquals(crime, database.crimeDao().get(crime.id))
            val solved = crime.copy(title = "Coffee refilled", solved = true)
            database.crimeDao().save(solved)
            assertEquals(listOf(solved), database.crimeDao().observeAll().first())
            database.crimeDao().delete(crime.id)
            assertNull(database.crimeDao().get(crime.id))
            assertTrue(database.crimeDao().observeAll().first().isEmpty())
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
