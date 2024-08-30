package com.github.se.stepquest.viewModels

import android.Manifest
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.github.se.stepquest.Routes
import com.github.se.stepquest.services.checkUserExistsOnLeaderboard
import com.github.se.stepquest.services.getUsername
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.ui.navigation.TopLevelDestination
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DatabaseLoadingState(
    val isNewPlayer: Boolean = false,
    val permissionsGranted: Boolean = false
)

/** ViewModel handling the behaviour of the DatabaseLoading screen. */
class DatabaseLoadingViewModel : ViewModel() {
  private val _state = MutableStateFlow(DatabaseLoadingState())
  val state: StateFlow<DatabaseLoadingState> = _state
  private val database = FirebaseDatabase.getInstance()

  /**
   * Checks if the user already has a profile on the app.
   *
   * @param userId the current user's database ID.
   * @param navigationActions the handler for navigating the app.
   * @param startService the service to start afterwards.
   */
  fun checkUser(userId: String, navigationActions: NavigationActions, startService: () -> Unit) {
    val databaseRef = database.reference

    databaseRef
        .child("users")
        .child(userId)
        .child("username")
        .addListenerForSingleValueEvent(
            object : com.google.firebase.database.ValueEventListener {
              override fun onDataChange(dataSnapshot: com.google.firebase.database.DataSnapshot) {
                val username = dataSnapshot.getValue(String::class.java)
                val isNewPlayer = username == null
                _state.value = _state.value.copy(isNewPlayer = isNewPlayer)
                handleNavigation(userId, isNewPlayer, navigationActions, startService)
              }

              override fun onCancelled(databaseError: com.google.firebase.database.DatabaseError) {
                // Handle cancellation
              }
            })
  }

  /**
   * Checks if the right permissions were granted.
   *
   * @param permissions the app's permissions.
   */
  fun updatePermissions(permissions: Map<String, Boolean>) {
    val bodySensorsGranted = permissions[Manifest.permission.BODY_SENSORS] ?: false
    val activityRecognitionGranted = permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
    _state.value =
        _state.value.copy(permissionsGranted = bodySensorsGranted && activityRecognitionGranted)
  }

  /**
   * Navigates to the right screen depending on if the player is new.
   *
   * @param isNewPlayer whether the current user is a new player.
   * @param navigationActions the handler for navigating the app.
   * @param startService the service to start afterwards.
   */
  private fun handleNavigation(
      userId: String,
      isNewPlayer: Boolean,
      navigationActions: NavigationActions,
      startService: () -> Unit
  ) {
    if (isNewPlayer) {
      navigationActions.navigateTo(TopLevelDestination(Routes.NewPlayerScreen.routName))
    } else {
      getUsername(userId) { username -> checkUserExistsOnLeaderboard(username) }
      startService()
      runAfterDelay(1000) {
        navigationActions.navigateTo(TopLevelDestination(Routes.MainScreen.routName))
      }
    }
  }

  private fun runAfterDelay(delayMillis: Long, action: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(action, delayMillis)
  }
}
