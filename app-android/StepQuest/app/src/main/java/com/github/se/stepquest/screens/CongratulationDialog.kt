package com.github.se.stepquest.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.se.stepquest.R

/**
 * Text box announcing the user has completed a goal (step goal, challenge...)
 *
 * @param titleText the category of the goal that was completed.
 * @param mainText the congratulatory text.
 * @param xpNumber the number of points awarded to the user.
 * @param onConfirm the action to execute when closing the box.
 */
@Composable
fun CongratulationDialog(
    titleText: String,
    mainText: String,
    xpNumber: Int,
    onConfirm: () -> Unit
) {
  val blueThemeColor = colorResource(id = R.color.blueTheme)

  Dialog(onDismissRequest = { onConfirm() }) {
    Surface(
        color = Color.White,
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(16.dp)) {
          Column(
              modifier = Modifier.padding(16.dp).fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = titleText, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = mainText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("main congratulation dialog text"))
                Spacer(modifier = Modifier.height(15.dp))
                Text(text = "You receive $xpNumber XP", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { onConfirm() },
                    colors = ButtonDefaults.buttonColors(blueThemeColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 4.dp).testTag("Confirm button")) {
                      Text(text = "Confirm")
                    }
              }
        }
  }
}
