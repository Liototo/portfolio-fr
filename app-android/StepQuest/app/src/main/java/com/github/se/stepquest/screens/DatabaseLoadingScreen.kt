package com.github.se.stepquest.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.viewModels.DatabaseLoadingViewModel

/**
 * Screen displayed when the app is waiting for database results.
 *
 * @param navigationActions the handler for navigating the app.
 * @param startService the service to start afterwards.
 * @param userId the current user's database ID.
 * @param viewModel the DatabaseLoading screen's viewModel.
 */
@Composable
fun DatabaseLoadingScreen(
    navigationActions: NavigationActions,
    startService: () -> Unit,
    userId: String,
    viewModel: DatabaseLoadingViewModel = viewModel()
) {
  val state by viewModel.state.collectAsState()

  val launcherPermission =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
          permissions ->
        viewModel.updatePermissions(permissions)
      }

  LaunchedEffect(Unit) {
    viewModel.checkUser(userId, navigationActions, startService)
    if (!state.permissionsGranted) {
      launcherPermission.launch(
          arrayOf(Manifest.permission.BODY_SENSORS, Manifest.permission.ACTIVITY_RECOGNITION))
    }
  }

  Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Waiting for database...",
            modifier = Modifier.padding(32.dp),
            fontWeight = FontWeight.Bold)
      }
}
