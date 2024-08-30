package com.github.se.stepquest.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StepGoalsState(val newDailyStepGoal: String = "", val newWeeklyStepGoal: String = "")

/** ViewModel handling the behaviour of the StepGoal update screen. */
class StepGoalsViewModel : ViewModel() {
  private val _state = MutableStateFlow(StepGoalsState())
  val state: StateFlow<StepGoalsState>
    get() = _state

  /**
   * Updates the user's daily step goal.
   *
   * @param newDailyStepGoal the user's new daily step goal.
   */
  fun updateDailyStepGoal(newDailyStepGoal: String) {
    _state.value = _state.value.copy(newDailyStepGoal = newDailyStepGoal)
  }

  /**
   * Confirms the user's new step goals.
   *
   * @param onConfirm the action to execute if the user confirms.
   * @param onDismiss the action to execute once the update is done.
   */
  fun calculateAndConfirmGoals(
      onConfirm: (dailyStepGoal: Int, weeklyStepGoal: Int) -> Unit,
      onDismiss: () -> Unit
  ) {
    viewModelScope.launch {
      val (dailyStep, weeklyStep) =
          calculateStepGoals(_state.value.newDailyStepGoal, _state.value.newWeeklyStepGoal)
      onConfirm(dailyStep, weeklyStep)
      onDismiss()
    }
  }

  /**
   * Rounds the user's daily step goals.
   *
   * @param newDailyStepGoal the user's new daily step goal.
   * @param newWeeklyStepGoal the user's new weekly step goal.
   */
  private fun calculateStepGoals(
      newDailyStepGoal: String,
      newWeeklyStepGoal: String
  ): Pair<Int, Int> {
    val dailyStep =
        newDailyStepGoal
            .filter { it.isDigit() }
            .take(5)
            .let {
              if (it.isBlank()) {
                5000 // Default value if blank
              } else {
                val parsedInput = it.toIntOrNull() ?: 0
                val roundedValue = (parsedInput + 249) / 250 * 250
                if (roundedValue < 1000) {
                  1000
                } else {
                  roundedValue
                }
              }
            }

    val weeklyStep = newWeeklyStepGoal.takeIf { it.isNotBlank() }?.toInt() ?: (dailyStep * 7)

    return Pair(dailyStep, weeklyStep)
  }
}
