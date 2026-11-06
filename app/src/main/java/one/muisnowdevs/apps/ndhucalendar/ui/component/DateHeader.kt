package one.muisnowdevs.apps.ndhucalendar.ui.component

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import one.muisnowdevs.apps.ndhucalendar.model.relativeDayText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SameYearFormat = DateTimeFormatter.ofPattern("M月d日（E）", Locale.TRADITIONAL_CHINESE)
private val OtherYearFormat = DateTimeFormatter.ofPattern("y年M月d日（E）", Locale.TRADITIONAL_CHINESE)

/**
 * Centred date label for a section of the list, with how far it is from [today] beside it.
 * While [isFloating] it takes the primary container colours so it stays readable over the rows.
 */
@Composable
fun DateHeader(
    date: LocalDate,
    today: LocalDate,
    isFloating: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val transition = updateTransition(isFloating, label = "date header")

    val containerColor by transition.animateColor(label = "container") { floating ->
        if (floating) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface
    }
    val contentColor by transition.animateColor(label = "content") { floating ->
        if (floating) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface
    }

    val dateText = remember(date, today) {
        (if (date.year == today.year) SameYearFormat else OtherYearFormat).format(date)
    }
    val relativeText = remember(date, today) { relativeDayText(date, today) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(containerColor)
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = dateText, color = contentColor)
            Text(
                text = relativeText,
                color = if (isFloating) contentColor else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
