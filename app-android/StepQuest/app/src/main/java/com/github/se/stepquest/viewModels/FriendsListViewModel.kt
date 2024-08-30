package com.github.se.stepquest.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.stepquest.Friend
import com.github.se.stepquest.services.fetchFriendsListFromDatabase
import com.github.se.stepquest.services.isOnline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FriendsListState(
    val currentFriendsList: List<Friend>? = emptyList(),
    val showAddFriendScreen: Boolean = false,
    val showFriendProfile: Boolean = false,
    val selectedFriend: Friend? = null,
    val isOnline: Boolean = true
)

/** ViewModel handling the behaviour of the FriendsList screen. */
class FriendsViewModel : ViewModel() {

  val state_ = MutableStateFlow(FriendsListState())
  val state: StateFlow<FriendsListState> = state_

  /**
   * Retrieves the friends list.
   *
   * @param userId the current user's database ID.
   */
  fun fetchFriends(userId: String) {
    viewModelScope.launch {
      fetchFriendsListFromDatabase(userId) { friendsList ->
        state_.value = state_.value.copy(currentFriendsList = friendsList)
      }
    }
  }

  /**
   * Updates the currently selected friend profile.
   *
   * @param friend the current friend.
   */
  fun selectFriend(friend: Friend) {
    state_.value = state_.value.copy(selectedFriend = friend, showFriendProfile = true)
  }

  /** Unselects the currently selected friend profile. */
  fun deselectFriend() {
    state_.value = state_.value.copy(selectedFriend = null, showFriendProfile = false)
  }

  /**
   * Switches views between the FriendsList screen and the AddFriend screen.
   *
   * @param show whether or not the AddFriend screen should be displayed.
   */
  fun toggleAddFriendScreen(show: Boolean) {
    state_.value = state_.value.copy(showAddFriendScreen = show)
  }

  /**
   * Checks if the user is online.
   *
   * @param context the application's context.
   */
  fun checkOnlineStatus(context: Context) {
    val online = isOnline(context)
    state_.value = state_.value.copy(isOnline = online)
  }
}
