package com.github.se.stepquest.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.stepquest.R
import com.github.se.stepquest.services.sendFriendRequest
import com.github.se.stepquest.viewModels.AddFriendViewModel

/**
 * Screen to add friends by searching their username in the database.
 *
 * @param onDismiss the action to execute when closing the screen.
 * @param userId the current user's database ID.
 * @param viewModel the AddFriend screen's viewModel.
 */
@Composable
fun AddFriendScreen(
    onDismiss: () -> Unit,
    userId: String,
    viewModel: AddFriendViewModel = viewModel()
) {
  val state by viewModel.state.collectAsState()

  LaunchedEffect(Unit) { viewModel.fetchCurrentUser(userId) }

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
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Back",
                        fontSize = 20.sp,
                        modifier = Modifier.clickable { onDismiss() })
                  }
              Text(
                  text = "Friends",
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp,
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                  text = "Search for friends",
                  fontSize = 16.sp,
                  modifier = Modifier.padding(top = 16.dp))
              Spacer(modifier = Modifier.height(16.dp))
              TextField(
                  value = state.searchQuery,
                  onValueChange = { viewModel.updateSearchQuery(it) },
                  placeholder = { Text("Enter your friend's username") },
                  modifier =
                      Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("searchField"))
              Spacer(modifier = Modifier.height(16.dp))
              if (state.searchQuery.isNotBlank() && !state.loading) {
                if (state.searchResults.isNotEmpty()) {
                  LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(state.searchResults) {
                      UserItem(currentUser = state.username!!, name = it)
                    }
                  }
                } else {
                  Text(
                      text = "No users were found.",
                      color = Color.Red,
                      modifier = Modifier.padding(horizontal = 16.dp))
                }
              } else if (state.loading) {
                Text(text = "Loading users...", modifier = Modifier.padding(horizontal = 16.dp))
              }
            }
      }
}

/**
 * One search result item, displaying a username. When clicked, expands to show the option to add
 * the user as a friend.
 *
 * @param currentUser the current user's username.
 * @param name the search result user's username.
 */
@Composable
fun UserItem(currentUser: String, name: String) {
  val blueThemeColor = colorResource(id = R.color.blueTheme)
  var isExpanded by remember { mutableStateOf(false) }

  Surface(
      color = blueThemeColor,
      modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp).fillMaxWidth(),
      shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier =
                Modifier.clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()) {
              Row(
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()) {
                    Text(text = name, color = Color.White, fontSize = 20.sp)
                    if (isExpanded) {
                      Icon(
                          imageVector = Icons.Default.Close,
                          contentDescription = "Close",
                          modifier = Modifier.clickable { isExpanded = false },
                          tint = Color.White)
                    }
                  }
              if (isExpanded) {
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Send a friend request",
                    color = Color.White,
                    modifier =
                        Modifier.clickable {
                          sendFriendRequest(currentUser, name)
                          isExpanded = false
                        })
              }
            }
      }
}
