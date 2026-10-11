package com.example.criminalintent

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CrimeDao {
    @Query("SELECT * FROM crimes ORDER BY dateMillis DESC, id ASC")
    fun observeAll(): Flow<List<Crime>>
    @Query("SELECT * FROM crimes WHERE id = :id")
    suspend fun get(id: String): Crime?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(crime: Crime)
    @Query("DELETE FROM crimes WHERE id = :id")
    suspend fun delete(id: String)
}
