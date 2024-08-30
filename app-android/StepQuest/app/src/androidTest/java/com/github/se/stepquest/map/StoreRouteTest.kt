package com.github.se.stepquest.map

import android.net.Uri
import androidx.test.runner.screenshot.Screenshot.capture
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StoreRouteTest {
  private lateinit var storeRoute: StoreRoute
  private lateinit var database: FirebaseDatabase

  @Before
  fun setup() {
    // Initialize StoreRoute
    val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    val firebaseDatabase = mockk<FirebaseDatabase>(relaxed = true)
    val firebaseStorage = mockk<FirebaseStorage>(relaxed = true)

    storeRoute =
        StoreRoute().apply {
          this.firebaseAuth = firebaseAuth
          this.database = firebaseDatabase
          this.storage = firebaseStorage
        }
  }

  @Test
  fun testRouteCreation() {
    val routeDetails =
        listOf(LocationDetails(1.0, 2.0), LocationDetails(3.0, 4.0), LocationDetails(5.0, 6.0))
    val checkpoints =
        listOf(
            CheckpointURL("Checkpoint 1", routeDetails[0]),
            CheckpointURL("Checkpoint 2", routeDetails[1]))
    val route = StoreRoute.Route(routeDetails, checkpoints)
    assertEquals(routeDetails, route.route)
    assertEquals(checkpoints, route.checkpoints)
  }

  @Test
  fun testGlobalRouteCreation() {
    val routeDetails =
        listOf(LocationDetails(1.0, 2.0), LocationDetails(3.0, 4.0), LocationDetails(5.0, 6.0))
    val checkpoints =
        listOf(
            CheckpointURL("Checkpoint 1", routeDetails[0]),
            CheckpointURL("Checkpoint 2", routeDetails[1]))
    val userId = "testUserId"
    val globalRoute = StoreRoute.GlobalRoute(routeDetails, checkpoints, userId)
    assertEquals(routeDetails, globalRoute.route)
    assertEquals(checkpoints, globalRoute.checkpoints)
    assertEquals(userId, globalRoute.userid)
  }

  @Test
  fun testAddRoute() {
    val userId = "testUserId"
    val route = listOf(LocationDetails(1.0, 2.0), LocationDetails(3.0, 4.0))
    val checkpoints =
        listOf(Checkpoint("Checkpoint 1", route[0]), Checkpoint("Checkpoint 2", route[1]))
    val database = mockk<FirebaseDatabase>(relaxed = true)
    every { database.reference } returns mockk(relaxed = true)
    storeRoute.addRoute(userId, route, checkpoints)
  }

  @Test
  fun addRoute_uploadsImagesAndUpdatesCheckpointURLs_whenCheckpointsAreNotNull() {
    val userId = "testUserId"
    val route = listOf(LocationDetails(1.0, 2.0), LocationDetails(3.0, 4.0))
    val checkpoints =
        listOf(
            Checkpoint("Checkpoint 1", route[0], "image1".toByteArray()),
            Checkpoint("Checkpoint 2", route[1], "image2".toByteArray()))

    val database = storeRoute.database
    val storage = storeRoute.storage
    val storageReference = mockk<StorageReference>(relaxed = true)
    val uploadTask = mockk<UploadTask>(relaxed = true)
    val downloadUrlTask = Tasks.forResult(Uri.parse("http://fake.url"))

    every { database.reference } returns mockk(relaxed = true)
    every { storage.reference } returns storageReference
    every { storageReference.child(any()).putBytes(any()) } returns uploadTask
    every { uploadTask.continueWithTask<Uri>(any()) } returns downloadUrlTask

    storeRoute.addRoute(userId, route, checkpoints)

    val slots = mutableListOf<String>()
    verify(exactly = 2) { storageReference.child(capture(slots)).putBytes(any()) }
  }
}
