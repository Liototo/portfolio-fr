package com.github.se.stepquest.screens

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.github.se.stepquest.Friend
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.viewModels.FriendsListState
import com.github.se.stepquest.viewModels.FriendsViewModel
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class FriendsListKtTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val mockNavigationActions = mockk<NavigationActions>(relaxed = true)
  private val context: Context = ApplicationProvider.getApplicationContext()

  @Test
  fun testFriendsListIsDisplayed() {
    // Arrange
    val friendsViewModel = FriendsViewModel()
    val testState =
        FriendsListState(
            currentFriendsList =
                listOf(Friend(name = "Alice", status = true), Friend(name = "Bob", status = false)),
            isOnline = true)
    friendsViewModel.state_.value = testState

    // Act
    composeTestRule.setContent {
      FriendsListScreenCheck(
          navigationActions = mockNavigationActions,
          userId = "testUserId",
          context = context,
          friendsViewModel = friendsViewModel)
    }
    friendsViewModel.state_.value = testState

    // Assert
    composeTestRule.onNodeWithText("Friends").assertIsDisplayed()
    composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
    composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
  }

  @Test
  fun testEmptyFriendsList() {
    val friendsViewModel = FriendsViewModel()
    val testState = FriendsListState(currentFriendsList = emptyList(), isOnline = true)
    friendsViewModel.state_.value = testState
    composeTestRule.setContent {
      FriendsListScreenCheck(
          navigationActions = mockNavigationActions,
          userId = "testUserId",
          context = context,
          friendsViewModel = friendsViewModel)
    }
    composeTestRule.onNodeWithText("No friends yet").assertIsDisplayed()
  }

  @Test
  fun testFriendsListWhenOffline() {
    val friendsViewModel = FriendsViewModel()
    val testState = FriendsListState(currentFriendsList = emptyList(), isOnline = false)
    friendsViewModel.state_.value = testState
    composeTestRule.setContent {
      FriendsListScreenCheck(
          navigationActions = mockNavigationActions,
          userId = "testUserId",
          context = context,
          friendsViewModel = friendsViewModel)
    }
    friendsViewModel.state_.value = testState
    composeTestRule
        .onNodeWithText("You must be online to view your friend list.")
        .assertIsDisplayed()
  }
}
