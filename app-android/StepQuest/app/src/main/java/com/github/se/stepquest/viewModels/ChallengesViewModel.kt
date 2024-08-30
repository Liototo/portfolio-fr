package com.github.se.stepquest.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.stepquest.data.model.ChallengeData
import com.github.se.stepquest.data.model.ChallengeType
import com.github.se.stepquest.services.getChallenges
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChallengesState(
    val challenges: List<ChallengeData> = listOf(),
    val challengeText: String = "Not available"
)

/** ViewModel handling the behaviour of the Challenges screen. */
class ChallengesViewModel : ViewModel() {
  val _state = MutableStateFlow(ChallengesState())
  val state: StateFlow<ChallengesState> = _state

  /**
   * Loads the user's active challenges.
   *
   * @param userId the current user's database ID.
   */
  fun loadChallenges(userId: String) {
    viewModelScope.launch {
      getChallenges(userId) { receivedChallenges ->
        _state.value = _state.value.copy(challenges = receivedChallenges)
      }
    }
  }

  /**
   * Determines the message to display with the challenge type
   *
   * @param challengeData the challenge displayed
   */
  fun challengeTypeAction(challengeData: ChallengeData) {
    when (challengeData.type) {
      ChallengeType.REGULAR_STEP_CHALLENGE ->
          _state.value =
              _state.value.copy(challengeText = "Walk ${challengeData.stepsToMake} steps!")
      ChallengeType.DAILY_STEP_CHALLENGE ->
          _state.value =
              _state.value.copy(challengeText = "Walk ${challengeData.stepsToMake} steps today!")
    }
  }
}
