package com.example.androidcrud.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.androidcrud.data.local.EntryEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import android.app.Application
import org.robolectric.annotation.Config
import java.time.Instant
import com.example.androidcrud.BuildConfig
import org.junit.Assume
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runners.model.Statement

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class DeleteConfirmationTest {

    val composeTestRule = createComposeRule()

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                Assume.assumeTrue("Skipping Robolectric Compose test in Release build", BuildConfig.DEBUG)
                base.evaluate()
            }
        }
    }).around(composeTestRule)

    private val viewModel = mockk<HomeViewModel>(relaxed = true)
    // Create a mock entry with a fixed timestamp
    private val fixedInstant = Instant.parse("2023-10-05T10:00:00Z")
    private val testEntry = EntryEntity(id = 1, timestamp = fixedInstant, entryValue = 123)
    
    private val uiStateFlow = MutableStateFlow<HomeUiState>(HomeUiState.Success(listOf(testEntry)))
    private val importExportStateFlow = MutableStateFlow<ImportExportState>(ImportExportState.Idle)

    @Test
    fun deleteEntry_showsConfirmationDialog_and_cancels() {
        every { viewModel.uiState } returns uiStateFlow
        every { viewModel.importExportState } returns importExportStateFlow

        composeTestRule.setContent {
            HomeScreen(
                onAddEntryClick = {},
                onEditEntryClick = {},
                viewModel = viewModel
            )
        }

        // Click Delete Icon on the list item
        composeTestRule.onNodeWithContentDescription("Delete Entry").performClick()
        
        // Dialog should appear
        composeTestRule.onNodeWithText("Confirm Deletion").assertIsDisplayed()
        
        // Click Cancel
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForIdle()
        
        // Dialog should be gone
        composeTestRule.onNodeWithText("Confirm Deletion").assertDoesNotExist()
        
        // Verify deleteEntry was NOT called
        verify(exactly = 0) { viewModel.deleteEntry(any()) }
    }

    @Test
    fun deleteEntry_showsConfirmationDialog_and_deletes() {
        every { viewModel.uiState } returns uiStateFlow
        every { viewModel.importExportState } returns importExportStateFlow

        composeTestRule.setContent {
            HomeScreen(
                onAddEntryClick = {},
                onEditEntryClick = {},
                viewModel = viewModel
            )
        }

        // Click Delete Icon
        composeTestRule.onNodeWithContentDescription("Delete Entry").performClick()
        
        // Click Delete (in Dialog)
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.waitForIdle()
        
        // Verify deleteEntry WAS called
        verify(exactly = 1) { viewModel.deleteEntry(testEntry) }
        
        // Dialog should be gone
        composeTestRule.onNodeWithText("Confirm Deletion").assertDoesNotExist()
    }
}
