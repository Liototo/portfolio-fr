package com.github.se.stepquest

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test

class NavigationTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun navigation_bar_correctly_built() {

    composeTestRule.setContent { BuildNavigationBar(rememberNavController()) }
  }
}
