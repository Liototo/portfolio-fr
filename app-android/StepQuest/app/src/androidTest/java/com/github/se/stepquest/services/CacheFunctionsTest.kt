package com.github.se.stepquest.services

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CacheFunctionsTest {

  private lateinit var context: Context

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun caching_a_step_works() {

    deleteCachedSteps(context)

    assert(getCachedSteps(context) == 0)

    saveStepLocally(context)

    assert(getCachedSteps(context) == 1)
  }

  @Test
  fun caching_user_info_works() {

    cacheUserInfo(context, "testID", "testUsername")

    val info = getCachedInfo(context)

    assert(info?.first == "testID")
    assert(info?.second == "testUsername")
  }

  @Test
  fun caching_step_goals_works() {

    cacheStepGoals(context, 10000, 70000)

    val stepInfo = getCachedStepInfo(context)

    assert(stepInfo["dailyStepGoal"] == 10000)
    assert(stepInfo["weeklyStepGoal"] == 70000)
  }
}
