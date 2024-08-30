package com.github.se.stepquest.viewModels

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.stepquest.R
import com.github.se.stepquest.Routes
import com.github.se.stepquest.services.StepCounterService
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.ui.navigation.TopLevelDestination
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewPlayerScreenState(
    val username: String = "",
    val isUsernameAvailable: Boolean = true,
    val blueThemeColor: Int = R.color.blueTheme
)

/** ViewModel handling the behaviour of the NewPlayer screen. */
class NewPlayerViewModel : ViewModel() {

  private val _state = MutableStateFlow(NewPlayerScreenState())
  val state: StateFlow<NewPlayerScreenState> = _state.asStateFlow()

  private val database = FirebaseDatabase.getInstance()
  private var checkUsernameJob: Job? = null

  /**
   * Handles the new player's username.
   *
   * @param newUsername the user's new username.
   */
  fun onUsernameChanged(newUsername: String) {
    _state.value = _state.value.copy(username = newUsername.replace("\\s".toRegex(), "").take(25))
    checkUsernameAvailability()
  }

  /** Checks if the chosen username is available. */
  private fun checkUsernameAvailability() {
    checkUsernameJob?.cancel()
    checkUsernameJob =
        viewModelScope.launch {
          delay(200)
          usernameIsAvailable(_state.value.username, database) { result ->
            _state.value = _state.value.copy(isUsernameAvailable = result)
          }
        }
  }

  /**
   * Handles sign-in completion.
   *
   * @param context the application's context.
   * @param userId the current user's database ID.
   * @param navigationActions the handler for navigating the app.
   */
  fun onSignInClicked(context: Context, userId: String, navigationActions: NavigationActions) {
    if (_state.value.isUsernameAvailable) {
      addUsername(_state.value.username, userId, database)
      context.startService(Intent(context, StepCounterService::class.java))
      runAfterDelay(1000) {
        navigationActions.navigateTo(TopLevelDestination(Routes.MainScreen.routName))
      }
    }
  }

  /**
   * Adds the new user's username to the database.
   *
   * @param username the username to add.
   * @param userId the current user's database ID.
   * @param database the database instance.
   */
  private fun addUsername(username: String, userId: String, database: FirebaseDatabase) {
    val databaseRef = database.reference
    databaseRef.addListenerForSingleValueEvent(
        object : ValueEventListener {
          override fun onDataChange(dataSnapshot: DataSnapshot) {
            databaseRef.child("users").child(userId).child("username").setValue(username)
            val usernamesRef = databaseRef.child("usernames").child(username)
            usernamesRef.setValue(userId)
            databaseRef.child("leaderboard").child(username).setValue(0)
          }

          override fun onCancelled(databaseError: DatabaseError) {
            // add code when failing to access database
          }
        })
  }

  /**
   * Checks in the database if a username is available.
   *
   * @param username the username to check.
   * @param database the database instance
   * @param callback the function to call when a result is obtained.
   */
  private fun usernameIsAvailable(
      username: String,
      database: FirebaseDatabase,
      callback: (Boolean) -> Unit
  ) {
    val usernamesRef = database.reference.child("usernames").child(username)
    usernamesRef.addListenerForSingleValueEvent(
        object : ValueEventListener {
          override fun onDataChange(snapshot: DataSnapshot) {
            val isAvailable = !snapshot.exists()
            callback(isAvailable)
          }

          override fun onCancelled(error: DatabaseError) {
            callback(false)
          }
        })
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
