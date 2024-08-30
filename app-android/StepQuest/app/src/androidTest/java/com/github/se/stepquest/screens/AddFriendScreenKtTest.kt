package com.github.se.stepquest.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddFriendScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var database: FirebaseDatabase
  private lateinit var usernameRef: DatabaseReference

  @Before
  fun setUp() {
    database = mockk(relaxed = true)
    usernameRef = mockk(relaxed = true)
    every { database.reference } returns mockk { every { child(any()) } returns usernameRef }
  }

  @Test
  fun addFriendScreen_display_works() {
    // Set up the composable content
    composeTestRule.setContent { AddFriendScreen(onDismiss = { /* Do nothing */}, "testUserId") }

    // Verify the Back button is displayed
    composeTestRule.onNodeWithText("Back").assertIsDisplayed()
    composeTestRule.onNodeWithText("Friends").assertIsDisplayed()
    composeTestRule.onNodeWithText("Search for friends").assertIsDisplayed()
    composeTestRule.onNodeWithText("Enter your friend's username").assertExists()
  }

  @Test
  fun searched_friend_is_displayed() {
    val currentUser = "Current User"
    val name = "Friend Name"
    composeTestRule.setContent { UserItem(currentUser = currentUser, name = name) }
    composeTestRule.onNodeWithText(text = name).assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Close").assertDoesNotExist()
    composeTestRule.onNodeWithText(text = name).performClick()
    composeTestRule.onNodeWithText("Send a friend request").performClick()
  }

  @Test
  fun test_dbUserSearch() {
    every { usernameRef.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)
          listener.onDataChange(mockk())
        }
    composeTestRule.setContent { AddFriendScreen(onDismiss = { /* Do nothing */}, "testUserId") }
    composeTestRule.onNodeWithTag("searchField").performTextInput("user")
    composeTestRule.onNodeWithText("testUsername").assertDoesNotExist()
  }
}
