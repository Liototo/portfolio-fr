import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.github.se.stepquest.map.LocationDetails
import com.github.se.stepquest.map.RouteProgression
import com.github.se.stepquest.map.calculateRouteLength
import com.github.se.stepquest.map.saveRoute
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class RouteProgressionKtTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displayCorrectText() {
    composeTestRule.setContent { RouteProgression({}, {}, 0f, 0) }
    composeTestRule.onNodeWithText("End Route").assertIsDisplayed()
    composeTestRule.onNodeWithText("Route name").assertExists()
    composeTestRule.onNodeWithText("Route name").assertIsDisplayed()
  }

  @Test
  fun displaysCorrectRouteLength() {
    val routeLength = 10f
    composeTestRule.setContent { RouteProgression({}, {}, routeLength, 0) }

    composeTestRule.onNodeWithText("Route length: $routeLength km").assertExists()
    composeTestRule.onNodeWithText("Route length: $routeLength km").assertIsDisplayed()
  }

  @Test
  fun displaysCorrectNumberOfCheckpoints() {
    val numCheckpoints = 5
    composeTestRule.setContent { RouteProgression({}, {}, 0f, numCheckpoints) }

    composeTestRule.onNodeWithText("Number of checkpoints: $numCheckpoints").assertExists()
    composeTestRule.onNodeWithText("Number of checkpoints: $numCheckpoints").assertIsDisplayed()
  }

  @Test
  fun displaysCorrectReward() {
    val routeLength = 10f
    val reward = routeLength.toInt().floorDiv(100)
    composeTestRule.setContent { RouteProgression({}, {}, routeLength, 0) }

    composeTestRule.onNodeWithText("Reward: $reward points").assertExists()
    composeTestRule.onNodeWithText("Reward: $reward points").assertIsDisplayed()
  }

  @Test
  fun displaysCloseButton() {
    composeTestRule.setContent { RouteProgression({}, {}, 0f, 0) }

    composeTestRule.onNodeWithContentDescription("Close").assertExists()
    composeTestRule.onNodeWithContentDescription("Close").assertIsDisplayed()
  }

  @Test
  fun displaysFinishButton() {
    composeTestRule.setContent { RouteProgression({}, {}, 0f, 0) }

    composeTestRule.onNodeWithText("Finish").assertExists()
    composeTestRule.onNodeWithText("Finish").assertIsDisplayed()
  }

  @Test
  fun dismissesDialog_onCloseButtonClick() {
    var dialogDismissed = true
    composeTestRule.setContent { RouteProgression({}, { dialogDismissed = false }, 0f, 0) }

    composeTestRule.onNodeWithContentDescription("Close").performClick()

    assert(!dialogDismissed)
  }

  @Test
  fun displaysExtraKilometersAndCheckpoints_forNextReward() {
    var extraKilometers = 0
    var extraCheckpoints = 0
    composeTestRule.setContent { RouteProgression({}, {}, 0f, 0) }

    composeTestRule
        .onNodeWithText(
            "$extraKilometers extra kilometers or $extraCheckpoints extra checkpoints for next reward")
        .assertExists()
    composeTestRule
        .onNodeWithText(
            "$extraKilometers extra kilometers or $extraCheckpoints extra checkpoints for next reward")
        .assertIsDisplayed()
  }

  @Test
  fun finishButtonIsDisabled_whenRouteNameIsEmpty() {
    composeTestRule.setContent { RouteProgression({}, {}, 0f, 0) }

    composeTestRule.onNodeWithText("Finish").assertIsNotEnabled()
  }

  @Test
  fun finishButtonIsEnabled_whenRouteNameIsNotEmpty() {
    composeTestRule.setContent { RouteProgression({}, {}, 0f, 0) }

    composeTestRule.onNodeWithText("Route name").performTextInput("Test Route")

    composeTestRule.onNodeWithText("Finish").assertIsEnabled()
  }

  @Test
  fun finishButtonIsDisabled_whenRouteNameIsCleared() {
    composeTestRule.setContent { RouteProgression({}, {}, 0f, 0) }

    composeTestRule.onNodeWithText("Route name").performTextInput("Test Route")
    composeTestRule.onNodeWithText("Test Route").performTextReplacement("")
    composeTestRule.onNodeWithText("Finish").assertIsNotEnabled()
  }

  @Test
  fun saveRoute_callsOnDismiss() {
    // Declare a boolean variable
    var isOnDismissCalled = false

    // Define a lambda that changes the variable's value
    val onDismiss = { isOnDismissCalled = true }

    // Other test data
    val routeName = "Test Route"
    val routeID = "Test ID"
    val routeLength = 10f
    val numCheckpoints = 5
    val reward = 500

    // Call the function with the test data
    saveRoute(onDismiss, routeName, routeID, routeLength, numCheckpoints, reward)

    // Check that the variable's value has changed
    assertTrue(isOnDismissCalled)
  }

  @Test
  fun calculateRouteLength_returnsZeroForSingleLocation() {
    val locations = listOf(LocationDetails(0.0, 0.0))

    val result = calculateRouteLength(locations)

    assertEquals(0f, result, 0.001f)
  }

  @Test
  fun calculateRouteLength_returnsZeroForEmptyList() {
    val locations = emptyList<LocationDetails>()

    val result = calculateRouteLength(locations)

    assertEquals(0f, result, 0.001f)
  }

  @Test
  fun finishButton_onClick_callsSaveRoute() {
    var isSaveRouteCalled = false

    // Mock the saveRoute function to update the flag when called
    val mockSaveRoute: (() -> Unit, String, String, Float, Int, Int) -> Unit = { _, _, _, _, _, _ ->
      isSaveRouteCalled = true
    }

    val stopRoute = {} // Define stopRoute as an empty lambda function
    val routeName = "Test Route" // Define routeName as a string
    val routeID = "Test ID" // Define routeID as a string
    val routeLength = 10f // Define routeLength as a float
    val numCheckpoints = 5 // Define numCheckpoints as an integer
    val reward = 500 // Define reward as an integer

    composeTestRule.setContent {
      RouteProgression(
          stopRoute = {
            mockSaveRoute(stopRoute, routeName, routeID, routeLength, numCheckpoints, reward)
          },
          {},
          0f,
          0)
    }

    // Input text into the text field
    composeTestRule.onNodeWithText("Route name").performTextInput(routeName)

    // Perform a click on the "Finish" button
    composeTestRule.onNodeWithText("Finish").performClick()

    // Check if the saveRoute function was called
    assertTrue(isSaveRouteCalled)
  }
}
