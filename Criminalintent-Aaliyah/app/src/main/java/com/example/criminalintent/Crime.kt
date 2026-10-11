package com.example.criminalintent

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "crimes")
data class Crime(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val solved: Boolean = false,
    val photoName: String? = null
)
