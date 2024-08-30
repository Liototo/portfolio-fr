package com.github.se.stepquest.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.text.format.DateFormat
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StepCounterService(
    private var sensorManager: SensorManager? = null,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val userId: String? = firebaseAuth.currentUser?.uid,
    private var context: Context? = null
) : Service(), SensorEventListener {

  private var stepSensor: Sensor? = null

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  override fun onCreate() {
    super.onCreate()

    if (sensorManager == null) {
      sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    }

    if (context == null) context = applicationContext

    stepSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    if (sensorManager != null) {
      sensorManager!!.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    if (userId != null) {
      cleanUpOldSteps(userId)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    sensorManager!!.unregisterListener(this)
  }

  override fun onSensorChanged(event: SensorEvent?) {

    if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {

      if (isOnline(context!!)) {
        val cachedSteps = getCachedSteps(context!!)

        saveStepCountToDatabase(cachedSteps + 1)

        if (cachedSteps > 0) deleteCachedSteps(context!!)
      } else {
        saveStepLocally(context!!)
      }
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

  private fun saveStepCountToDatabase(newSteps: Int) {

    if (userId != null) {
      val stepsRefTotal = database.reference.child("users").child(userId).child("totalSteps")
      stepsRefTotal.addListenerForSingleValueEvent(
          object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
              val currentSteps = dataSnapshot.getValue(Int::class.java) ?: 0
              val totalSteps = currentSteps + newSteps
              stepsRefTotal.setValue(totalSteps)
            }

            override fun onCancelled(databaseError: DatabaseError) {
              Log.e("StepCounterService", "Database error: ${databaseError.message}")
            }
          })

      val d = Date()
      val s: CharSequence = DateFormat.format("MMMM d, yyyy ", d.time)
      val stepsRef = database.reference.child("users").child(userId).child("dailySteps $s")
      stepsRef.addListenerForSingleValueEvent(
          object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
              val currentSteps = dataSnapshot.getValue(Int::class.java) ?: 0
              val totalSteps = currentSteps + newSteps
              stepsRef.setValue(totalSteps)

              val dailyStepGoal = (getCachedStepInfo(context!!)["dailyStepGoal"] ?: 5000)

              if (totalSteps >= dailyStepGoal) {

                awardPoints(dailyStepGoal, s)
              }
            }

            override fun onCancelled(databaseError: DatabaseError) {
              Log.e("StepCounterService", "Database error: ${databaseError.message}")
            }
          })

      // Store weekly steps
      val calendar = Calendar.getInstance()
      // Set the calendar to the current date
      calendar.time = d
      // Find the start of the week (Monday)
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
      val startOfWeek = calendar.time
      // Find the end of the week (Sunday)
      calendar.add(Calendar.DAY_OF_WEEK, 6)
      val endOfWeek = calendar.time
      // Format the dates
      val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
      val startFormatted = dateFormat.format(startOfWeek)
      val endFormatted = dateFormat.format(endOfWeek)
      // Combine formatted dates
      val current_period = "$startFormatted - $endFormatted"

      val stepsRefWeek =
          database.reference.child("users").child(userId).child("weeklySteps $current_period")
      stepsRefWeek.addListenerForSingleValueEvent(
          object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
              val currentSteps = dataSnapshot.getValue(Int::class.java) ?: 0
              val totalSteps = currentSteps + newSteps
              stepsRefWeek.setValue(totalSteps)
            }

            override fun onCancelled(databaseError: DatabaseError) {
              Log.e("StepCounterService", "Database error: ${databaseError.message}")
            }
          })
    }
  }

  private fun awardPoints(dailyStepGoal: Int, date: CharSequence) {

    if (userId == null) return

    val hasReceivedRef =
        database.reference.child("users").child(userId).child("hasReceivedPoints $date")

    hasReceivedRef.addListenerForSingleValueEvent(
        object : ValueEventListener {

          override fun onDataChange(snapshot: DataSnapshot) {
            val hasReceivedPoints = snapshot.getValue(Boolean::class.java) ?: false

            if (!hasReceivedPoints) {

              val earnedPoints = dailyStepGoal.floorDiv(100)

              getUsername(userId) { addPoints(it, earnedPoints) }

              hasReceivedRef.setValue(true)
            }
          }

          override fun onCancelled(databaseError: DatabaseError) {
            Log.e("StepCounterService", "Database error: ${databaseError.message}")
          }
        })
  }

  fun cleanUpOldSteps(userId: String) {
    // clean up old daily steps
    val userRef = database.reference.child("users").child(userId)
    val d = Date()
    val s: CharSequence = DateFormat.format("MMMM d, yyyy ", d.getTime())
    val calendar = Calendar.getInstance()
    // Set the calendar to the current date
    calendar.time = d
    // Find the start of the week (Monday)
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val startOfWeek = calendar.time
    // Find the end of the week (Sunday)
    calendar.add(Calendar.DAY_OF_WEEK, 6)
    val endOfWeek = calendar.time
    // Format the dates
    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val startFormatted = dateFormat.format(startOfWeek)
    val endFormatted = dateFormat.format(endOfWeek)
    // Combine formatted dates
    val current_period = "$startFormatted - $endFormatted"

    userRef.addListenerForSingleValueEvent(
        object : ValueEventListener {
          override fun onDataChange(dataSnapshot: DataSnapshot) {
            for (child in dataSnapshot.children) {
              val nodeName = child.key
              if (nodeName != null &&
                  nodeName.contains("dailySteps") &&
                  nodeName != "dailySteps $s") {
                userRef.child(nodeName).removeValue()
              }

              if (nodeName != null &&
                  nodeName.contains("weeklySteps") &&
                  nodeName != "weeklySteps $current_period") {
                userRef.child(nodeName).removeValue()
              }

              if (nodeName != null &&
                  nodeName.contains("hasReceivedPoints") &&
                  nodeName != "hasReceivedPoints $s") {
                userRef.child(nodeName).removeValue()
              }
            }
          }

          override fun onCancelled(databaseError: DatabaseError) {
            // Handle error
          }
        })
  }
}
