package com.github.se.stepquest.viewModels

import androidx.lifecycle.ViewModel
import com.github.se.stepquest.Friend
import com.github.se.stepquest.data.model.NotificationData
import com.github.se.stepquest.data.model.NotificationType
import com.github.se.stepquest.data.repository.INotificationRepository
import com.github.se.stepquest.services.acceptChallenge
import com.github.se.stepquest.services.addFriend
import com.github.se.stepquest.services.getPendingChallenge
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** ViewModel handling the behaviour of the Notification screen. */
class NotificationViewModel : ViewModel() {
  private val _notificationList = MutableStateFlow<List<NotificationData>>(emptyList())
  val notificationList: StateFlow<List<NotificationData>> = _notificationList

  private val notificationRepository = INotificationRepository()

  /**
   * Retrieves the user's notifications in the database.
   *
   * @param userId the current user's database ID.
   */
  fun updateNotificationList(userId: String) {
    notificationRepository
        .getNotificationList(userId)
        .addValueEventListener(
            object : ValueEventListener {
              override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newNotificationList = mutableListOf<NotificationData>()
                for (postSnapshot in dataSnapshot.children) {
                  val resultData = postSnapshot.getValue<NotificationData>()
                  if (resultData != null && !resultData.isNull()) {
                    newNotificationList.add(resultData)
                  }
                }
                _notificationList.value = newNotificationList
              }

              override fun onCancelled(databaseError: DatabaseError) {
                println("Error: $databaseError")
              }
            })
  }

  /**
   * Handles actions when accepting the notification. If it's a friend request, add the user as a
   * friend. If it's a challenge, accept the challenge.
   *
   * @param data the original notification.
   * @param userId the current user's database ID.
   */
  fun handleNotificationAction(data: NotificationData, userId: String) {
    when (data.type) {
      NotificationType.FRIEND_REQUEST -> handleFriendRequest(data)
      NotificationType.CHALLENGE -> handleChallenge(data, userId)
      else -> {}
    }
    removeNotification(data)
  }

  /**
   * Accepts a friend request from a notification.
   *
   * @param data the original notification.
   */
  private fun handleFriendRequest(data: NotificationData) {
    val database = FirebaseDatabase.getInstance()
    val currentUserRef = database.reference.child("users").child(data.userUuid).child("username")
    currentUserRef.addListenerForSingleValueEvent(
        object : ValueEventListener {
          override fun onDataChange(snapshot: DataSnapshot) {
            val currentuserName = snapshot.getValue(String::class.java) ?: return
            val senderRef =
                database.reference.child("users").child(data.senderUuid).child("username")
            senderRef.addListenerForSingleValueEvent(
                object : ValueEventListener {
                  override fun onDataChange(snapshot: DataSnapshot) {
                    val friendName = snapshot.getValue(String::class.java) ?: return
                    addFriend(Friend(name = currentuserName), Friend(name = friendName))
                  }

                  override fun onCancelled(error: DatabaseError) {}
                })
          }

          override fun onCancelled(error: DatabaseError) {}
        })
  }

  /**
   * Accepts a challenge from a notification.
   *
   * @param data the original notification.
   * @param userId the current user's database ID.
   */
  private fun handleChallenge(data: NotificationData, userId: String) {
    getPendingChallenge(userId, data.objectUuid) { challenge ->
      if (challenge != null) {
        acceptChallenge(challenge)
      }
    }
  }

  /**
   * Removes a notification from the list.
   *
   * @param data the notification to remove.
   */
  fun removeNotification(data: NotificationData) {
    notificationRepository.removeNotification(data.userUuid, data.uuid)
  }
}
