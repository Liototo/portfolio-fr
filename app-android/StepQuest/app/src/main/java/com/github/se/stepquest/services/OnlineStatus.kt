package com.github.se.stepquest.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

fun setOnline() {

  val firebaseAuth = FirebaseAuth.getInstance()
  val database = FirebaseDatabase.getInstance()
  val userId = firebaseAuth.currentUser?.uid ?: return

  val userStatusRef = database.reference.child("users").child(userId).child("online")

  userStatusRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          userStatusRef.setValue(true)
          userStatusRef.onDisconnect().setValue(false)
        }

        override fun onCancelled(error: DatabaseError) {
          // add code when failing to access database
        }
      })
}
