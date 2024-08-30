package com.github.se.stepquest.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
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
import com.github.se.stepquest.Routes
import com.github.se.stepquest.services.isOnline
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.ui.navigation.TopLevelDestination
import com.github.se.stepquest.viewModels.FriendsViewModel

/**
 * Checks if the user is online before displaying the friend list.
 *
 * @param navigationActions the handler for navigating the app.
 * @param userId the current user's database ID.
 * @param context the application's context.
 * @param friendsViewModel the FriendsList screen's viewModel.
 */
@Composable
fun FriendsListScreenCheck(
    navigationActions: NavigationActions,
    userId: String,
    context: Context,
    friendsViewModel: FriendsViewModel = viewModel()
) {
  LaunchedEffect(Unit) { friendsViewModel.checkOnlineStatus(context) }
  val state by friendsViewModel.state.collectAsState()
  if (state.isOnline) {
    FriendsListScreen(navigationActions, userId, friendsViewModel)
  } else {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
      Text(
          text = "You must be online to view your friend list.",
          color = Color.Red,
          fontSize = 18.sp)
    }
  }
}

/**
 * Screen displaying the user's friend list.
 *
 * @param navigationActions the handler for navigating the app.
 * @param userId the current user's database ID.
 * @param friendsViewModel the FriendsList screen's viewModel.
 */
@Composable
fun FriendsListScreen(
    navigationActions: NavigationActions,
    userId: String,
    friendsViewModel: FriendsViewModel
) {
  val blueThemeColor = colorResource(id = R.color.blueTheme)
  val state by friendsViewModel.state.collectAsState()

  LaunchedEffect(Unit) { friendsViewModel.fetchFriends(userId) }

  if (state.showAddFriendScreen) {
    AddFriendScreen(onDismiss = { friendsViewModel.toggleAddFriendScreen(false) }, userId)
  } else if (state.showFriendProfile) {
    FriendDialogBox(
        friend = state.selectedFriend!!, userId, onDismiss = { friendsViewModel.deselectFriend() })
  } else {
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
                                TopLevelDestination(Routes.ProfileScreen.routName))
                          })
                }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Friends", fontWeight = FontWeight.Bold, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { friendsViewModel.toggleAddFriendScreen(true) },
                colors = ButtonDefaults.buttonColors(blueThemeColor),
                modifier =
                    Modifier.fillMaxWidth()
                        .height(72.dp)
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp)) {
                  androidx.compose.material3.Text(
                      text = "Add Friends", fontSize = 24.sp, color = Color.White)
                }
            Spacer(modifier = Modifier.height(16.dp))
            if (state.currentFriendsList!!.isEmpty()) {
              Text(text = "No friends yet", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            } else {
              LazyColumn {
                items(state.currentFriendsList!!) { friend ->
                  FriendItem(friend = friend, onClick = { friendsViewModel.selectFriend(friend) })
                }
              }
            }
          }
    }
  }
}

/**
 * One friend list item.
 *
 * @param friend the current friend.
 * @param onClick the action to execute when clicking the friend item.
 */
@Composable
fun FriendItem(friend: Friend, onClick: () -> Unit) {
  val blueThemeColor = Color.Gray
  Surface(
      color = blueThemeColor,
      modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().padding(horizontal = 16.dp),
      shape = MaterialTheme.shapes.medium,
      onClick = onClick) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = friend.name,
                  modifier = Modifier.weight(1f),
                  color = Color.White,
                  style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            }
      }
}
