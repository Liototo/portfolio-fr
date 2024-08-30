package com.github.se.stepquest.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.viewModels.LeaderboardsState
import com.github.se.stepquest.viewModels.LeaderboardsViewModel
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class LeaderboardScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  private val mockNavigationActions = mockk<NavigationActions>(relaxed = true)

  @Test
  fun testLeaderboardsScreenWithData() {
    // Arrange
    val viewModel = LeaderboardsViewModel()
    val testState =
        LeaderboardsState(
            generalLeaderboard = listOf("Alice" to 2000, "Bob" to 1800, "Charlie" to 1500),
            friendsLeaderboard = listOf("Dave" to 1600, "Eve" to 1400))
    viewModel._state.value = testState

    // Act
    composeTestRule.setContent {
      Leaderboards(
          userId = "testUserId", navigationActions = mockNavigationActions, viewModel = viewModel)
    }

    viewModel._state.value = testState

    // Assert
    composeTestRule.onNodeWithText("Leaderboard").assertIsDisplayed()
    composeTestRule.onNodeWithText("General Leaderboard").assertIsDisplayed()
    composeTestRule.onNodeWithText("Friends Leaderboard").assertIsDisplayed()
    composeTestRule.onNodeWithText("1. Alice : 2000").assertIsDisplayed()
    composeTestRule.onNodeWithText("2. Bob : 1800").assertIsDisplayed()
    composeTestRule.onNodeWithText("3. Charlie : 1500").assertIsDisplayed()
    composeTestRule.onNodeWithText("1. Dave : 1600").assertIsDisplayed()
    composeTestRule.onNodeWithText("2. Eve : 1400").assertIsDisplayed()
  }

  @Test
  fun testLeaderboardsScreenWithoutData() {
    // Arrange
    val viewModel = LeaderboardsViewModel()
    val testState =
        LeaderboardsState(generalLeaderboard = emptyList(), friendsLeaderboard = emptyList())
    viewModel._state.value = testState

    // Act
    composeTestRule.setContent {
      Leaderboards(
          userId = "testUserId", navigationActions = mockNavigationActions, viewModel = viewModel)
    }

    viewModel._state.value = testState

    // Assert
    composeTestRule.onNodeWithText("Leaderboard").assertIsDisplayed()
    composeTestRule.onNodeWithText("General Leaderboard").assertIsDisplayed()
    composeTestRule.onNodeWithText("Friends Leaderboard").assertIsDisplayed()
  }

  @Test
  fun testBackButtonNavigation() {
    // Arrange
    val viewModel = LeaderboardsViewModel()

    // Act
    composeTestRule.setContent {
      Leaderboards(
          userId = "testUserId", navigationActions = mockNavigationActions, viewModel = viewModel)
    }

    // Assert
    composeTestRule.onNodeWithText("Back").assertIsDisplayed().performClick()
    // Assuming the NavigationActions is mocked to verify the navigation call
    // Verify navigation call to the home screen
  }
}
