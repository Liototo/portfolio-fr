package com.github.se.stepquest

import android.text.format.DateFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date

interface UserRepository {
  fun getUid(): String?

  fun getSteps(callback: (List<Int>) -> Unit)

  fun getDailyStepsForDate(date: Date, callback: (Int) -> Unit)
}

class IUserRepository : UserRepository {
  val firebaseAuth = FirebaseAuth.getInstance()
  val userId = firebaseAuth.currentUser?.uid
  val database = FirebaseDatabase.getInstance()

  override fun getUid(): String? {
    return this.userId
  }

  override fun getSteps(callback: (List<Int>) -> Unit) {
    var list = listOf(0, 0)
    val d = Date()
    val s: CharSequence = DateFormat.format("MMMM d, yyyy ", d.getTime())
    val stepsRef = database.reference.child("users").child(userId!!).child("dailySteps $s")
    stepsRef.addListenerForSingleValueEvent(
        object : ValueEventListener {
          override fun onDataChange(dataSnapshot: DataSnapshot) {
            list =
                listOf(
                    dataSnapshot.getValue(Int::class.java) ?: 0,
                    dataSnapshot.getValue(Int::class.java) ?: 0)
            callback(list)
          }

          override fun onCancelled(databaseError: DatabaseError) {
            callback(list)
          }
        })
  }

  override fun getDailyStepsForDate(date: Date, callback: (Int) -> Unit) {
    if (date > Date()) {
      callback(0)
      return
    }
    var dailySteps = 0
    val d = date
    val s: CharSequence = DateFormat.format("MMMM d, yyyy ", d.time)
    val stepsRef = database.reference.child("users").child(userId!!).child("dailySteps $s")
    stepsRef.addListenerForSingleValueEvent(
        object : ValueEventListener {
          override fun onDataChange(dataSnapshot: DataSnapshot) {
            dailySteps = dataSnapshot.getValue(Int::class.java) ?: 0
            callback(dailySteps)
          }

          override fun onCancelled(databaseError: DatabaseError) {
            callback(dailySteps)
          }
        })
  }
}

class TestUserRepository1 : UserRepository {
  private val uid = "testUser2"
  private val dailyStepsMade = 2500
  private val weeklyStepsMade = 6500

  override fun getUid(): String {
    return this.uid
  }

  override fun getSteps(callback: (List<Int>) -> Unit) {
    return callback(listOf(dailyStepsMade, weeklyStepsMade))
  }

  override fun getDailyStepsForDate(date: Date, callback: (Int) -> Unit) {
    return callback(dailyStepsMade)
  }
}

class TestUserRepository2 : UserRepository {
  private val uid = "testuid"
  private val dailyStepsMade = 6000
  private val weeklyStepsMade = 17500

  override fun getUid(): String {
    return this.uid
  }

  override fun getSteps(callback: (List<Int>) -> Unit) {
    return callback(listOf(dailyStepsMade, weeklyStepsMade))
  }

  override fun getDailyStepsForDate(date: Date, callback: (Int) -> Unit) {
    return callback(dailyStepsMade)
  }
}
