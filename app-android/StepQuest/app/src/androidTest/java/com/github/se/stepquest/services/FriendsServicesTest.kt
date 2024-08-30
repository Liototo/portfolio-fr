package com.github.se.stepquest.services

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseServiceTest {

  private lateinit var database: FirebaseDatabase

  private lateinit var requestsRef: DatabaseReference

  @Before
  fun setup() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    FirebaseApp.initializeApp(context)

    database = mockk(relaxed = true)
  }

  @Test
  fun sendFriendRequestTest() {

    sendFriendRequest("currentUsername", "friendName")
  }

  @Test
  fun deletePendingFriendRequestTest() {

    requestsRef = mockk(relaxed = true)

    every { database.reference } returns
        mockk {
          every { child(any()) } returns
              mockk {
                every { child(any()) } returns mockk { every { child(any()) } returns requestsRef }
              }
        }

    deletePendingFriendRequest("friendName", database, "currentUsername")
  }

  @Test
  fun deleteFriendTest() {

    deleteFriend("currentUsername", "friendName", database, "uid")
  }
}
