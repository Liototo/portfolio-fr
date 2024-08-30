package com.github.se.stepquest.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.stepquest.Routes
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.ui.navigation.TopLevelDestination
import com.github.se.stepquest.viewModels.HomeViewModel

/**
 * The application's home screen.
 *
 * @param navigationActions the handler for navigating the app.
 * @param userId the current user's database ID.
 * @param context the application's context.
 * @param viewModel the Home screen's viewModel.
 */
@Composable
fun HomeScreen(
    navigationActions: NavigationActions,
    userId: String,
    context: Context = LocalContext.current,
    viewModel: HomeViewModel = viewModel()
) {
  val state by viewModel.state.collectAsState()

  LaunchedEffect(userId) { viewModel.initialize(userId, context) }

  if (state.showChallengeCompletionPopUp) {
    CongratulationDialog(
        titleText = "Challenges",
        mainText = "Congratulations! You have completed some challenges!",
        xpNumber = 100) {
          viewModel.dismissChallengeCompletionPopUp()
        }
  }

  Scaffold(
      containerColor = Color(0xFF0D99FF),
      topBar = {
        Row(modifier = Modifier.height(100.dp).fillMaxWidth().padding(start = 15.dp, end = 15.dp)) {
          // Notifications icon
          TextButton(
              onClick = {
                navigationActions.navigateTo(
                    TopLevelDestination(Routes.NotificationScreen.routName))
              },
              modifier = Modifier.testTag("notifications_button")) {
                Image(
                    painter = painterResource(com.github.se.stepquest.R.drawable.notification),
                    modifier = Modifier.fillMaxHeight().size(50.dp),
                    contentDescription = "notifications_icon")
              }
          Spacer(Modifier.weight(1f))
          // Profile icon
          TextButton(
              onClick = {
                navigationActions.navigateTo(TopLevelDestination(Routes.ProfileScreen.routName))
              },
              modifier = Modifier.testTag("profile_button")) {
                Image(
                    painter = painterResource(com.github.se.stepquest.R.drawable.profile),
                    modifier = Modifier.fillMaxHeight().size(50.dp),
                    contentDescription = "profile_icon")
              }
        }
      },
      bottomBar = {}) { innerPadding ->
        // Main content of the home screen
        Column(modifier = Modifier.padding(innerPadding).fillMaxWidth()) {
          // Challenges tab
          Card(
              modifier = Modifier.fillMaxWidth().padding(25.dp).height(190.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                  Text(
                      text = "Challenges",
                      modifier = Modifier.padding(start = 18.dp, top = 14.dp),
                      fontSize = 20.sp,
                      fontWeight = FontWeight.Bold)
                  Column {
                    // Challenge
                    if (state.topChallenge == null) {
                      Text(
                          text = "No challenges available",
                          modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(top = 50.dp),
                          fontSize = 16.sp,
                          textAlign = TextAlign.Center,
                          fontWeight = FontWeight.Bold)
                    } else {
                      Row(
                          modifier = Modifier.padding(top = 20.dp, start = 30.dp).fillMaxWidth(),
                          horizontalArrangement = Arrangement.Center,
                          verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter =
                                    painterResource(
                                        com.github.se.stepquest.R.drawable.profile_challenges),
                                modifier = Modifier.size(40.dp).fillMaxHeight().fillMaxWidth(),
                                contentDescription = "profile_challenges")
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                  Spacer(modifier = Modifier.width(10.dp))
                                  Text(text = "Main challenge", fontSize = 18.sp)
                                  Text(
                                      text =
                                          "${state.topChallenge!!.stepsToMake} steps until ${state.topChallenge!!.dateTime}!",
                                      fontSize = 18.sp,
                                      fontWeight = FontWeight.Bold)
                                }
                          }
                      Spacer(modifier = Modifier.weight(1f))
                      Button(
                          onClick = {
                            navigationActions.navigateTo(
                                TopLevelDestination(Routes.ChallengeScreen.routName))
                          },
                          colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF)),
                          modifier =
                              Modifier.padding(horizontal = 30.dp, vertical = 10.dp)
                                  .height(40.dp)
                                  .fillMaxWidth()
                                  .align(Alignment.CenterHorizontally)) {
                            Text(
                                text = "Check active challenges",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(0.dp))
                          }
                    }
                  }
                }
              }

          // Leaderboard
          Card(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(start = 25.dp, end = 25.dp, bottom = 30.dp)
                      .height(300.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Text(
                    text = "Leaderboard",
                    modifier = Modifier.padding(start = 18.dp, top = 14.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold)
                Column {
                  if (state.leaderboard.isEmpty()) {
                    Text(
                        text = "Leaderboard is not available",
                        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(top = 70.dp),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold)
                  } else {
                    for ((i, user) in state.leaderboard.withIndex()) {
                      var uScore = user.second.toString()
                      if (user.second > 99999) {
                        uScore = "+99999"
                      }
                      Row(
                          modifier = Modifier.padding(top = 10.dp, start = 30.dp).fillMaxWidth(),
                          horizontalArrangement = Arrangement.Center,
                          verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${i + 1}. ${user.first} : $uScore",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f))
                          }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                          navigationActions.navigateTo(
                              TopLevelDestination(Routes.Leaderboard.routName))
                        },
                        colors = ButtonDefaults.buttonColors(Color(0xFF0D99FF)),
                        modifier =
                            Modifier.padding(horizontal = 30.dp, vertical = 10.dp)
                                .height(40.dp)
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)) {
                          Text(
                              text = "Check the leaderboard",
                              fontSize = 16.sp,
                              modifier = Modifier.padding(0.dp))
                        }
                  }
                }
              }

          if (state.isOnline) {
            Card(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(start = 25.dp, end = 25.dp, top = 10.dp)
                        .height(100.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)) {
                  Text(
                      text = "Your current score is ${state.userScore}",
                      modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                      fontSize = 18.sp,
                      fontWeight = FontWeight.Bold)
                  Text(
                      text = "which makes you number ${state.currentPosition}",
                      modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                      fontSize = 18.sp,
                      fontWeight = FontWeight.Bold)
                }
          }
        }
      }
}
