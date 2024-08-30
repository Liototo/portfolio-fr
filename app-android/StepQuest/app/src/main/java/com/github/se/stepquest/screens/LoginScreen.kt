package com.github.se.stepquest.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.viewModels.LoginViewModel

/**
 * Screen to log in when entering the app. If online, enter the app normally. If offline, check for
 * cached login data.
 *
 * @param navigationActions the handler for navigating the app.
 * @param context the application's context.
 * @param loginViewModel the Login screen's viewModel.
 */
@Composable
fun LoginScreen(
    navigationActions: NavigationActions,
    context: Context,
    loginViewModel: LoginViewModel = viewModel()
) {
  val uiState by loginViewModel.state.collectAsState()
  LaunchedEffect(Unit) { loginViewModel.initialize(context) }

  val signInLauncher =
      rememberLauncherForActivityResult(contract = FirebaseAuthUIActivityResultContract()) {
        loginViewModel.onSignInResult(it, navigationActions)
      }

  val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())

  val signInIntent =
      AuthUI.getInstance()
          .createSignInIntentBuilder()
          .setAvailableProviders(providers)
          .setIsSmartLockEnabled(false)
          .build()

  Column(
      modifier = Modifier.padding(38.dp).fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Spacer(modifier = Modifier.height(100.dp))

        // Temporary until we have a logo
        val greyColor = Color(0xFF808080)
        Canvas(
            modifier =
                Modifier.align(Alignment.CenterHorizontally)
                    .size(200.dp)
                    .padding(vertical = 16.dp)
                    .testTag("App logo"),
            onDraw = {
              drawRect(color = greyColor, topLeft = Offset.Zero, size = Size(500f, 500f))
            })

        Spacer(modifier = Modifier.height(150.dp))

        if (uiState.isOnline) {
          Button(
              onClick = { signInLauncher.launch(signInIntent) },
              colors = ButtonDefaults.buttonColors(uiState.blueThemeColor),
              modifier = Modifier.fillMaxWidth().height(72.dp).padding(vertical = 8.dp),
              shape = RoundedCornerShape(8.dp)) {
                Text(text = "Authenticate", color = Color.White, fontSize = 24.sp)
              }
        } else {
          Button(
              onClick = { loginViewModel.onEnterAppClicked(context, navigationActions) },
              colors = ButtonDefaults.buttonColors(uiState.blueThemeColor),
              modifier = Modifier.fillMaxWidth().height(72.dp).padding(vertical = 8.dp),
              shape = RoundedCornerShape(8.dp)) {
                Text(text = "Enter the app", color = Color.White, fontSize = 24.sp)
              }
        }

        if (uiState.showError) {
          Text(
              text = "Internet connection required.",
              modifier = Modifier.fillMaxWidth(),
              color = Color.Red)
        }
      }
}
