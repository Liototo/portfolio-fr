package com.github.se.stepquest.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.stepquest.data.model.ChallengeData
import com.github.se.stepquest.data.model.ChallengeProgression
import com.github.se.stepquest.data.model.ChallengeType
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.viewModels.ChallengesState
import com.github.se.stepquest.viewModels.ChallengesViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChallengesUITest {

  @get:Rule val composeTestRule = createComposeRule()

  private val mockNavigationActions = mockk<NavigationActions>(relaxed = true)

  @Test
  fun testChallengesScreenDisplaysCorrectly() {
    // Arrange
    val challengesViewModel = ChallengesViewModel()
    val challenges =
        listOf(
            ChallengeData(
                uuid = "1",
                type = ChallengeType.REGULAR_STEP_CHALLENGE,
                stepsToMake = 1000,
                dateTime = "May 28, 2024",
                challengedUsername = "Bob",
                senderUsername = "Alice",
                challengedProgress = ChallengeProgression("user1", 500, 0),
                senderProgress = ChallengeProgression("user2", 300, 0)))
    val testState = ChallengesState(challenges = challenges, challengeText = "Walk 1000 steps!")

    // Act
    composeTestRule.setContent {
      ChallengesScreen(
          userId = "testUser",
          navigationActions = mockNavigationActions,
          viewModel = challengesViewModel)
    }

    challengesViewModel._state.value = testState

    // Assert
    composeTestRule.onNodeWithText("Back").assertExists()
    composeTestRule.onNodeWithText("Challenges").assertExists()
  }
}
