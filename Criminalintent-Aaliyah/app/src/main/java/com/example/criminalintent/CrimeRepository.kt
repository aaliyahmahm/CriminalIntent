package com.example.criminalintent

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CrimeRepository(private val dao: CrimeDao, private val context: Context) {
    val crimes = dao.observeAll()
    suspend fun get(id: String) = dao.get(id)
    suspend fun save(crime: Crime) = withContext(Dispatchers.IO) {
        val old = dao.get(crime.id)
        dao.save(crime)
        if (old?.photoName != crime.photoName) removePhoto(old?.photoName)
    }
    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        val old = dao.get(id)
        dao.delete(id)
        removePhoto(old?.photoName)
    }
    private fun removePhoto(name: String?) {
        if (name != null) runCatching { File(context.filesDir, "photos/$name").delete() }
    }
}
