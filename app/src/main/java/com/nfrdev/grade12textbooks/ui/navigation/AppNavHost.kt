package com.nfrdev.grade12textbooks.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
fun AppNavHost() {
    val navController = rememberNavController()
    val snackbarHost = remember { SnackbarHostState() }
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val bottom = listOf("home", "downloads", "library", "settings")

    CompositionLocalProvider(LocalAppSnackbarHost provides snackbarHost) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHost) },
            bottomBar = {
                if (route in bottom) {
                    NavigationBar {
                        bottom.forEach { destination ->
                            val label = when (destination) {
                                "home" -> stringResource(R.string.app_name)
                                "downloads" -> stringResource(R.string.downloads)
                                "library" -> stringResource(R.string.library)
                                else -> stringResource(R.string.settings)
                            }
                            val icon = when (destination) {
                                "home" -> Icons.Default.Home
                                "downloads" -> Icons.Default.Download
                                "library" -> Icons.Default.LocalLibrary
                                else -> Icons.Default.Settings
                            }
                            NavigationBarItem(
                                selected = route == destination,
                                onClick = {
                                    if (route != destination) {
                                        navController.navigate(destination) {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(imageVector = icon, contentDescription = label) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("home") { HomeScreen { navController.navigate("subjects/$it") } }
                composable("subjects/{stream}") { SubjectsScreen(navController) }
                composable("books/{stream}/{subjectId}") { BookListScreen(navController) }
                composable("book/{bookId}") { BookDetailsScreen(navController) }
                composable("downloads") { DownloadsScreen() }
                composable("library") { LibraryScreen() }
                composable("settings") { SettingsScreen() }
                composable("reader/{bookId}") { ReaderScreen() }
                composable("bookmarks/{bookId}") { BookmarksScreen() }
            }
        }
    }
}
