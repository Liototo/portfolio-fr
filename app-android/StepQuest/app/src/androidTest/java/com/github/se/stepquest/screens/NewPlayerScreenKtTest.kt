package com.github.se.stepquest.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.mockk.*
import io.mockk.junit4.MockKRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewPlayerScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule val mockkRule = MockKRule(this)

  private lateinit var database: FirebaseDatabase
  private lateinit var usernameRef: DatabaseReference

  @Before
  fun setUp() {
    database = mockk(relaxed = true)
    usernameRef = mockk(relaxed = true)
    every { database.reference } returns
        mockk {
          every { child(any()) } returns
              mockk {
                every { child(any()) } returns mockk { every { child(any()) } returns usernameRef }
              }
        }
    every { usernameRef.setValue(any()) } returns mockk()
  }

  @Test
  fun blankUsernameIsNotAccepted() {
    every { usernameRef.addListenerForSingleValueEvent(any()) } answers
        {
          val callback = arg<ValueEventListener>(1)
          callback.onDataChange(mockk())
        }

    composeTestRule.setContent {
      NewPlayerScreen(navigationActions = mockk(), context = mockk(), "testUserId")
    }

    composeTestRule.onNodeWithTag("username_input").performTextInput("")
    // composeTestRule.onNodeWithText("Sign in").assertDoesNotExist()
  }
}
