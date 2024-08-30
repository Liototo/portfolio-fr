package com.github.se.stepquest.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.stepquest.R
import com.github.se.stepquest.UserRepository
import com.github.se.stepquest.data.model.NotificationData
import com.github.se.stepquest.viewModels.NotificationViewModel

/**
 * Screen displaying the user's notifications.
 *
 * @param userRepository the handler for user progress.
 * @param notificationViewModel the Notification screen's viewModel.
 */
@Composable
fun NotificationScreen(
    userRepository: UserRepository,
    notificationViewModel: NotificationViewModel = viewModel()
) {
  val uuid = userRepository.getUid()
  val notificationList by notificationViewModel.notificationList.collectAsState()

  LaunchedEffect(uuid) {
    if (uuid != null) {
      notificationViewModel.updateNotificationList(uuid)
    }
  }

  Column(modifier = Modifier.padding(0.dp, 30.dp)) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
      Text("Notifications", fontSize = 20.sp, modifier = Modifier.testTag("Notifications title"))
    }
    Box(modifier = Modifier.height(40.dp))
    Divider(color = colorResource(id = R.color.blueTheme), thickness = 1.dp)
    if (uuid != null) {
      NotificationList(notificationList, uuid, notificationViewModel)
    }
  }
}

/**
 * Builds the notification list.
 *
 * @param notificationList the list to fill.
 * @param userId the current user's database ID.
 * @param notificationViewModel the Notification screen's viewModel.
 */
@Composable
private fun NotificationList(
    notificationList: List<NotificationData>,
    userId: String,
    notificationViewModel: NotificationViewModel
) {
  LazyColumn {
    items(notificationList.size) { index ->
      BuildNotification(notificationList[index], userId, notificationViewModel)
    }
  }
}

/**
 * Builds a single notification.
 *
 * @param data the notification to build.
 * @param userId the current user's database ID.
 * @param notificationViewModel the Notification screen's viewModel.
 */
@Composable
fun BuildNotification(
    data: NotificationData?,
    userId: String,
    notificationViewModel: NotificationViewModel
) {
  if (data == null) return
  Column {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(20.dp, 10.dp)) {
          Text(text = data.text)
          Row {
            Text(text = data.dateTime)
            Box(modifier = Modifier.width(20.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                modifier =
                    Modifier.size(20.dp, 20.dp).testTag("CloseNotification").clickable {
                      notificationViewModel.removeNotification(data)
                    })
          }
        }
    if (data.senderUuid.isNotEmpty()) {
      Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth().padding(20.dp, 10.dp)) {
            Button(
                onClick = { notificationViewModel.handleNotificationAction(data, userId) },
                content = { Text("Accept") },
                modifier =
                    Modifier.fillMaxWidth().weight(1f).height(35.dp).testTag("AcceptNotification"),
                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.blueTheme)))
            Box(modifier = Modifier.width(20.dp))
            Button(
                onClick = { notificationViewModel.removeNotification(data) },
                content = { Text("Reject") },
                modifier =
                    Modifier.fillMaxWidth().weight(1f).height(35.dp).testTag("RejectNotification"),
                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.lightGrey)))
          }
      Divider(color = colorResource(id = R.color.blueTheme), thickness = 1.dp)
    }
  }
}
