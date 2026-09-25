package com.nfrdev.grade12textbooks.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nfrdev.grade12textbooks.ui.home.HomeScreen
import com.nfrdev.grade12textbooks.ui.subjects.SubjectsScreen
import com.nfrdev.grade12textbooks.ui.books.BookListScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val snackbarHost = remember { SnackbarHostState() }
    CompositionLocalProvider(LocalAppSnackbarHost provides snackbarHost) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { HomeScreen { navController.navigate("subjects/$it") } }
                composable("subjects/{stream}") { SubjectsScreen(navController) }
                composable("books/{stream}/{subjectId}") { BookListScreen(navController) }
            }
        }
    }
}
