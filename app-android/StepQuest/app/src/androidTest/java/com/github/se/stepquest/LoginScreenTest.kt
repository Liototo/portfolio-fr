package com.github.se.stepquest

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.se.stepquest.ui.theme.StepQuestTheme
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun everythingIsDisplayed() {

    composeTestRule.setContent { StepQuestTheme { AppNavigationHost() } }

    // Application logo is displayed
    composeTestRule.onNodeWithTag("App logo").assertExists("The app logo doesn't exist?!")
    composeTestRule.onNodeWithTag("App logo").assertHasNoClickAction()

    // Button is displayed and clickable
    composeTestRule.onNodeWithText("Authenticate").assertExists("The login button doesn't exist?!")
    composeTestRule.onNodeWithText("Authenticate").assertHasClickAction()
  }
}
