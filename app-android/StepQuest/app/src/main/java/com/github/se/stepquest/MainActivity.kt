package com.github.se.stepquest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.github.se.stepquest.map.FollowRoute
import com.github.se.stepquest.ui.theme.StepQuestTheme

class MainActivity : ComponentActivity() {
  private lateinit var followRoute: FollowRoute

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      StepQuestTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          AppNavigationHost()
        }
      }
    }
    followRoute = FollowRoute.getInstance()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == FollowRoute.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      followRoute.onPictureTaken()
    }
  }
}
