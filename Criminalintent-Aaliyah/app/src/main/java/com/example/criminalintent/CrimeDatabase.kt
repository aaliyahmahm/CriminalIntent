package com.example.criminalintent

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Crime::class], version = 1, exportSchema = true)
abstract class CrimeDatabase : RoomDatabase() {
    abstract fun crimeDao(): CrimeDao
    companion object {
        @Volatile private var instance: CrimeDatabase? = null
        fun get(context: Context): CrimeDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext,
                CrimeDatabase::class.java, "crimes.db").build().also { instance = it }
        }
    }
}
