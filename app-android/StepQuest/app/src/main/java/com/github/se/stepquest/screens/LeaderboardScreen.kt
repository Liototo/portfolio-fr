package com.github.se.stepquest.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.viewModels.LeaderboardsViewModel

/** Screen displaying the global and friend score leaderboards. */
@Composable
fun Leaderboards(
    userId: String,
    navigationActions: NavigationActions,
    viewModel: LeaderboardsViewModel = viewModel()
) {
  val state by viewModel.state.collectAsState()
  LaunchedEffect(Unit) { viewModel.initialize(userId) }

  Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
          item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically) {
                  Text(
                      text = "Back",
                      fontSize = 20.sp,
                      modifier = Modifier.clickable { viewModel.backToHome(navigationActions) })
                }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Leaderboard", fontWeight = FontWeight.Bold, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(16.dp))
          }

          item {
            LeaderboardCard(
                title = "General Leaderboard",
                leaderboard = state.generalLeaderboard,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(300.dp))
          }

          item {
            LeaderboardCard(
                title = "Friends Leaderboard",
                leaderboard = state.friendsLeaderboard,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(300.dp))
          }
        }
  }
}

@Composable
fun LeaderboardCard(
    title: String,
    leaderboard: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
  val blueThemeColor: Int = com.github.se.stepquest.R.color.blueTheme
  Card(
      modifier = modifier,
      colors = CardDefaults.cardColors(containerColor = colorResource(blueThemeColor))) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
          Text(
              text = title,
              color = Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center)
          Spacer(modifier = Modifier.height(16.dp))
          if (leaderboard.isEmpty()) {
            Text(
                text = "Not available",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp).semantics { testTag = "NotAv" })
          } else {
            LazyColumn {
              items(leaderboard.size) { index ->
                val user = leaderboard[index]
                val scoreText = if (user.second > 99999) "+99999" else user.second.toString()
                Text(
                    text = "${index + 1}. ${user.first} : $scoreText",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier.padding(vertical = 4.dp).semantics { testTag = "${index + 1}" })
              }
            }
          }
        }
      }
}
