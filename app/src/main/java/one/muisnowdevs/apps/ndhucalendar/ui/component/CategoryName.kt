package one.muisnowdevs.apps.ndhucalendar.ui.component

import androidx.annotation.StringRes
import one.muisnowdevs.apps.ndhucalendar.R
import uniffi.ndhucal.Category
import uniffi.ndhucal.Subcategory

@get:StringRes
val Category.nameRes: Int
    get() = when (this) {
        Category.SEMESTER -> R.string.category_semester
        Category.ENROLLMENT -> R.string.category_enrollment
        Category.ASSESSMENT -> R.string.category_assessment
        Category.REGISTRATION -> R.string.category_registration
        Category.ACADEMIC -> R.string.category_academic
        Category.GRADUATION -> R.string.category_graduation
        Category.HOLIDAY -> R.string.category_holiday
        Category.VACATION -> R.string.category_vacation
        Category.STUDENT_AFFAIRS -> R.string.category_student_affairs
        Category.CAMPUS -> R.string.category_campus
        Category.OTHER -> R.string.category_other
    }

@get:StringRes
val Subcategory.nameRes: Int
    get() = when (this) {
        Subcategory.SEMESTER_START -> R.string.subcategory_semester_start
        Subcategory.CLASSES_BEGIN -> R.string.subcategory_classes_begin
        Subcategory.SEMESTER_END -> R.string.subcategory_semester_end
        Subcategory.COURSE_SELECTION -> R.string.subcategory_course_selection
        Subcategory.ADD_DROP -> R.string.subcategory_add_drop
        Subcategory.DROP -> R.string.subcategory_drop
        Subcategory.ADD_BY_SIGNATURE -> R.string.subcategory_add_by_signature
        Subcategory.COURSE_WITHDRAW -> R.string.subcategory_course_withdraw
        Subcategory.CREDIT_FEE -> R.string.subcategory_credit_fee
        Subcategory.MIDTERM -> R.string.subcategory_midterm_exam
        Subcategory.FINAL -> R.string.subcategory_final_exam
        Subcategory.DEGREE_EXAM -> R.string.subcategory_degree_exam
        Subcategory.MAKEUP -> R.string.subcategory_makeup_exam
        Subcategory.OTHER_EXAM -> R.string.subcategory_exam
        Subcategory.REGISTRATION -> R.string.subcategory_registration
        Subcategory.TUITION -> R.string.subcategory_tuition_payment
        Subcategory.FEE_WAIVER -> R.string.subcategory_fee_waiver
        Subcategory.LOAN -> R.string.subcategory_student_loan
        Subcategory.FINANCIAL_AID -> R.string.subcategory_financial_aid
        Subcategory.CREDIT_TRANSFER -> R.string.subcategory_credit_transfer
        Subcategory.DEPT_TRANSFER -> R.string.subcategory_dept_transfer
        Subcategory.DUAL_DEGREE -> R.string.subcategory_dual_degree
        Subcategory.GRADES -> R.string.subcategory_grades
        Subcategory.TEACHING_SURVEY -> R.string.subcategory_teaching_survey
        Subcategory.MIDTERM_WARNING -> R.string.subcategory_midterm_warning
        Subcategory.SELF_STUDY -> R.string.subcategory_self_study
        Subcategory.WITHDRAWAL -> R.string.subcategory_withdrawal
        Subcategory.GRADUATION_CEREMONY -> R.string.subcategory_graduation_ceremony
        Subcategory.EARLY_GRADUATION -> R.string.subcategory_early_graduation
        Subcategory.DEPARTURE -> R.string.subcategory_departure
        Subcategory.PUBLIC_HOLIDAY -> R.string.subcategory_public_holiday
        Subcategory.SUBSTITUTE_DAY -> R.string.subcategory_substitute_day
        Subcategory.WINTER_BREAK -> R.string.subcategory_winter_break
        Subcategory.SPRING_BREAK -> R.string.subcategory_spring_break
        Subcategory.SUMMER_BREAK -> R.string.subcategory_summer_break
        Subcategory.DORMITORY -> R.string.subcategory_dormitory
        Subcategory.HEALTH -> R.string.subcategory_health_check
        Subcategory.ORIENTATION -> R.string.subcategory_orientation
        Subcategory.MILITARY -> R.string.subcategory_military_service
        Subcategory.CONDUCT -> R.string.subcategory_conduct
        Subcategory.ANNIVERSARY -> R.string.subcategory_anniversary
        Subcategory.SPORTS_DAY -> R.string.subcategory_sports_day
        Subcategory.DRILL -> R.string.subcategory_drill
        Subcategory.ELECTION -> R.string.subcategory_election
        Subcategory.MAINTENANCE -> R.string.subcategory_maintenance
        Subcategory.ADMISSION -> R.string.subcategory_admission
        Subcategory.OTHER -> R.string.subcategory_other
    }
