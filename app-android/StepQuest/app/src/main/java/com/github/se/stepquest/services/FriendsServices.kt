package com.github.se.stepquest.services

import com.github.se.stepquest.Friend
import com.github.se.stepquest.IUserRepository
import com.github.se.stepquest.data.model.NotificationData
import com.github.se.stepquest.data.model.NotificationType
import com.github.se.stepquest.data.repository.INotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

fun addFriend(
    currentUser: Friend,
    newFriend: Friend,
    database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    currentUserId: String? = FirebaseAuth.getInstance().currentUser?.uid
) {

  if (currentUserId == null) return

  // Retrieve friend's uid
  val usernamesRef = database.reference.child("usernames")
  usernamesRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          val uid = snapshot.child(newFriend.name).getValue(String::class.java) ?: return

          // Add newFriend to currentUser's list
          val curFriendsRefTotal =
              database.reference.child("users").child(currentUserId).child("numberOfFriends")
          curFriendsRefTotal.addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                  val numberOfFriends = dataSnapshot.getValue(Int::class.java) ?: 0
                  curFriendsRefTotal.setValue(numberOfFriends + 1)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                  // add code when failing to access database
                }
              })

          val curFriendsListRef =
              database.reference
                  .child("users")
                  .child(currentUserId)
                  .child("friendsList")
                  .child(newFriend.name)
          curFriendsListRef.addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                  curFriendsListRef.setValue(newFriend)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                  // add code when failing to access database
                }
              })

          // Add currentUser to newFriend's list
          val friendsRefTotal =
              database.reference.child("users").child(uid).child("numberOfFriends")
          friendsRefTotal.addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                  val numberOfFriends = dataSnapshot.getValue(Int::class.java) ?: 0
                  friendsRefTotal.setValue(numberOfFriends + 1)
                }

                override fun onCancelled(error: DatabaseError) {
                  // Handle database access failure
                }
              })

          val friendsListRef =
              database.reference
                  .child("users")
                  .child(uid)
                  .child("friendsList")
                  .child(currentUser.name)
          friendsListRef.addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                  friendsListRef.setValue(currentUser)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                  // add code when failing to access database
                }
              })
        }

        override fun onCancelled(error: DatabaseError) {
          // Handle database access failure
        }
      })
}

fun deleteFriend(
    currentUsername: String,
    friendName: String,
    database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    currentUserId: String? = FirebaseAuth.getInstance().currentUser?.uid
) {

  if (currentUserId == null) return

  // Retrieve friend's uid
  val usernamesRef = database.reference.child("usernames")
  usernamesRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          val uid = snapshot.child(friendName).getValue(String::class.java) ?: return

          // Remove friend from current user's list
          val curFriendsRefTotal =
              database.reference.child("users").child(currentUserId).child("numberOfFriends")
          curFriendsRefTotal.addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                  val numberOfFriends = dataSnapshot.getValue(Int::class.java) ?: 0
                  curFriendsRefTotal.setValue(numberOfFriends - 1)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                  // add code when failing to access database
                }
              })

          val curFriendsListRef =
              database.reference.child("users").child(currentUserId).child("friendsList")
          curFriendsListRef.addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                  val currentFriendsList: MutableList<Friend> =
                      dataSnapshot.getValue<List<Friend>>()?.toMutableList() ?: mutableListOf()
                  val friendToRemove = currentFriendsList.find { it.name == friendName }

                  if (friendToRemove == null) return

                  currentFriendsList.remove(friendToRemove)
                  curFriendsListRef.setValue(currentFriendsList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                  // add code when failing to access database
                }
              })

          // Remove current user from friend's list
          val friendsRefTotal =
              database.reference.child("users").child(uid).child("numberOfFriends")
          friendsRefTotal.addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                  val numberOfFriends = dataSnapshot.getValue(Int::class.java) ?: 0
                  friendsRefTotal.setValue(numberOfFriends - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                  // Handle database access failure
                }
              })

          val friendsListRef = database.reference.child("users").child(uid).child("friendsList")
          friendsListRef.addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                  val currentFriendsList: MutableList<Friend> =
                      dataSnapshot.getValue<List<Friend>>()?.toMutableList() ?: mutableListOf()
                  val friendToRemove = currentFriendsList.find { it.name == currentUsername }

                  if (friendToRemove == null) return

                  currentFriendsList.remove(friendToRemove)
                  friendsListRef.setValue(currentFriendsList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                  // add code when failing to access database
                }
              })
        }

        override fun onCancelled(error: DatabaseError) {
          // Handle database access failure
        }
      })
}

fun sendFriendRequest(currentUsername: String, friendName: String) {
  val userRepository = IUserRepository()
  val notificationRepository = INotificationRepository()
  val database = FirebaseDatabase.getInstance()

  // Retrieve the new friend's uid
  val usernamesRef = database.reference.child("usernames")

  usernamesRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          val uid = snapshot.child(friendName).getValue(String::class.java) ?: return

          // Send the friend request
          val friendRequestsRef =
              database.reference.child("users").child(uid).child("pendingFriendRequests")
          friendRequestsRef.addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(requestsSnapshot: DataSnapshot) {
                  val pendingRequests =
                      requestsSnapshot.getValue<List<String>>()?.toMutableList() ?: mutableListOf()
                  if (currentUsername in pendingRequests) return
                  pendingRequests.add(currentUsername)
                  friendRequestsRef.setValue(pendingRequests)

                  // Send notification
                  val senderUserId = userRepository.getUid().toString()
                  usernamesRef
                      .child(friendName)
                      .addListenerForSingleValueEvent(
                          object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                              println("Snapshot: ${snapshot.value}")
                              val receiverUserId = snapshot.value.toString()
                              notificationRepository.createNotification(
                                  receiverUserId,
                                  NotificationData(
                                      "You received new friend request from $currentUsername!",
                                      LocalDateTime.now()
                                          .format(DateTimeFormatter.ofPattern("HH:mm")),
                                      UUID.randomUUID().toString(),
                                      receiverUserId,
                                      senderUserId,
                                      type = NotificationType.FRIEND_REQUEST))
                            }

                            override fun onCancelled(error: DatabaseError) {
                              TODO("Not yet implemented")
                            }
                          })
                }

                override fun onCancelled(error: DatabaseError) {
                  // Handle access failure
                }
              })
        }

        override fun onCancelled(error: DatabaseError) {
          // Handle access failure
        }
      })
}

fun deletePendingFriendRequest(
    friendName: String,
    database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    userId: String? = FirebaseAuth.getInstance().currentUser?.uid
) {

  if (userId == null) return

  val pendingRequestsRef =
      database.reference.child("users").child(userId).child("pendingFriendRequests")
  pendingRequestsRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(requestsSnapshot: DataSnapshot) {
          val pendingRequests =
              requestsSnapshot.getValue<List<String>>()?.toMutableList() ?: mutableListOf()
          if (friendName !in pendingRequests) return
          pendingRequests.remove(friendName)
          pendingRequestsRef.setValue(pendingRequests)
        }

        override fun onCancelled(error: DatabaseError) {
          // Handle access failure
        }
      })
}

fun fetchFriendsListFromDatabase(userId: String, callback: (List<Friend>?) -> Unit) {
  val database = FirebaseDatabase.getInstance()
  val currentFriendsList = mutableListOf<Friend>()
  val friendsListRef = database.reference.child("users").child(userId).child("friendsList")
  friendsListRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          for (snapshot in dataSnapshot.children) {
            val friend = snapshot.getValue(Friend::class.java)
            friend?.let { currentFriendsList.add(it) }
          }
          callback(currentFriendsList)
        }

        override fun onCancelled(databaseError: DatabaseError) {
          callback(null)
        }
      })
}
