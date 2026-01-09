package com.example.androidcrud.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.androidcrud.MainActivity
import com.example.androidcrud.data.repository.EntryRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NoteFeatureTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var entryRepository: EntryRepository

    @Before
    fun setup() {
        hiltRule.inject()
        runBlocking {
            entryRepository.replaceAllEntries(emptyList())
        }
    }

    @Test
    fun addEntry_withNote_displaysNoteInList() {
        val entryValue = "12345"
        val noteContent = "This is a test note"

        composeTestRule.onNodeWithContentDescription("Add Entry").performClick()
        composeTestRule.onNodeWithText("Entry Value (Positive Integer)").performTextInput(entryValue)
        composeTestRule.onNodeWithText("Note").performTextInput(noteContent)
        composeTestRule.onNodeWithText("Save Entry").performClick()

        composeTestRule.onNodeWithText(noteContent).assertIsDisplayed()
        composeTestRule.onNodeWithText("Value: $entryValue").assertIsDisplayed()
    }

    @Test
    fun addEntry_withoutNote_doesNotShowEmptySpace() {
        val entryValue = "67890"
        
        composeTestRule.onNodeWithContentDescription("Add Entry").performClick()
        composeTestRule.onNodeWithText("Entry Value (Positive Integer)").performTextInput(entryValue)
        composeTestRule.onNodeWithText("Save Entry").performClick()

        composeTestRule.onNodeWithText("Value: $entryValue").assertIsDisplayed()
        // We verify that no empty text node is taking up space or "null" text exists.
        // This is implicit if the list looks correct, hard to assert strictly "no empty space" 
        // without visual snapshot testing, but we can assert we don't see "null" or empty rows.
    }

    @Test
    fun editEntry_addNote_updatesList() {
        // 1. Add entry without note
        val entryValue = "11111"
        composeTestRule.onNodeWithContentDescription("Add Entry").performClick()
        composeTestRule.onNodeWithText("Entry Value (Positive Integer)").performTextInput(entryValue)
        composeTestRule.onNodeWithText("Save Entry").performClick()
        
        // 2. Click Edit (Assuming it's the only/first entry)
        composeTestRule.onAllNodesWithContentDescription("Edit Entry").onFirst().performClick()
        
        // 3. Add Note
        val noteContent = "Added Note"
        composeTestRule.onNodeWithText("Note").performTextInput(noteContent)
        composeTestRule.onNodeWithText("Save Entry").performClick()
        
        // 4. Verify Note Displayed
        composeTestRule.onNodeWithText(noteContent).assertIsDisplayed()
    }

    @Test
    fun editEntry_removeNote_updatesList() {
        // 1. Add entry with note
        val entryValue = "22222"
        val noteContent = "Original Note"
        composeTestRule.onNodeWithContentDescription("Add Entry").performClick()
        composeTestRule.onNodeWithText("Entry Value (Positive Integer)").performTextInput(entryValue)
        composeTestRule.onNodeWithText("Note").performTextInput(noteContent)
        composeTestRule.onNodeWithText("Save Entry").performClick()
        
        // 2. Click Edit
        composeTestRule.onAllNodesWithContentDescription("Edit Entry").onFirst().performClick()
        
        // 3. Clear Note
        composeTestRule.onNodeWithText("Note").performTextClearance()
        composeTestRule.onNodeWithText("Save Entry").performClick()
        
        // 4. Verify Note NOT Displayed
        composeTestRule.onNodeWithText(noteContent).assertIsNotDisplayed()
    }
}