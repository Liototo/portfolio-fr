package com.github.se.stepquest.viewModels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.stepquest.UserRepository
import com.github.se.stepquest.services.cacheDailyWeeklySteps
import com.github.se.stepquest.services.cacheStepGoals
import com.github.se.stepquest.services.getCachedStepInfo
import com.github.se.stepquest.services.isOnline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProgressionPageState(
    val dailyStepsMade: Int = 0,
    val weeklyStepsMade: Int = 0,
    val dailyStepGoal: Int = 5000,
    val weeklyStepGoal: Int = 35000,
    val dailyGoalAchieved: Boolean = false
)

/** ViewModel handling the behaviour of the progression page. */
class ProgressionPageViewModel : ViewModel() {
  private val _state = MutableStateFlow(ProgressionPageState())

  private var _user: UserRepository? = null

  @SuppressLint("StaticFieldLeak") private var _context: Context? = null
  val state: StateFlow<ProgressionPageState>
    get() = _state

  /**
   * Initialises the progression page.
   *
   * @param user the handler for user progress.
   * @param context the application's context.
   */
  fun initialize(user: UserRepository, context: Context) {
    viewModelScope.launch(Dispatchers.IO) {
      val stepList = getCachedStepInfo(context)
      val dailyStepsMade = stepList["dailySteps"] ?: 0
      val weeklyStepsMade = stepList["weeklySteps"] ?: 0
      val dailyStepGoal = stepList["dailyStepGoal"] ?: 5000
      val weeklyStepGoal = stepList["weeklyStepGoal"] ?: 35000
      val dailyGoalAchieved = dailyStepsMade >= dailyStepGoal

      _user = user
      _context = context

      _state.value =
          ProgressionPageState(
              dailyStepsMade = dailyStepsMade,
              weeklyStepsMade = weeklyStepsMade,
              dailyStepGoal = dailyStepGoal,
              weeklyStepGoal = weeklyStepGoal,
              dailyGoalAchieved = dailyGoalAchieved)

      if (isOnline(context)) {
        fetchOnlineData(user, context)
      }
    }
  }

  /**
   * Retrieves daily and weekly step counts.
   *
   * @param user the handler for user progress.
   * @param context the application's context.
   */
  private fun fetchOnlineData(user: UserRepository, context: Context) {
    viewModelScope.launch(Dispatchers.IO) {
      user.getSteps { steps ->
        val onlineDailySteps = steps[0]
        val onlineWeeklySteps = steps[1]

        _state.value =
            _state.value.copy(
                dailyStepsMade = onlineDailySteps,
                weeklyStepsMade = onlineWeeklySteps,
                dailyGoalAchieved = onlineDailySteps >= _state.value.dailyStepGoal)
        cacheDailyWeeklySteps(context, onlineDailySteps, onlineWeeklySteps)
      }
    }
  }

  /**
   * Updates the user's step goals.
   *
   * @param dailyGoal the new daily goal.
   * @param weeklyGoal the new weekly goal.
   * @param context the application's context.
   */
  fun updateGoals(dailyGoal: Int, weeklyGoal: Int, context: Context) {
    _state.value = _state.value.copy(dailyStepGoal = dailyGoal, weeklyStepGoal = weeklyGoal)
    cacheStepGoals(context, dailyGoal, weeklyGoal)
  }

  /** Resets the user's daily goal achievement. */
  fun resetDailyGoalAchievement() {
    _state.value = _state.value.copy(dailyGoalAchieved = false)
  }

  /** Updates the user's step count. */
  fun updateSteps() {
    if (_user != null && _context != null) {
      if (isOnline(_context!!)) {
        _user!!.getSteps { steps ->
          val onlineDailySteps = steps[0]
          val onlineWeeklySteps = steps[1]
          _state.value =
              _state.value.copy(
                  dailyStepsMade = onlineDailySteps, weeklyStepsMade = onlineWeeklySteps)
          cacheDailyWeeklySteps(_context!!, onlineDailySteps, onlineWeeklySteps)
        }
      }
    }
  }
}
