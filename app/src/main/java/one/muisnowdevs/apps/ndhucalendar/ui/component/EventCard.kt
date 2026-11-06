package one.muisnowdevs.apps.ndhucalendar.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import one.muisnowdevs.apps.ndhucalendar.model.CalendarEntry
import one.muisnowdevs.apps.ndhucalendar.model.EventStatus
import one.muisnowdevs.apps.ndhucalendar.model.isDayOff
import one.muisnowdevs.apps.ndhucalendar.ui.theme.FavoriteStar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ShortDate = DateTimeFormatter.ofPattern("M/d（E）", Locale.TRADITIONAL_CHINESE)

/**
 * A calendar row: category badge, dates and title. Starring is the caller's swipe and the details
 * are the caller's sheet; the card only shows the star as a tag beside the date.
 *
 * Days off take the tertiary container colour and ongoing events the secondary one; ended ones of
 * either kind fade back.
 */
@Composable
fun EventCard(
    entry: CalendarEntry,
    today: LocalDate,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val status = entry.statusOn(today)
    val isHighlightedDayOff = entry.isDayOff && status != EventStatus.Ended

    val containerColor by animateColorAsState(
        when {
            status == EventStatus.Ended -> MaterialTheme.colorScheme.surfaceContainerLow
            entry.isDayOff -> MaterialTheme.colorScheme.tertiaryContainer
            status == EventStatus.Ongoing -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        label = "event card container"
    )

    val dateText = remember(entry) {
        if (entry.dayCount == 1L) ShortDate.format(entry.start)
        else "${ShortDate.format(entry.start)} – ${ShortDate.format(entry.end)}"
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CategoryBadge(
                entry = entry,
                modifier = Modifier.alpha(if (status == EventStatus.Ended) 0.6f else 1f)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isHighlightedDayOff) MaterialTheme.colorScheme.onTertiaryContainer
                        else MaterialTheme.colorScheme.primary
                    )
                    if (entry.isImportant) ImportantIcon()
                    AnimatedVisibility(visible = isFavorite) { FavoriteTag() }
                }

                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (status == EventStatus.Ended) MaterialTheme.colorScheme.onSurfaceVariant
                    else LocalContentColor.current
                )
            }
        }
    }
}

@Composable
fun CategoryBadge(entry: CalendarEntry, modifier: Modifier = Modifier) {
    // Inverted for days off: a tertiary-container badge would vanish into the tertiary-container card.
    val (containerColor, contentColor) =
        if (entry.isDayOff) MaterialTheme.colorScheme.onTertiaryContainer to MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = modifier
            .size(52.dp)
            .background(containerColor, MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            entry.category.icon,
            contentDescription = entry.categoryName,
            tint = contentColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun ImportantIcon() {
    Icon(
        Icons.Rounded.PriorityHigh,
        contentDescription = "重要",
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun FavoriteTag() {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.extraLarge)
            .padding(start = 4.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Star,
            contentDescription = null,
            tint = FavoriteStar,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "已收藏",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
