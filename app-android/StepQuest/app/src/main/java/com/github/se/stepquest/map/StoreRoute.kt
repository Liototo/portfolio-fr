package com.github.se.stepquest.map

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

class StoreRoute {
  var firebaseAuth: FirebaseAuth
  var database: FirebaseDatabase
  var storage: FirebaseStorage

  init {
    firebaseAuth = FirebaseAuth.getInstance()
    database = FirebaseDatabase.getInstance()
    storage = Firebase.storage
  }

  data class Route(
      val route: List<LocationDetails>?,
      val checkpoints: List<CheckpointURL>? // Change to the correct checkpoints data type
  )

  data class GlobalRoute(
      val route: List<LocationDetails>?,
      val checkpoints: List<CheckpointURL>?, // Change to the correct checkpoints data type
      val userid: String
  )

  fun getUserid(): String? {
    return firebaseAuth.currentUser?.uid
  }

  fun addRoute(userId: String?, route: List<LocationDetails>?, checkpoints: List<Checkpoint>?) {
    if (userId != null) {
      val routeId = database.reference.child("routes").push().key.toString()
      val globalRouteRef = database.reference.child("routes").child(routeId)
      val routeRef =
          database.reference.child("users").child(userId).child("new_route").child(routeId)

      // Save image to the database and swap checkpoint's images with their URLs
      var checkpointsURL = emptyList<CheckpointURL>()
      val storageRef = storage.reference
      val uploadTasks = mutableListOf<Task<Uri>>()

      if (checkpoints != null) {
        for (checkpoint in checkpoints) {
          // If there is no image associated with the checkpoint, save an empty URL
          if (checkpoint.image.isNotEmpty()) {
            val imageId = database.reference.child("images").child(routeId).push().key.toString()
            val imageRef = storageRef.child("images/${routeId}/${imageId}.jpg")
            val uploadTask = imageRef.putBytes(checkpoint.image)
            val continueTask =
                uploadTask.continueWithTask { task ->
                  if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                  }
                  imageRef.downloadUrl
                }
            uploadTasks.add(continueTask)
          } else {
            val checkpointURL = CheckpointURL(checkpoint.name, checkpoint.location, "")
            checkpointsURL = checkpointsURL.plus(checkpointURL)
          }
        }

        Tasks.whenAllSuccess<Uri>(uploadTasks).addOnSuccessListener { urls ->
          for ((index, url) in urls.withIndex()) {
            val checkpointURL =
                CheckpointURL(checkpoints[index].name, checkpoints[index].location, url.toString())
            checkpointsURL = checkpointsURL.plus(checkpointURL)
          }
          val newroute = Route(route, checkpointsURL)
          val globalroute = GlobalRoute(route, checkpointsURL, userId)
          globalRouteRef.setValue(globalroute)
          routeRef.setValue(newroute)
        }
      }
    }
  }
}

data class CheckpointURL(
    val name: String,
    val location: LocationDetails,
    val imageURL: String? = null
)
