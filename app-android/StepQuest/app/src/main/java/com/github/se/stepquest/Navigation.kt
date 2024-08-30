package com.github.se.stepquest

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.se.stepquest.map.LocationViewModel
import com.github.se.stepquest.map.Map
import com.github.se.stepquest.screens.ChallengesScreen
import com.github.se.stepquest.screens.DatabaseLoadingScreen
import com.github.se.stepquest.screens.FriendsListScreenCheck
import com.github.se.stepquest.screens.HomeScreen
import com.github.se.stepquest.screens.Leaderboards
import com.github.se.stepquest.screens.LoginScreen
import com.github.se.stepquest.screens.NewPlayerScreen
import com.github.se.stepquest.screens.NotificationScreen
import com.github.se.stepquest.screens.ProfilePageLayout
import com.github.se.stepquest.screens.ProgressionPage
import com.github.se.stepquest.services.StepCounterService
import com.github.se.stepquest.ui.navigation.NavigationActions
import com.github.se.stepquest.ui.navigation.TopLevelDestination
import com.google.firebase.auth.FirebaseAuth

@Composable
fun BuildNavigationBar(navigationController: NavHostController) {
  val screens =
      listOf(
          Routes.HomeScreen,
          Routes.MapScreen,
          Routes.ProgressionScreen,
      )
  val navigationBackStack by navigationController.currentBackStackEntryAsState()
  val currentPage = navigationBackStack?.destination?.route
  val navigationActions = remember(navigationController) { NavigationActions(navigationController) }

  NavigationBar(containerColor = Color.White, contentColor = Color.Black) {
    screens.forEach { screen ->
      NavigationBarItem(
          label = { Text(text = screen.title, fontSize = 16.sp) },
          selected = currentPage == screen.routName,
          onClick = {
            navigationActions.navigateTo(destination = TopLevelDestination(screen.routName))
          },
          icon = { Icons.Filled.Home },
          colors =
              NavigationBarItemDefaults.colors(
                  unselectedTextColor = Color.Black,
                  selectedTextColor = colorResource(id = R.color.blueTheme)),
      )
    }
  }
}

@Composable
fun AppNavigationHost(
    modifier: Modifier = Modifier,
    navigationController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LoginScreen.routName,
    locationviewModel: LocationViewModel = remember { LocationViewModel() }
) {
  val navigationActions = remember(navigationController) { NavigationActions(navigationController) }
  val context = LocalContext.current
  val firebaseAuth = FirebaseAuth.getInstance()
  var userId = firebaseAuth.currentUser?.uid
  val profilePictureUrl = firebaseAuth.currentUser?.photoUrl
  val startServiceLambda: () -> Unit = {
    context.startService(Intent(context, StepCounterService::class.java))
  }
  if (userId == null) {
    userId = "testUserId"
  }
  NavHost(
      modifier = modifier,
      navController = navigationController,
      startDestination = startDestination) {
        composable(Routes.LoginScreen.routName) { LoginScreen(navigationActions, context) }
        composable(Routes.DatabaseLoadingScreen.routName) {
          DatabaseLoadingScreen(navigationActions, startServiceLambda, userId)
        }
        composable(Routes.NewPlayerScreen.routName) {
          NewPlayerScreen(navigationActions, context, userId)
        }
        composable(Routes.MainScreen.routName) { BuildMainScreen() }
        composable(Routes.HomeScreen.routName) { HomeScreen(navigationActions, userId, context) }
        composable(Routes.ProgressionScreen.routName) {
          ProgressionPage(IUserRepository(), context)
        }
        composable(Routes.HomeScreen.routName) { HomeScreen(navigationActions, userId, context) }
        composable(Routes.ProgressionScreen.routName) {
          ProgressionPage(IUserRepository(), context)
        }
        composable(Routes.MapScreen.routName) { Map(locationviewModel) }
        composable(Routes.ProfileScreen.routName) {
          ProfilePageLayout(navigationActions, userId, profilePictureUrl, context)
        }
        composable(Routes.FriendsListScreen.routName) {
          FriendsListScreenCheck(navigationActions = navigationActions, userId, context = context)
        }
        composable(Routes.NotificationScreen.routName) { NotificationScreen(IUserRepository()) }
        composable(Routes.ChallengeScreen.routName) { ChallengesScreen(userId, navigationActions) }
        composable(Routes.Leaderboard.routName) { Leaderboards(userId, navigationActions) }
      }
}

@Composable
fun BuildMainScreen() {
  val navigationController: NavHostController = rememberNavController()
  Scaffold(bottomBar = { BuildNavigationBar(navigationController = navigationController) }) {
      paddingValues ->
    Box(modifier = Modifier.padding(paddingValues)) {
      AppNavigationHost(
          navigationController = navigationController,
          startDestination = Routes.HomeScreen.routName)
    }
  }
}
