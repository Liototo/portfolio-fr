package com.github.se.stepquest.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.stepquest.Routes
import com.github.se.stepquest.services.fetchFriendsListFromDatabase
import com.github.se.stepquest.services.getFriendsLeaderboard
import com.github.se.stepquest.services.getTopLeaderboard
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.ui.navigation.TopLevelDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LeaderboardsState(
    val generalLeaderboard: List<Pair<String, Int>> = emptyList(),
    val friendsLeaderboard: List<Pair<String, Int>> = emptyList(),
)

class LeaderboardsViewModel : ViewModel() {
  val _state = MutableStateFlow(LeaderboardsState())
  val state: StateFlow<LeaderboardsState> = _state.asStateFlow()

  fun initialize(userId: String) {
    viewModelScope.launch {
      getTopLeaderboard(30) { topLeaderboard ->
        _state.value = _state.value.copy(generalLeaderboard = topLeaderboard ?: emptyList())
      }

      // Assuming you have access to userId from somewhere
      // Replace "userId" with your actual userId variable
      fetchFriendsListFromDatabase(userId) { friendsList ->
        if (friendsList != null) {
          getFriendsLeaderboard(friendsList) { topFriendsLeaderboard ->
            _state.value =
                _state.value.copy(friendsLeaderboard = topFriendsLeaderboard ?: emptyList())
          }
        }
      }
    }
  }

  fun backToHome(navigationActions: NavigationActions) {
    navigationActions.navigateTo(TopLevelDestination(Routes.HomeScreen.routName))
  }
}
