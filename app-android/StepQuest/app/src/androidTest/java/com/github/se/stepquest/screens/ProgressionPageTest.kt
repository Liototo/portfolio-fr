package com.github.se.stepquest.screens

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.github.se.stepquest.TestUserRepository1
import com.github.se.stepquest.TestUserRepository2
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProgressionPageTest {

  private lateinit var context: Context

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext<Context>()
  }

  @Test
  fun everythingIsDisplayed() {
    composeTestRule.setContent { ProgressionPage(TestUserRepository1(), context) }
    composeTestRule.onNodeWithTag("CharacterImage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Daily steps icon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Daily steps text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Weekly steps icon").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Weekly steps text").assertIsDisplayed()

    val button = composeTestRule.onNodeWithTag("SetNewGoalButton")
    button.assertIsDisplayed()
  }

  @Test
  fun dailyGoalAchievementTest() {
    composeTestRule.setContent { ProgressionPage(TestUserRepository2(), context) }
    composeTestRule.onNodeWithTag("main congratulation dialog text").assertIsDisplayed()
    composeTestRule.onNodeWithTag("Confirm button").assertIsDisplayed()
  }

  @Test
  fun offline_stats_work() {
    composeTestRule.setContent { OfflineStats(context) }
  }
}
