package com.nfrdev.grade12textbooks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.createComposeRule
import com.nfrdev.grade12textbooks.ui.home.HomeScreen
import com.nfrdev.grade12textbooks.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class HomeSmokeTest {
    @get:Rule val composeRule = createComposeRule()
    @Test fun homeRendersBothStreams() {
        composeRule.setContent { AppTheme { HomeScreen() } }
        composeRule.onNodeWithText("Natural Science").assertIsDisplayed()
        composeRule.onNodeWithText("Social Science").assertIsDisplayed()
    }
}
