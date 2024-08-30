package com.github.se.stepquest.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.stepquest.R
import com.github.se.stepquest.Routes
import com.github.se.stepquest.data.model.ChallengeData
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.ui.navigation.TopLevelDestination
import com.github.se.stepquest.viewModels.ChallengesViewModel

/**
 * Screen displaying the user's active challenges.
 *
 * @param userId the current user's database ID.
 * @param navigationActions the handler for navigating the app.
 * @param viewModel the Challenges screen's viewModel.
 */
@Composable
fun ChallengesScreen(
    userId: String,
    navigationActions: NavigationActions,
    viewModel: ChallengesViewModel = viewModel()
) {
  val state by viewModel.state.collectAsState()

  LaunchedEffect(Unit) { viewModel.loadChallenges(userId) }

  Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Start,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Back",
                    fontSize = 20.sp,
                    modifier =
                        Modifier.clickable {
                          navigationActions.navigateTo(
                              TopLevelDestination(Routes.HomeScreen.routName))
                        })
              }
          Spacer(modifier = Modifier.height(16.dp))
          Text(text = "Challenges", fontWeight = FontWeight.Bold, fontSize = 40.sp)
          Spacer(modifier = Modifier.height(16.dp))
          Column { state.challenges.forEach { challenge -> ChallengeItem(challenge = challenge) } }
        }
  }
}

/**
 * One active challenge item displaying various information on the challenge.
 *
 * @param challenge the information on the challenge.
 */
@Composable
fun ChallengeItem(challenge: ChallengeData, viewModel: ChallengesViewModel = viewModel()) {
  val blueThemeColor = colorResource(id = R.color.blueTheme)
  val state by viewModel.state.collectAsState()
  LaunchedEffect(Unit) { viewModel.challengeTypeAction(challenge) }
  Surface(
      color = blueThemeColor,
      modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().padding(horizontal = 16.dp),
      shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(8.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center) {
                Text(
                    text =
                        "Challengers: ${challenge.senderUsername} and ${challenge.challengedUsername}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp))
              }
          Spacer(modifier = Modifier.height(16.dp))
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "Challenge: ${state.challengeText}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp))
              }
          Spacer(modifier = Modifier.height(16.dp))
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "End Date: ${challenge.dateTime}",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp))
              }
        }
      }
}
