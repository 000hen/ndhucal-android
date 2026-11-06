package one.muisnowdevs.apps.ndhucalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import one.muisnowdevs.apps.ndhucalendar.model.CalendarEntry
import one.muisnowdevs.apps.ndhucalendar.model.toEntry
import one.muisnowdevs.apps.ndhucalendar.source.CalendarClient

class CalendarViewModel : ViewModel() {
    private val _entries = MutableStateFlow<List<CalendarEntry>>(emptyList())
    val entries: StateFlow<List<CalendarEntry>> = _entries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    private val client = CalendarClient()
    private var loadJob: Job? = null

    init {
        reload()
    }

    fun reload() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { load() }
    }

    private suspend fun load() {
        _isLoading.value = true
        _error.value = null

        try {
            // Parsing runs on the caller's thread, so keep it off the main one.
            _entries.value = withContext(Dispatchers.Default) {
                // Lazy list keys must be unique; an exact repeat on the page is the same event.
                client.get().map { it.toEntry() }.distinctBy { it.key }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            // A failed refresh keeps the last good list; the screen only swaps to the error when
            // there is nothing to fall back on.
            _error.value = failure
        } finally {
            if (currentCoroutineContext().isActive) _isLoading.value = false
        }
    }
}
