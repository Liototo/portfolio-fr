package com.github.se.stepquest

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.se.stepquest.data.model.NotificationData
import com.github.se.stepquest.data.model.NotificationType
import com.github.se.stepquest.screens.BuildNotification
import com.github.se.stepquest.screens.NotificationScreen
import com.github.se.stepquest.viewModels.NotificationViewModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class NotificationsTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun everythingIsDisplayed() {
    composeTestRule.setContent { NotificationScreen(TestUserRepository1()) }
    composeTestRule.onNodeWithTag("Notifications title").assertIsDisplayed()
  }

  @Test
  fun notification_built_correctly() {

    val notification =
        NotificationData(
            "Test notification",
            "testtime",
            "testuuid",
            "testuser1",
            "testuser2",
            "testobjectuuid",
            NotificationType.FRIEND_REQUEST)

    val viewmodel = mockk<NotificationViewModel>()

    every { viewmodel.removeNotification(any()) } answers {}

    every { viewmodel.handleNotificationAction(any(), any()) } answers {}

    composeTestRule.setContent { BuildNotification(notification, "testuid", viewmodel) }

    composeTestRule.onNodeWithTag("CloseNotification").performClick()
    composeTestRule.onNodeWithTag("AcceptNotification").performClick()
    composeTestRule.onNodeWithTag("RejectNotification").performClick()
  }
}
