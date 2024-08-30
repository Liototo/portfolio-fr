import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.se.stepquest.R
import com.github.se.stepquest.viewModels.StepGoalsViewModel

/**
 * Screen to set the user's step goals.
 *
 * @param onDismiss the action to execute once the update is done.
 * @param onConfirm the action to execute if the user confirms.
 * @param viewModel the StepGoal screen's viewModel.
 */
@Composable
fun SetStepGoalsDialog(
    onDismiss: () -> Unit,
    onConfirm: (dailyStepGoal: Int, weeklyStepGoal: Int) -> Unit,
    viewModel: StepGoalsViewModel = viewModel()
) {
  val state by viewModel.state.collectAsState()
  val blueThemeColor = colorResource(id = R.color.blueTheme)

  Dialog(onDismissRequest = { onDismiss() }) {
    Surface(
        color = Color.White,
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(16.dp)) {
          Column(
              modifier = Modifier.padding(16.dp).fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(text = "Set New Step Goals", fontSize = 20.sp)
                      IconButton(onClick = { onDismiss() }, modifier = Modifier.padding(8.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                      }
                    }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Daily steps")
                Spacer(modifier = Modifier.height(2.dp))
                TextField(
                    modifier = Modifier.testTag("daily_steps_setter"),
                    value = state.newDailyStepGoal,
                    onValueChange = {
                      viewModel.updateDailyStepGoal(it.filter { it.isDigit() }.take(5))
                    },
                    label = { Text("Enter your daily step goal") },
                    placeholder = { Text("5000") },
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions =
                        KeyboardActions(
                            onDone = { viewModel.calculateAndConfirmGoals(onConfirm, onDismiss) }))
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.calculateAndConfirmGoals(onConfirm, onDismiss) },
                    colors = ButtonDefaults.buttonColors(blueThemeColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)) {
                      Text(text = "Confirm")
                    }
              }
        }
  }
}
