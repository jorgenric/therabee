package com.therapycompanion.ui.navigation

/**
 * All navigation routes defined as typed constants.
 * Never use raw strings for navigation — always use these objects.
 */
sealed class Screen(val route: String) {
    object Today : Screen("today")
    object Library : Screen("library")
    object Progress : Screen("progress")
    object Settings : Screen("settings")

    // Parameterized routes
    object Session : Screen("session/{exerciseId}") {
        fun route(exerciseId: String) = "session/$exerciseId"
    }
    object ExerciseDetail : Screen("exercise/{exerciseId}") {
        fun route(exerciseId: String) = "exercise/$exerciseId"
    }
    object ExerciseEdit : Screen("exercise/{exerciseId}/edit") {
        fun route(exerciseId: String) = "exercise/$exerciseId/edit"
    }
    object ExerciseNew : Screen("exercise/new")
    object Import : Screen("import")

    companion object {
        /** The four bottom-nav destinations */
        val bottomNavDestinations = listOf(Today, Library, Progress, Settings)
    }
}

/** Intent extra key used to deep-link from notification tap to a specific screen */
const val EXTRA_NAVIGATE_TO = "navigate_to"
const val NAVIGATE_TO_TODAY = "today"
