package one.muisnowdevs.apps.ndhucalendar.model

import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.AbsoluteUnit
import android.icu.text.RelativeDateTimeFormatter.Direction
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit
import uniffi.ndhucal.CalendarDate
import uniffi.ndhucal.Category
import uniffi.ndhucal.Event
import uniffi.ndhucal.Subcategory
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

enum class EventStatus { Upcoming, Ongoing, Ended }

data class CalendarEntry(
    val key: String,
    val title: String,
    val start: LocalDate,
    val end: LocalDate,
    val category: Category,
    val categoryName: String,
    val subcategoryName: String?,
    val isImportant: Boolean,
    val source: String,
) {
    val dayCount: Long get() = ChronoUnit.DAYS.between(start, end) + 1

    fun statusOn(today: LocalDate): EventStatus = when {
        today < start -> EventStatus.Upcoming
        today > end -> EventStatus.Ended
        else -> EventStatus.Ongoing
    }
}

/** Holidays and breaks: the days with no classes. */
val CalendarEntry.isDayOff: Boolean
    get() = category == Category.HOLIDAY || category == Category.VACATION

fun CalendarDate.toLocalDate(): LocalDate = LocalDate.of(year, month.toInt(), day.toInt())
fun Event.toEntry(): CalendarEntry {
    val startDate = start.toLocalDate()
    val endDate = end.toLocalDate()

    return CalendarEntry(
        key = "$startDate/$endDate/$source/$title",
        title = title,
        start = startDate,
        end = endDate,
        category = category,
        categoryName = category.zhName(),
        subcategoryName = subcategory.takeIf { it != Subcategory.OTHER }?.zhName(),
        isImportant = subcategory.isImportant(),
        source = source,
    )
}

fun relativeDayText(date: LocalDate, today: LocalDate): String {
    // Pinned to the UI's language, since the status line appends Chinese text to this.
    val formatter = RelativeDateTimeFormatter.getInstance(Locale.TRADITIONAL_CHINESE)
    val days = ChronoUnit.DAYS.between(today, date)

    val namedDay = when (days) {
        0L -> Direction.THIS
        1L -> Direction.NEXT
        -1L -> Direction.LAST
        2L -> Direction.NEXT_2
        -2L -> Direction.LAST_2
        else -> null
    }
    if (namedDay != null) return formatter.format(namedDay, AbsoluteUnit.DAY)

    val distance = abs(days)
    val (earlier, later) = if (days > 0) today to date else date to today
    val (quantity, unit) = when {
        distance < 7 -> distance to RelativeUnit.DAYS
        distance < 30 -> distance / 7 to RelativeUnit.WEEKS
        distance < 365 -> ChronoUnit.MONTHS.between(earlier, later)
            .coerceAtLeast(1) to RelativeUnit.MONTHS

        else -> ChronoUnit.YEARS.between(earlier, later).coerceAtLeast(1) to RelativeUnit.YEARS
    }

    return formatter.format(
        quantity.toDouble(),
        if (days > 0) Direction.NEXT else Direction.LAST,
        unit
    )
}

fun statusText(entry: CalendarEntry, today: LocalDate): String = when (entry.statusOn(today)) {
    EventStatus.Upcoming -> "${relativeDayText(entry.start, today)}開始"
    EventStatus.Ongoing ->
        if (entry.end == today) "進行中，今天結束"
        else "進行中，${relativeDayText(entry.end, today)}結束"

    EventStatus.Ended -> "${relativeDayText(entry.end, today)}結束"
}
