package com.nfrdev.grade12textbooks.ui.navigation

import android.app.Activity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.nfrdev.grade12textbooks.R
import com.nfrdev.grade12textbooks.ui.bookmarks.BookmarksScreen
import com.nfrdev.grade12textbooks.ui.books.BookDetailsScreen
import com.nfrdev.grade12textbooks.ui.books.BookListScreen
import com.nfrdev.grade12textbooks.ui.downloads.DownloadsScreen
import com.nfrdev.grade12textbooks.ui.home.HomeScreen
import com.nfrdev.grade12textbooks.ui.library.LibraryScreen
import com.nfrdev.grade12textbooks.ui.reader.ReaderScreen
import com.nfrdev.grade12textbooks.ui.settings.SettingsScreen
import com.nfrdev.grade12textbooks.ui.subjects.SubjectsScreen

@Composable
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun AppNavHost() {
    val navController = rememberNavController()
    val snackbarHost = remember { SnackbarHostState() }
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val topLevelRoutes = setOf("home", "downloads", "library", "settings")
    val activity = LocalContext.current as Activity
    val windowWidthClass = calculateWindowSizeClass(activity).widthSizeClass
    val useNavigationRail = windowWidthClass != WindowWidthSizeClass.Compact
    val destinations = listOf(
        NavigationDestination("home", stringResource(R.string.home), Icons.Default.Home),
        NavigationDestination("downloads", stringResource(R.string.downloads), Icons.Default.Download),
        NavigationDestination("library", stringResource(R.string.library), Icons.Default.LocalLibrary),
        NavigationDestination("settings", stringResource(R.string.settings), Icons.Default.Settings)
    )

    fun navigateTo(destination: String) {
        if (route != destination) {
            navController.navigate(destination) {
                popUpTo("home") { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    CompositionLocalProvider(LocalAppSnackbarHost provides snackbarHost) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHost) },
            bottomBar = {
                if (route in topLevelRoutes && !useNavigationRail) {
                    NavigationBar {
                        destinations.forEach { destination ->
                            NavigationBarItem(
                                selected = route == destination.route,
                                onClick = { navigateTo(destination.route) },
                                icon = { Icon(destination.icon, contentDescription = null) },
                                label = { Text(destination.label) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (route in topLevelRoutes && useNavigationRail) {
                    NavigationRail {
                        destinations.forEach { destination ->
                            NavigationRailItem(
                                selected = route == destination.route,
                                onClick = { navigateTo(destination.route) },
                                icon = { Icon(destination.icon, contentDescription = null) },
                                label = { Text(destination.label) },
                                alwaysShowLabel = true
                            )
                        }
                    }
                }
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    composable("home") { HomeScreen { navController.navigate("subjects/$it") } }
                    composable("subjects/{stream}") { SubjectsScreen(navController) }
                    composable("books/{stream}/{subjectId}") { BookListScreen(navController) }
                    composable("book/{bookId}") { BookDetailsScreen(navController) }
                    composable("downloads") {
                        DownloadsScreen(
                            onBrowseSubjects = { navController.navigate("home") },
                            onOpenBook = { id -> navController.navigate("book/$id") }
                        )
                    }
                    composable("library") {
                        LibraryScreen(
                            onBrowseSubjects = { navController.navigate("home") },
                            onOpenBook = { id -> navController.navigate("reader/$id") }
                        )
                    }
                    composable("settings") { SettingsScreen() }
                    composable(
                        route = "reader/{bookId}?page={page}",
                        arguments = listOf(navArgument("page") { type = NavType.IntType; defaultValue = -1 })
                    ) { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getString("bookId").orEmpty()
                        ReaderScreen(onShowBookmarks = { navController.navigate("bookmarks/$bookId") })
                    }
                    composable("bookmarks/{bookId}") { backStackEntry ->
                        val bookId = backStackEntry.arguments?.getString("bookId").orEmpty()
                        BookmarksScreen(onOpenPage = { page ->
                            val readerRoute = page?.let { "reader/$bookId?page=$it" } ?: "reader/$bookId"
                            navController.navigate(readerRoute)
                        })
                    }
                }
            }
        }
    }
}

private data class NavigationDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
