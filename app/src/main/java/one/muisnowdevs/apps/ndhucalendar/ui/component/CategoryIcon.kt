package one.muisnowdevs.apps.ndhucalendar.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import uniffi.ndhucal.Category

val Category.icon: ImageVector
    get() = when (this) {
        Category.SEMESTER -> Icons.Rounded.School
        Category.ENROLLMENT -> Icons.Rounded.Checklist
        Category.ASSESSMENT -> Icons.Rounded.Quiz
        Category.REGISTRATION -> Icons.Rounded.Payments
        Category.ACADEMIC -> Icons.Rounded.AutoStories
        Category.GRADUATION -> Icons.Rounded.WorkspacePremium
        Category.HOLIDAY -> Icons.Rounded.Celebration
        Category.VACATION -> Icons.Rounded.BeachAccess
        Category.STUDENT_AFFAIRS -> Icons.Rounded.Groups
        Category.CAMPUS -> Icons.Rounded.AccountBalance
        Category.OTHER -> Icons.Rounded.Event
    }
