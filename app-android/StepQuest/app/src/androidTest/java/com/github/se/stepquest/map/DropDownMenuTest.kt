package com.github.se.stepquest.map

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DropDownMenuTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testSuggestions =
      listOf(
          PlaceSuggestion("Place 1", "1"),
          PlaceSuggestion("Place 2", "2"),
          PlaceSuggestion("Place 3", "3"))

  @Test
  fun dropDownMenuDisplaysSuggestions() {
    composeTestRule.setContent {
      DropDownMenu(suggestions = testSuggestions, onSuggestionSelected = {})
    }

    testSuggestions.forEach { suggestion ->
      composeTestRule.onNodeWithText(suggestion.name).assertExists()
    }
  }

  @Test
  fun dropDownMenuCallsOnSuggestionSelected() {
    var selectedSuggestion: PlaceSuggestion? = null
    composeTestRule.setContent {
      DropDownMenu(
          suggestions = testSuggestions, onSuggestionSelected = { selectedSuggestion = it })
    }

    val suggestionToSelect = testSuggestions[1]
    composeTestRule.onNodeWithText(suggestionToSelect.name).performClick()

    assert(selectedSuggestion == suggestionToSelect)
  }

  @Test
  fun dropDownMenuIsEmptyWhenNoSuggestions() {
    composeTestRule.setContent {
      DropDownMenu(suggestions = emptyList(), onSuggestionSelected = {})
    }

    composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(0)
  }
}
