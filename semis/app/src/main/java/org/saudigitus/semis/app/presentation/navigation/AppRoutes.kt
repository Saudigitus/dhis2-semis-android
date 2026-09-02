package org.saudigitus.semis.app.presentation.navigation

object AppRoutes {
    const val TRACKER_LIST = "tei"
    const val ATTENDANCE = "attendance"
    const val ENROLLMENT = "enrollment"
    const val ENROLLMENT_FORM = "enrollment/form"
    const val ABSENTEEISM = "absenteeism"
    const val PERFORMANCE = "performance"
    const val TRANSFER = "transfer"
    const val HOME = "home"

    /** Learner dashboard, opened from the tracker listing with the record uid. */
    const val STUDENT_PROFILE = "student/profile"
    const val STUDENT_PROFILE_ARG_TEI = "teiUid"
    const val STUDENT_PROFILE_ROUTE = "student/profile/{teiUid}"

    fun studentProfile(teiUid: String) = "student/profile/" + teiUid
}
