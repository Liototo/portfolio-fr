package com.github.se.stepquest.services

import android.content.Context
import android.net.ConnectivityManager
import com.github.se.stepquest.map.LocationDetails
import com.github.se.stepquest.map.RouteDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun isOnline(context: Context): Boolean {

  val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  val networkInfo = manager.activeNetworkInfo

  return networkInfo != null && networkInfo.isConnected
}

fun cacheUserInfo(context: Context, userId: String, username: String) {

  val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)

  val editor = sharedPreferences.edit()
  editor.putString("userId", userId)
  editor.putString("username", username)
  editor.apply()
}

fun getCachedInfo(context: Context): Pair<String, String>? {

  val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)

  val userId = sharedPreferences.getString("userId", null)
  val username = sharedPreferences.getString("username", null)

  return if (userId != null && username != null) Pair(userId, username) else null
}

fun saveStepLocally(context: Context) {
  val sharedPreferences = context.getSharedPreferences("StepCounter", Context.MODE_PRIVATE)
  val cachedSteps = sharedPreferences.getInt("cachedSteps", 0)
  val editor = sharedPreferences.edit()
  editor.putInt("cachedSteps", cachedSteps + 1)
  editor.apply()
}

fun getCachedSteps(context: Context): Int {

  val sharedPreferences = context.getSharedPreferences("StepCounter", Context.MODE_PRIVATE)

  return sharedPreferences.getInt("cachedSteps", 0)
}

fun deleteCachedSteps(context: Context) {
  val sharedPreferences = context.getSharedPreferences("StepCounter", Context.MODE_PRIVATE)

  val editor = sharedPreferences.edit()
  editor.remove("cachedSteps")
  editor.apply()
}

fun cacheStepGoals(context: Context, dailyStepGoal: Int, weeklyStepGoal: Int) {
  val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
  val editor = sharedPreferences.edit()
  editor.putInt("dailyStepGoal", dailyStepGoal)
  editor.putInt("weeklyStepGoal", weeklyStepGoal)
  editor.apply()
}

fun cacheDailyWeeklySteps(context: Context, dailySteps: Int, weeklySteps: Int) {
  val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
  val editor = sharedPreferences.edit()
  editor.putInt("dailySteps", dailySteps)
  editor.putInt("weeklySteps", weeklySteps)
  editor.apply()
}

fun cacheTotalSteps(context: Context, totalSteps: Int) {
  val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
  val editor = sharedPreferences.edit()
  editor.putInt("totalSteps", totalSteps)
  editor.apply()
}

fun getCachedStepInfo(context: Context): Map<String, Int> {
  val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
  val dailyStepGoal = sharedPreferences.getInt("dailyStepGoal", 5000) // Default value is 5000
  val dailySteps = sharedPreferences.getInt("dailySteps", 0) // Default value is 0
  val weeklyStepGoal = sharedPreferences.getInt("weeklyStepGoal", 35000) // Default value is 35000
  val weeklySteps = sharedPreferences.getInt("weeklySteps", 0) // Default value is 0
  val totalSteps = sharedPreferences.getInt("totalSteps", 0) // Default value is 0

  return mapOf(
      "dailyStepGoal" to dailyStepGoal,
      "dailySteps" to dailySteps,
      "weeklyStepGoal" to weeklyStepGoal,
      "weeklySteps" to weeklySteps,
      "totalSteps" to totalSteps)
}

fun cacheRouteData(
    context: Context,
    routeList: List<LocationDetails>,
    routeDetailList: List<RouteDetails>
) {
  val sharedPreferences = context.getSharedPreferences("RouteData", Context.MODE_PRIVATE)
  val editor = sharedPreferences.edit()
  val gson = Gson()

  // Convert the lists to JSON strings
  val routeListJson = gson.toJson(routeList)
  val routeDetailListJson = gson.toJson(routeDetailList)

  // Save the JSON strings in SharedPreferences
  editor.putString("routeList", routeListJson)
  editor.putString("routeDetailList", routeDetailListJson)
  editor.apply()
}

fun getcacheRouteData(context: Context): Pair<List<LocationDetails>, List<RouteDetails>> {
  val sharedPreferences = context.getSharedPreferences("RouteData", Context.MODE_PRIVATE)
  val gson = Gson()

  // Get the JSON strings from SharedPreferences
  val routeListJson = sharedPreferences.getString("routeList", null)
  val routeDetailListJson = sharedPreferences.getString("routeDetailList", null)

  // Convert the JSON strings back to lists
  val routeListType = object : TypeToken<List<LocationDetails>>() {}.type
  val routeDetailListType = object : TypeToken<List<RouteDetails>>() {}.type

  val routeList: List<LocationDetails> = gson.fromJson(routeListJson, routeListType) ?: emptyList()
  val routeDetailList: List<RouteDetails> =
      gson.fromJson(routeDetailListJson, routeDetailListType) ?: emptyList()

  return Pair(routeList, routeDetailList)
}
