package one.muisnowdevs.apps.ndhucalendar.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import one.muisnowdevs.apps.ndhucalendar.ui.theme.FavoriteStar

/** Switches the list between every event and only the starred ones. */
@Composable
fun FavoriteFilterButton(
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = { onEnabledChange(!isEnabled) }, modifier = modifier) {
        AnimatedContent(
            targetState = isEnabled,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.6f)) togetherWith
                        (fadeOut() + scaleOut(targetScale = 0.6f))
            },
            label = "favorite filter icon"
        ) { enabled ->
            Icon(
                imageVector = if (enabled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = if (enabled) "顯示全部" else "只顯示收藏",
                tint = if (enabled) FavoriteStar else LocalContentColor.current
            )
        }
    }
}
