package com.github.se.stepquest.viewModels

import android.app.Activity
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.github.se.stepquest.Routes
import com.github.se.stepquest.services.getCachedInfo
import com.github.se.stepquest.services.isOnline
import com.github.se.stepquest.services.setOnline
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.ui.navigation.TopLevelDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginScreenState(
    val isOnline: Boolean = false,
    val showError: Boolean = false,
    val blueThemeColor: Color = Color(0xFF0D99FF)
)

/** ViewModel handling the behaviour of the Login screen. */
class LoginViewModel : ViewModel() {
  private val _state = MutableStateFlow(LoginScreenState())
  val state: StateFlow<LoginScreenState> = _state.asStateFlow()

  /**
   * Initialises the viewModel's state.
   *
   * @param context the application's context.
   */
  fun initialize(context: Context) {
    _state.value = LoginScreenState(isOnline = isOnline(context))
  }

  /**
   * Handles the actions after authenticating.
   *
   * @param result the result of the authentication.
   * @param navigationActions the handler for navigating the app.
   */
  fun onSignInResult(
      result: FirebaseAuthUIAuthenticationResult,
      navigationActions: NavigationActions
  ) {
    val response = result.idpResponse

    if (result.resultCode == Activity.RESULT_OK) {
      setOnline()
      navigationActions.navigateTo(TopLevelDestination(Routes.DatabaseLoadingScreen.routName))
    } else if (response != null) {
      throw Exception(response.error?.errorCode.toString())
    } else {
      throw Exception("Sign in failed")
    }
  }

  /**
   * Handles the case where a user tries entering the app while offline. If the user has previously
   * logged in, enter the app normally. If the user has never logged in, display an error message.
   *
   * @param context the application's context.
   * @param navigationActions the handler for navigating the app.
   */
  fun onEnterAppClicked(context: Context, navigationActions: NavigationActions) {
    val cacheCheck = getCachedInfo(context)

    if (cacheCheck != null) {
      navigationActions.navigateTo(TopLevelDestination(Routes.MainScreen.routName))
    } else {
      _state.value = _state.value.copy(showError = true)
    }
  }
}
