package com.github.se.stepquest.screens

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.github.se.stepquest.Routes
import com.github.se.stepquest.services.StepCounterService
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.ui.navigation.TopLevelDestination
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseLoadingScreenTest {

  private lateinit var navigationActions: NavigationActions
  private lateinit var context: Context
  private lateinit var database: FirebaseDatabase
  private lateinit var usernameRef: DatabaseReference

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.BODY_SENSORS,
          android.Manifest.permission.ACTIVITY_RECOGNITION)

  @Before
  fun setup() {
    navigationActions = mockk(relaxed = true)
    context = ApplicationProvider.getApplicationContext<Context>()
    database = mockk(relaxed = true)
    usernameRef = mockk(relaxed = true)
    every { database.reference } returns
        mockk {
          every { child(any()) } returns
              mockk {
                every { child(any()) } returns mockk { every { child(any()) } returns usernameRef }
              }
        }
  }

  @Test
  fun displayWaitingForDatabase() {
    val startServiceLambda: () -> Unit = {
      context.startService(Intent(context, StepCounterService::class.java))
    }
    every { usernameRef.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)
          listener.onDataChange(mockk { every { getValue(String::class.java) } returns null })
        }
    composeTestRule.setContent {
      DatabaseLoadingScreen(navigationActions, { startServiceLambda() }, "testUserId")
    }

    composeTestRule.onNodeWithText("Waiting for database...").assertIsDisplayed()
    verify(exactly = 0) {
      navigationActions.navigateTo(TopLevelDestination(Routes.NewPlayerScreen.routName))
    }
  }
}
