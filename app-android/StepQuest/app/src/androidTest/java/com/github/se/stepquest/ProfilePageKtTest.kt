package com.github.se.stepquest

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.github.se.stepquest.screens.ProfilePageLayout
import com.github.se.stepquest.ui.theme.StepQuestTheme
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfilePageLayoutTest {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule val mockkRule = MockKRule(this)

  private lateinit var database: FirebaseDatabase
  private lateinit var stepsRefTotal: DatabaseReference
  private lateinit var usernameRef: DatabaseReference

  @Before
  fun setUp() {
    database = mockk(relaxed = true)
    stepsRefTotal = mockk(relaxed = true)
    usernameRef = mockk(relaxed = true)
    every { database.reference } returns
        mockk {
          every { child(any()) } returns
              mockk {
                every { child(any()) } returns
                    mockk { every { child(any()) } returns stepsRefTotal andThen usernameRef }
              }
        }
  }

  @Test
  fun display_profile_layout() {
    composeTestRule.setContent {
      StepQuestTheme {
        ProfilePageLayout(
            navigationActions = mockk(relaxed = true),
            userId = "testUserId",
            mockk(),
            LocalContext.current)
      }
    }

    // Assertions
    // Check if the "Profile" text is displayed
    composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    // Check if the "Total Steps" text is displayed
    composeTestRule.onNodeWithText("Total Steps: 0").assertIsDisplayed()
    // Check if the "Friends List" button is displayed and has click action
    composeTestRule.onNodeWithText("Friends List").apply {
      assertIsDisplayed()
      assertHasClickAction()
    }
  }

  @Test
  fun firebase_access_tests_for_profile() {

    every { stepsRefTotal.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)
          listener.onDataChange(mockk { every { getValue(Int::class.java) } returns 0 })
        }
    every { usernameRef.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)
          listener.onDataChange(mockk { every { getValue(String::class.java) } returns "No name" })
        }
    composeTestRule.setContent {
      StepQuestTheme {
        ProfilePageLayout(
            navigationActions = mockk(relaxed = true), "testUserId", mockk(), LocalContext.current)
      }
    }

    composeTestRule.onNodeWithText("Total Steps: 0").assertIsDisplayed()
  }
}
