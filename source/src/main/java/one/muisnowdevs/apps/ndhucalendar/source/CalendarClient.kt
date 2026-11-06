package one.muisnowdevs.apps.ndhucalendar.source

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import uniffi.ndhucal.Event
import uniffi.ndhucal.calendarUrl
import uniffi.ndhucal.parseCalendar
import java.io.IOException

class CalendarClient(private val client: OkHttpClient = OkHttpClient()) {
    private fun getCalendarURL(): Uri = calendarUrl().toUri()
    private suspend fun fetch(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code} from $url")
                response.body.string()
            }
        }
    }

    suspend fun get(): List<Event> {
        val response = fetch(getCalendarURL().toString())
        return parseCalendar(response)
    }
}