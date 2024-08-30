package com.github.se.stepquest.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun DropDownMenu(
    suggestions: List<PlaceSuggestion>,
    onSuggestionSelected: (PlaceSuggestion) -> Unit
) {
  if (suggestions.isNotEmpty()) {
    Column(
        modifier =
            Modifier.background(Color.White).border(1.dp, Color.Gray).padding(4.dp).width(200.dp)) {
          suggestions.forEach { suggestion ->
            Text(
                text = suggestion.name,
                modifier =
                    Modifier.clickable { onSuggestionSelected(suggestion) }
                        .padding(8.dp)
                        .testTag("SuggestionItem"))
          }
        }
  }
}
