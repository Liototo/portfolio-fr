package com.github.se.stepquest

open class Routes(val routName: String, val title: String) {
  object LoginScreen : Routes(routName = "/login", title = "Login")

  object DatabaseLoadingScreen : Routes(routName = "/databaseLoading", title = "DatabaseLoading")

  object NewPlayerScreen : Routes(routName = "/newPlayer", title = "NewPlayer")

  object HomeScreen : Routes(routName = "/home", title = "Home")

  object MapScreen : Routes(routName = "/map", title = "Map")

  object ProgressionScreen : Routes(routName = "/progression", title = "Progression")

  object MainScreen : Routes(routName = "/main", title = "Main")

  object ProfileScreen : Routes(routName = "/profile", title = "Profile")

  object FriendsListScreen : Routes(routName = "/friendsList", title = "FriendsList")

  object NotificationScreen : Routes(routName = "/notifications", title = "Notifications")

  object ChallengeScreen : Routes(routName = "/challenges", title = "Challenges")

  object Leaderboard : Routes(routName = "/leaderboard", title = "Leaderboard")
}
