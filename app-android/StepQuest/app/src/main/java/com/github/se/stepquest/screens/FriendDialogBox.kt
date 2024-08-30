package com.github.se.stepquest.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.stepquest.Friend
import com.github.se.stepquest.R
import com.github.se.stepquest.data.model.ChallengeType
import com.github.se.stepquest.viewModels.FriendDialogViewModel

/**
 * Screen to send challenges to a friend.
 *
 * @param friend the current friend.
 * @param userId the current user's database ID.
 * @param onDismiss the action to execute when closing the screen.
 * @param viewModel the FriendDialog screen's viewModel.
 */
@Composable
fun FriendDialogBox(
    friend: Friend,
    userId: String,
    onDismiss: () -> Unit,
    viewModel: FriendDialogViewModel = viewModel()
) {
  LaunchedEffect(friend) { viewModel.setFriend(friend) }
  val state by viewModel.state.collectAsState()

  Surface(
      color = Color.White,
      border = BorderStroke(1.dp, Color.Black),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier.padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onDismiss() }, modifier = Modifier.padding(8.dp)) {
                      Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                  }
              Text(text = state.friend?.name ?: "", fontWeight = FontWeight.Bold, fontSize = 40.sp)
              Spacer(modifier = Modifier.height(2.dp))
              Spacer(modifier = Modifier.height(16.dp))
              if (state.challengeMode) {
                ButtonElement(
                    buttonText = "Regular Step Challenge",
                    onClick = {
                      viewModel.sendChallenge(
                          userId, friend.name, ChallengeType.REGULAR_STEP_CHALLENGE)
                    })
                ButtonElement(
                    buttonText = "Daily Step Challenge",
                    onClick = {
                      viewModel.sendChallenge(
                          userId, friend.name, ChallengeType.DAILY_STEP_CHALLENGE)
                    })
              } else {
                if (state.challengeSentVisible) {
                  Text(
                      text = "Challenge sent",
                      color = Color.Green,
                      modifier = Modifier.padding(top = 4.dp))
                }
                ButtonElement(
                    buttonText = "Challenge", onClick = { viewModel.toggleChallengeMode() })
              }
            }
      }
}

/**
 * Standardised button for the FriendDialog screen.
 *
 * @param buttonText the text to display on the button.
 * @param onClick the action to execute when clicking the button.
 */
@Composable
fun ButtonElement(buttonText: String, onClick: () -> Unit) {
  val blueThemeColor = colorResource(id = R.color.blueTheme)
  Button(
      onClick = { onClick() },
      colors = ButtonDefaults.buttonColors(blueThemeColor),
      modifier =
          Modifier.fillMaxWidth()
              .height(72.dp)
              .padding(vertical = 8.dp)
              .padding(horizontal = 16.dp),
      shape = RoundedCornerShape(8.dp)) {
        Text(text = buttonText, fontSize = 24.sp, color = Color.White)
      }
}
