package com.github.se.stepquest.services

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

fun getUsername(userId: String, callback: (String) -> Unit) {
  var username: String
  val database = FirebaseDatabase.getInstance()
  val usernamesRef = database.reference.child("users").child(userId).child("username")
  usernamesRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          username = snapshot.getValue(String::class.java).toString()
          callback(username)
        }

        override fun onCancelled(error: DatabaseError) {
          callback("")
        }
      })
}

fun getUserId(username: String, callback: (String) -> Unit) {
  var uid: String?
  val database = FirebaseDatabase.getInstance()
  val usernamesRef = database.reference.child("usernames").child(username)
  usernamesRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          uid = snapshot.getValue(String::class.java)
          uid?.let { callback(it) }
        }

        override fun onCancelled(error: DatabaseError) {
          callback("")
        }
      })
}
