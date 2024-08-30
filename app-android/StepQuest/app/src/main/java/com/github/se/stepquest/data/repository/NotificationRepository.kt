package com.github.se.stepquest.data.repository

import com.github.se.stepquest.data.model.NotificationData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

interface NotificationRepository {
  fun getNotificationList(userUuid: String): DatabaseReference

  fun removeNotification(userUuid: String, notificationUuid: String)

  fun createNotification(userUuid: String, notificationData: NotificationData)
}

class INotificationRepository : NotificationRepository {
  private val notificationCollection =
      FirebaseDatabase.getInstance().reference.child("notifications")

  override fun getNotificationList(userUuid: String): DatabaseReference {
    return notificationCollection.child(userUuid)
  }

  override fun removeNotification(userUuid: String, notificationUuid: String) {
    println("Remove userId: $userUuid; notificationId: $notificationUuid")
    getNotificationList(userUuid)
        .child(notificationUuid)
        .addListenerForSingleValueEvent(
            object : ValueEventListener {
              override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                  println("Data: ${data.value}")
                  data.ref.removeValue()
                }
              }

              override fun onCancelled(databaseError: DatabaseError) {}
            })
  }

  override fun createNotification(userUuid: String, notificationData: NotificationData) {
    notificationCollection.child(userUuid).child(notificationData.uuid).setValue(notificationData)
  }
}
