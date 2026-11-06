package one.muisnowdevs.apps.ndhucalendar

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val STORAGE_NAME = "favorites"
private const val STORAGE_KEY = "favorites"

/** Keys of the events the user has starred, backed by shared preferences. */
class EventFavorite private constructor(context: Context) {
    private val storage = context.applicationContext
        .getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE)

    private val _favorites =
        MutableStateFlow(storage.getStringSet(STORAGE_KEY, emptySet())?.toSet() ?: emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    fun setFavorite(key: String, favorite: Boolean) {
        val updated = if (favorite) _favorites.value + key else _favorites.value - key
        if (updated == _favorites.value) return

        _favorites.value = updated
        storage.edit { putStringSet(STORAGE_KEY, updated) }
    }

    companion object {
        @Volatile
        private var instance: EventFavorite? = null

        fun getInstance(context: Context): EventFavorite =
            instance ?: synchronized(this) {
                instance ?: EventFavorite(context).also { instance = it }
            }
    }
}
