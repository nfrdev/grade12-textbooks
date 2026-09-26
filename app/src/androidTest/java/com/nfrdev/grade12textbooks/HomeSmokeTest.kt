package com.nfrdev.grade12textbooks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Rule
import org.junit.Test

class HomeSmokeTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    @Test(timeout = 180_000) fun browseDownloadAndOpenOfflineBook() {
        composeRule.onNodeWithText("Natural Science").assertIsDisplayed()
        composeRule.onNodeWithText("Social Science").assertIsDisplayed()
        composeRule.onNodeWithText("Common subjects").assertIsDisplayed()
        composeRule.onNodeWithText("Natural Science").performClick()
        composeRule.waitUntil(30_000) {
            composeRule.onAllNodesWithText("Agriculture").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Agriculture").performClick()
        composeRule.waitUntil(30_000) {
            composeRule.onAllNodesWithText("Books").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Agriculture", useUnmergedTree = true).performTouchInput { click() }
        composeRule.waitUntil(30_000) {
            composeRule.onAllNodesWithText("Download").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Download").performClick()
        composeRule.waitUntil(120_000) {
            composeRule.onAllNodesWithText("Download complete").fetchSemanticsNodes().isNotEmpty()
        }
        repeat(3) {
            composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        }
        composeRule.onNodeWithText("My Books").performClick()
        composeRule.waitUntil(30_000) {
            composeRule.onAllNodesWithText("Agriculture").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Agriculture").assertIsDisplayed()
    }
}
