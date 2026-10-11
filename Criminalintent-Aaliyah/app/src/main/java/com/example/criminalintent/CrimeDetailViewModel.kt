package com.example.criminalintent

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class CrimeDetailViewModel(app: Application, private val state: SavedStateHandle) : AndroidViewModel(app) {
    private val repository = (app as CrimeApplication).repository
    val ready = state.getLiveData("ready", false)
    val busy = MutableLiveData(false)
    val error = MutableLiveData<Int?>()
    val finished = MutableLiveData(false)
    private var original: Crime? = null
    private var loading = false
    var pendingPhoto: String?
        get() = state["pendingPhoto"]
        set(value) { state["pendingPhoto"] = value }
    val title: String get() = state["title"] ?: ""
    val dateMillis: Long get() = state["date"] ?: System.currentTimeMillis()
    val solved: Boolean get() = state["solved"] ?: false
    val photoName: String? get() = state["photo"]

    fun load(id: String) {
        if (loading || original != null) return
        loading = true
        viewModelScope.launch {
            try {
                val record = repository.get(id) ?: if (state.get<Boolean>("isNew") == true) Crime(id = id)
                    else { error.value = R.string.missing_record; finished.value = true; return@launch }
                original = record
                state["id"] = id
                if (ready.value != true) {
                    setTitle(record.title); state["date"] = record.dateMillis
                    state["solved"] = record.solved; state["photo"] = record.photoName
                    state["ready"] = true
                }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { error.value = R.string.load_failed }
            finally { loading = false }
        }
    }
    fun setTitle(value: String) { state["title"] = value }
    fun setSolved(value: Boolean) { state["solved"] = value }
    fun setDate(value: Long) { state["date"] = value }
    fun setPhoto(value: String) { state["photo"] = value }
    fun draft() = Crime(state.get<String>("id") ?: error("Crime not loaded"), title, dateMillis, solved, photoName)
    fun hasChanges(): Boolean = ready.value == true && (original == null || draft() != original)
    fun save() {
        if (busy.value == true || ready.value != true) return
        busy.value = true
        val record = draft().copy(title = title.trim())
        viewModelScope.launch {
            try { repository.save(record); original = record; finished.value = true }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { error.value = R.string.save_failed }
            finally { busy.value = false }
        }
    }
    fun delete() {
        if (busy.value == true || ready.value != true) return
        busy.value = true
        viewModelScope.launch {
            try { repository.delete(draft().id); finished.value = true }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { error.value = R.string.save_failed }
            finally { busy.value = false }
        }
    }
}
