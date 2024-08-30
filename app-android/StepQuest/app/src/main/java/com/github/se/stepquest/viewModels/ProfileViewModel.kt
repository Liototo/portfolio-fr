package com.github.se.stepquest.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.se.stepquest.services.cacheTotalSteps
import com.github.se.stepquest.services.getCachedInfo
import com.github.se.stepquest.services.getCachedStepInfo
import com.github.se.stepquest.services.isOnline
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfilePageState(val totalStepsMade: Int = 0, val username: String = "No name")

/** ViewModel handling the behaviour of the profile page. */
class ProfilePageViewModel : ViewModel() {
  private val _state = MutableStateFlow(ProfilePageState())
  val state: StateFlow<ProfilePageState>
    get() = _state

  /**
   * Initialises the profile page.
   *
   * @param userId the current user's database ID.
   * @param context the application's context.
   */
  fun initialize(userId: String, context: Context) {
    viewModelScope.launch(Dispatchers.IO) {
      val stepList = getCachedStepInfo(context)
      _state.value = _state.value.copy(totalStepsMade = stepList["totalSteps"] ?: 0)

      if (isOnline(context)) {
        val database = Firebase.database
        val databaseRef = database.reference.child("users")
        val stepsRef = databaseRef.child(userId).child("totalSteps")
        stepsRef.addListenerForSingleValueEvent(
            object : ValueEventListener {
              override fun onDataChange(dataSnapshot: DataSnapshot) {
                val totalSteps = dataSnapshot.getValue(Int::class.java) ?: 0
                _state.value = _state.value.copy(totalStepsMade = totalSteps)
                cacheTotalSteps(context, totalSteps)
              }

              override fun onCancelled(databaseError: DatabaseError) {
                // add code when failing to access database
              }
            })
        val usernameRef = databaseRef.child(userId).child("username")

        usernameRef.addListenerForSingleValueEvent(
            object : ValueEventListener {
              override fun onDataChange(dataSnapshot: DataSnapshot) {
                val username = dataSnapshot.getValue(String::class.java) ?: "No name"
                _state.value = _state.value.copy(username = username)
              }

              override fun onCancelled(databaseError: DatabaseError) {
                // add code when failing to access database
              }
            })
      } else {
        val userInfo = getCachedInfo(context)
        if (userInfo != null) _state.value = _state.value.copy(username = userInfo.second)
      }
    }
  }
}
