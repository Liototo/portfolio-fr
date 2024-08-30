package com.github.se.stepquest.viewModels

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.stepquest.Friend
import com.github.se.stepquest.data.model.ChallengeType
import com.github.se.stepquest.services.createChallengeItem
import com.github.se.stepquest.services.getUserId
import com.github.se.stepquest.services.getUsername
import com.github.se.stepquest.services.sendPendingChallenge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FriendDialogState(
    val challengeMode: Boolean = false,
    val challengeSentVisible: Boolean = false,
    val friend: Friend? = null
)

/** ViewModel handling the behaviour of the FriendDialog screen. */
class FriendDialogViewModel : ViewModel() {
  private val _state = MutableStateFlow(FriendDialogState())
  val state: StateFlow<FriendDialogState> = _state

  /**
   * Adds the friend's information to the viewModel.
   *
   * @param friend the current friend.
   */
  fun setFriend(friend: Friend) {
    _state.value = _state.value.copy(friend = friend)
  }

  /** Switches views between the base screen and the different challenges to send. */
  fun toggleChallengeMode() {
    _state.value = _state.value.copy(challengeMode = !_state.value.challengeMode)
  }

  /**
   * Sends a challenge to a friend.
   *
   * @param userId the current user's database ID.
   * @param friendName the friend's name.
   * @param challengeType the type of the challenge to send.
   */
  fun sendChallenge(userId: String, friendName: String, challengeType: ChallengeType) {
    viewModelScope.launch {
      getUsername(userId) { currentUsername ->
        getUserId(friendName) { friendUserId ->
          val challenge =
              createChallengeItem(userId, currentUsername, friendUserId, friendName, challengeType)
          sendPendingChallenge(challenge)
          _state.value = _state.value.copy(challengeSentVisible = true, challengeMode = false)
          runAfterDelay(2000) { _state.value = _state.value.copy(challengeSentVisible = false) }
        }
      }
    }
  }

  /**
   * Runs a certain action after a given delay.
   *
   * @param delayMillis the number of milliseconds to wait.
   * @param action the action to execute once the delay has passed.
   */
  private fun runAfterDelay(delayMillis: Long, action: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(action, delayMillis)
  }
}
