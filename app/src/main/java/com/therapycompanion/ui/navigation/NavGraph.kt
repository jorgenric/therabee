package com.therapycompanion.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.therapycompanion.R
import com.therapycompanion.ui.home.HomeScreen
import com.therapycompanion.ui.library.ExerciseDetailScreen
import com.therapycompanion.ui.library.ExerciseEditScreen
import com.therapycompanion.ui.library.LibraryScreen
import com.therapycompanion.ui.progress.ProgressScreen
import com.therapycompanion.ui.session.SessionScreen
import com.therapycompanion.ui.settings.ImportScreen
import com.therapycompanion.ui.settings.SettingsScreen

private data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: ImageVector,
    val contentDescriptionRes: Int
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Today, R.string.nav_today, Icons.Filled.Home, R.string.nav_today),
    BottomNavItem(Screen.Library, R.string.nav_library, Icons.Filled.FitnessCenter, R.string.nav_library),
    BottomNavItem(Screen.Progress, R.string.nav_progress, Icons.Filled.CalendarMonth, R.string.nav_progress),
    BottomNavItem(Screen.Settings, R.string.nav_settings, Icons.Filled.Settings, R.string.nav_settings)
)

@Composable
fun TherapyCompanionNavGraph(startDestination: String? = null) {
    val navController = rememberNavController()
    val initialRoute = startDestination ?: Screen.Today.route

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Show bottom nav only on top-level destinations
            val showBottomNav = bottomNavItems.any {
                currentDestination?.hierarchy?.any { dest -> dest.route == it.screen.route } == true
            }

            if (showBottomNav) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = stringResource(item.contentDescriptionRes)
                                )
                            },
                            label = { Text(stringResource(item.labelRes)) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute
        ) {
            composable(Screen.Today.route) {
                HomeScreen(
                    contentPadding = innerPadding,
                    onStartSession = { exerciseId ->
                        navController.navigate(Screen.Session.route(exerciseId))
                    }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    contentPadding = innerPadding,
                    onExerciseClick = { exerciseId ->
                        navController.navigate(Screen.ExerciseDetail.route(exerciseId))
                    },
                    onAddExercise = {
                        navController.navigate(Screen.ExerciseNew.route)
                    }
                )
            }

            composable(Screen.Progress.route) {
                ProgressScreen(contentPadding = innerPadding)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    contentPadding = innerPadding,
                    onImportClick = {
                        navController.navigate(Screen.Import.route)
                    }
                )
            }

            composable(
                route = Screen.Session.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
                SessionScreen(
                    exerciseId = exerciseId,
                    onDone = {
                        navController.popBackStack(Screen.Today.route, inclusive = false)
                    },
                    onSkip = {
                        navController.popBackStack(Screen.Today.route, inclusive = false)
                    }
                )
            }

            composable(
                route = Screen.ExerciseDetail.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
                ExerciseDetailScreen(
                    exerciseId = exerciseId,
                    onEdit = { navController.navigate(Screen.ExerciseEdit.route(exerciseId)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ExerciseEdit.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
                ExerciseEditScreen(
                    exerciseId = exerciseId,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ExerciseNew.route) {
                ExerciseEditScreen(
                    exerciseId = null,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Import.route) {
                ImportScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
