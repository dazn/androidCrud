package com.example.androidcrud

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class E2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun addEntry_appearsInList() {
        // Wait for list to load (handle empty or existing)
        // Since we are adding, we don't care about initial state much, 
        // as long as we can click FAB.
        
        // Click Add FAB
        composeTestRule.onNodeWithContentDescription("Add Entry").performClick()

        // Enter value
        composeTestRule.onNodeWithText("Entry Value (Positive Integer)").performTextInput("999")

        // Save
        composeTestRule.onNodeWithText("Save Entry").performClick()

        // Verify we are back on Home and item exists
        composeTestRule.onNodeWithText("Value: 999").assertIsDisplayed()
    }
}
