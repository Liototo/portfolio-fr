package com.github.se.stepquest.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddFriendState(
    val searchQuery: String = "",
    val searchResults: List<String> = emptyList(),
    val loading: Boolean = false,
    val username: String? = null
)

/** ViewModel handling the behaviour of the AddFriend screen. */
class AddFriendViewModel : ViewModel() {
  private val _state = MutableStateFlow(AddFriendState())
  val state: StateFlow<AddFriendState> = _state

  private val database = FirebaseDatabase.getInstance()

  /**
   * Adds the current user's username to the viewModel.
   *
   * @param userId the current user's database ID.
   */
  fun fetchCurrentUser(userId: String) {
    viewModelScope.launch {
      getCurrentUser(database.reference, userId) { username ->
        _state.value = _state.value.copy(username = username)
      }
    }
  }

  /**
   * Updates the user search query.
   *
   * @param query the current query.
   */
  fun updateSearchQuery(query: String) {
    _state.value = _state.value.copy(searchQuery = query)
    performSearch(query)
  }

  /**
   * Finds usernames matching the search query in the database.
   *
   * @param query the current query.
   */
  private fun performSearch(query: String) {
    val lowercaseQuery = query.lowercase()
    _state.value = _state.value.copy(loading = true)

    val usersRef = database.reference.child("usernames")

    usersRef.addListenerForSingleValueEvent(
        object : ValueEventListener {
          override fun onDataChange(snapshot: DataSnapshot) {
            val results = mutableListOf<String>()
            val currentUser = _state.value.username?.lowercase()
            snapshot.children.forEach { userSnapshot ->
              val username = userSnapshot.key?.lowercase()
              if (username?.startsWith(lowercaseQuery) == true && username != currentUser) {
                results.add(userSnapshot.key!!)
              }
            }

            _state.value = _state.value.copy(searchResults = results, loading = false)
          }

          override fun onCancelled(error: DatabaseError) {
            _state.value = _state.value.copy(loading = false)
            // Handle error if needed
          }
        })
  }

  /**
   * Retrieves the current user's username from the database.
   *
   * @param database the database instance reference.
   * @param userId the current user's database ID.
   * @param callback the action to execute when the database connection is over.
   */
  private fun getCurrentUser(
      database: DatabaseReference,
      userId: String,
      callback: (String?) -> Unit
  ) {
    userId.let { uid ->
      database
          .child("users")
          .child(uid)
          .child("username")
          .addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                  val username = snapshot.getValue(String::class.java)
                  callback(username)
                }

                override fun onCancelled(error: DatabaseError) {
                  // Handle error
                  callback(null)
                }
              })
    }
  }
}
