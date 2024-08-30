package com.github.se.stepquest.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.viewModels.NewPlayerViewModel

/**
 * Sign-up screen for new players.
 *
 * @param navigationActions the handler for navigating the app.
 * @param context the application's context.
 * @param userId the current user's database ID.
 * @param newPlayerViewModel the NewPlayer screen's viewModel.
 */
@Composable
fun NewPlayerScreen(
    navigationActions: NavigationActions,
    context: Context,
    userId: String,
    newPlayerViewModel: NewPlayerViewModel = viewModel()
) {

  val uiState by newPlayerViewModel.state.collectAsState()
  val blueThemeColor = colorResource(id = uiState.blueThemeColor)

  DisposableEffect(Unit) { onDispose {} }

  Column(
      modifier = Modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = uiState.username,
            onValueChange = { newPlayerViewModel.onUsernameChanged(it) },
            label = { Text("Username") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                      newPlayerViewModel.onSignInClicked(context, userId, navigationActions)
                    }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("username_input"))

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isUsernameAvailable) {
          Button(
              onClick = { newPlayerViewModel.onSignInClicked(context, userId, navigationActions) },
              colors = ButtonDefaults.buttonColors(blueThemeColor),
              modifier = Modifier.fillMaxWidth().height(72.dp).padding(vertical = 8.dp),
              shape = RoundedCornerShape(8.dp)) {
                Text(text = "Sign in", color = Color.White, fontSize = 24.sp)
              }
        } else {
          Text(
              text = "Username is not available",
              color = Color.Red,
              fontSize = 14.sp,
              modifier = Modifier.padding(vertical = 8.dp))
        }
      }
}
