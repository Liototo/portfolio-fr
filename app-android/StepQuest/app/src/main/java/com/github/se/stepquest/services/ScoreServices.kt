package com.github.se.stepquest.services

import android.util.Log
import com.github.se.stepquest.Friend
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

fun getTopLeaderboard(top: Int, callback: (List<Pair<String, Int>>?) -> Unit) {
  val database = FirebaseDatabase.getInstance()
  val leaderboardRef = database.reference.child("leaderboard")
  leaderboardRef
      .orderByValue()
      .limitToLast(top)
      .addListenerForSingleValueEvent(
          object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
              val leaderboard = mutableListOf<Pair<String, Int>>()
              for (snapshot in dataSnapshot.getChildren()) {
                val username = snapshot.key ?: continue
                val score = snapshot.getValue(Int::class.java) ?: continue
                leaderboard.add(Pair(username, score))
              }
              leaderboard.sortByDescending { it.second }
              callback(leaderboard)
            }

            override fun onCancelled(error: DatabaseError) {
              callback(emptyList())
            }
          })
}

fun getFriendsLeaderboard(
    currentFriendsList: List<Friend>,
    callback: (List<Pair<String, Int>>?) -> Unit
) {
  val database = FirebaseDatabase.getInstance()
  val leaderboardRef = database.reference.child("leaderboard")
  val friendsScores = mutableListOf<Pair<String, Int>>()
  if (currentFriendsList.isEmpty()) {
    callback(emptyList())
    return
  }
  for (friend in currentFriendsList) {
    val userRef = leaderboardRef.child(friend.name)
    userRef.addListenerForSingleValueEvent(
        object : ValueEventListener {
          override fun onDataChange(dataSnapshot: DataSnapshot) {
            val score = dataSnapshot.getValue(Int::class.java) ?: 0
            friendsScores.add(Pair(friend.name, score))
            friendsScores.sortByDescending { it.second }
            callback(friendsScores)
          }

          override fun onCancelled(databaseError: DatabaseError) {
            callback(emptyList())
          }
        })
  }
}

fun getUserScore(username: String, callback: (Int) -> Unit) {
  val database = FirebaseDatabase.getInstance()
  val leaderboardRef = database.reference.child("leaderboard").child(username)
  leaderboardRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          val score = dataSnapshot.getValue(Int::class.java) ?: 0
          callback(score)
        }

        override fun onCancelled(error: DatabaseError) {
          callback(0)
        }
      })
}

fun getUserPlacement(username: String, callback: (Int?) -> Unit) {
  val database = FirebaseDatabase.getInstance()
  val leaderboardRef = database.reference.child("leaderboard")

  // Retrieve the leaderboard data
  leaderboardRef
      .orderByValue()
      .addListenerForSingleValueEvent(
          object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
              val leaderboard = mutableListOf<Pair<String, Int>>()

              // Iterate through the leaderboard data to populate the list
              for (snapshot in dataSnapshot.children) {
                val playerName = snapshot.key ?: continue
                val playerScore = snapshot.getValue(Int::class.java) ?: continue
                leaderboard.add(Pair(playerName, playerScore))
              }

              // Sort the leaderboard by score in descending order
              leaderboard.sortByDescending { it.second }

              // Find the index of the player in the sorted leaderboard
              val playerIndex = leaderboard.indexOfFirst { it.first == username }

              // If the player is found in the leaderboard, calculate their placement
              val placement = if (playerIndex != -1) playerIndex + 1 else null

              // Invoke the callback with the player's placement
              callback(placement)
            }

            override fun onCancelled(error: DatabaseError) {
              // Handle the error if the retrieval is canceled
              callback(0)
            }
          })
}

fun checkUserExistsOnLeaderboard(username: String) {
  val database = FirebaseDatabase.getInstance()
  val leaderboardRef = database.reference.child("leaderboard")

  // Check if the user exists in the leaderboard
  leaderboardRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          if (!dataSnapshot.hasChild(username)) {
            leaderboardRef.child(username).setValue(0)
          }
        }

        override fun onCancelled(error: DatabaseError) {}
      })
}

fun addPoints(
    username: String,
    points: Int,
    database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {

  val userPointsRef = database.reference.child("leaderboard").child(username)

  userPointsRef.addListenerForSingleValueEvent(
      object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {

          val curr = snapshot.getValue(Int::class.java) ?: 0
          val newPoints = curr + points
          userPointsRef.setValue(newPoints)
        }

        override fun onCancelled(error: DatabaseError) {
          Log.e("ScoreServices", "Database error: ${error.message}")
        }
      })
}
