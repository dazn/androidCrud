package com.example.androidcrud.ui.screens.home

import android.net.Uri
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityOptionsCompat
import com.example.androidcrud.R
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

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class ImportExportUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<HomeViewModel>(relaxed = true)
    private val uiStateFlow = MutableStateFlow<HomeUiState>(HomeUiState.Success(emptyList()))
    private val importExportStateFlow = MutableStateFlow<ImportExportState>(ImportExportState.Idle)

    @Test
    fun importData_showsConfirmationDialog_and_cancels() {
        every { viewModel.uiState } returns uiStateFlow
        every { viewModel.importExportState } returns importExportStateFlow

        // Custom Registry to capture the launch and simulate result
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I : Any?, O : Any?> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                // Simulate selecting a file immediately
                dispatchResult(requestCode, Uri.parse("content://test/backup.json"))
            }
        }
        
        val owner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry = testRegistry
        }

        composeTestRule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                HomeScreen(
                    onAddEntryClick = {},
                    onEditEntryClick = {},
                    viewModel = viewModel
                )
            }
        }

        // Open Menu
        composeTestRule.onNodeWithContentDescription("More options").performClick()
        
        // Click Import
        composeTestRule.onNodeWithText("Import Data").performClick()
        
        // Dialog should appear
        composeTestRule.onNodeWithText("Confirm Import").assertIsDisplayed()
        composeTestRule.onNodeWithText("Restoration from backup file replaces all existing data. All existing data will be lost!").assertIsDisplayed()
        
        // Click Cancel
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.waitForIdle()
        
        // Dialog should be gone
        composeTestRule.onNodeWithText("Restoration from backup file replaces all existing data. All existing data will be lost!").assertDoesNotExist()
        
        // Verify importData was NOT called
        verify(exactly = 0) { viewModel.importData(any()) }
    }

    @Test
    fun importData_showsConfirmationDialog_and_continues() {
        every { viewModel.uiState } returns uiStateFlow
        every { viewModel.importExportState } returns importExportStateFlow

        val testUri = Uri.parse("content://test/backup.json")
        val testRegistry = object : ActivityResultRegistry() {
            override fun <I : Any?, O : Any?> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(requestCode, testUri)
            }
        }
        
        val owner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry = testRegistry
        }

        composeTestRule.setContent {
             CompositionLocalProvider(LocalActivityResultRegistryOwner provides owner) {
                HomeScreen(
                    onAddEntryClick = {},
                    onEditEntryClick = {},
                    viewModel = viewModel
                )
             }
        }

        // Open Menu and Click Import
        composeTestRule.onNodeWithContentDescription("More options").performClick()
        composeTestRule.onNodeWithText("Import Data").performClick()

        // Click Continue
        composeTestRule.onNodeWithText("Continue").performClick()
        composeTestRule.waitForIdle()
        
        // Verify importData WAS called with correct URI
        verify(exactly = 1) { viewModel.importData(testUri) }
        
        // Dialog should be gone
        composeTestRule.onNodeWithText("Restoration from backup file replaces all existing data. All existing data will be lost!").assertDoesNotExist()
    }
}
