package com.github.se.stepquest.services

import android.text.format.DateFormat
import android.util.Log
import com.github.se.stepquest.IUserRepository
import com.github.se.stepquest.data.model.ChallengeData
import com.github.se.stepquest.data.model.ChallengeProgression
import com.github.se.stepquest.data.model.ChallengeType
import com.github.se.stepquest.data.model.NotificationData
import com.github.se.stepquest.data.model.NotificationType
import com.github.se.stepquest.data.repository.INotificationRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

fun createChallengeItem(
    currentUserId: String,
    currentUsername: String,
    friendUserId: String,
    friendUsername: String,
    type: ChallengeType
): ChallengeData {
  val currentDate = Date()
  return when (type) {
    ChallengeType.REGULAR_STEP_CHALLENGE ->
        ChallengeData(
            DateFormat.format("MMMM d, yyyy ", currentDate.time)
                .toString(), // use of the current date for Uid
            ChallengeType.REGULAR_STEP_CHALLENGE,
            stepsToMake = 50000,
            kilometersToWalk = 0,
            daysToComplete = 10,
            getEndDate(currentDate, 10),
            friendUsername,
            friendUserId,
            currentUsername,
            currentUserId,
            ChallengeProgression(friendUserId, 0, 0),
            ChallengeProgression(currentUserId, 0, 0))
    ChallengeType.DAILY_STEP_CHALLENGE ->
        ChallengeData(
            DateFormat.format("MMMM d, yyyy ", currentDate.time)
                .toString(), // use of the current date for Uid
            ChallengeType.DAILY_STEP_CHALLENGE,
            stepsToMake = 5000,
            kilometersToWalk = 0,
            daysToComplete = 1,
            getEndDate(currentDate, 1),
            friendUsername,
            friendUserId,
            currentUsername,
            currentUserId,
            ChallengeProgression(friendUserId, 0, 0),
            ChallengeProgression(currentUserId, 0, 0))
  }
}

fun sendPendingChallenge(challenge: ChallengeData) {
  val userRepository = IUserRepository()
  val notificationRepository = INotificationRepository()
  val database = FirebaseDatabase.getInstance()
  val usernamesRef = database.reference.child("usernames")
  val senderUserId = userRepository.getUid().toString()
  usernamesRef
      .child(challenge.challengedUsername)
      .addListenerForSingleValueEvent(
          object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
              val uid = snapshot.getValue(String::class.java) ?: return
              val challengeRequestsRef =
                  database.reference
                      .child("users")
                      .child(uid)
                      .child("pendingChallenges")
                      .child(challenge.uuid)
              challengeRequestsRef.addListenerForSingleValueEvent(
                  object : ValueEventListener {
                    override fun onDataChange(requestsSnapshot: DataSnapshot) {
                      if (requestsSnapshot.value == null) {
                        challengeRequestsRef.setValue(challenge)
                        println("Snapshot: ${snapshot.value}")
                        val receiverUserId = snapshot.value.toString()
                        val challengeType = challenge.type
                        val messageText = challengeType.messageText
                        val senderUsername = challenge.senderUsername
                        notificationRepository.createNotification(
                            receiverUserId,
                            NotificationData(
                                "$senderUsername sent a new challenge! : $messageText",
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                                UUID.randomUUID().toString(),
                                receiverUserId,
                                senderUserId,
                                objectUuid = challenge.uuid,
                                type = NotificationType.CHALLENGE))
                      }
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

fun getPendingChallenge(userId: String, challengeUuid: String, callback: (ChallengeData?) -> Unit) {
  val database = FirebaseDatabase.getInstance()
  val challengeRef =
      database.reference
          .child("users")
          .child(userId)
          .child("pendingChallenges")
          .child(challengeUuid)
  challengeRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          val challenge = snapshot.getValue(ChallengeData::class.java)
          callback(challenge)
        }

        override fun onCancelled(error: DatabaseError) {}
      })
}

fun acceptChallenge(challenge: ChallengeData) {
  val database = FirebaseDatabase.getInstance()
  val firstUserRef =
      database.reference
          .child("users")
          .child(challenge.challengedUserUuid)
          .child("acceptedChallenges")
          .child(challenge.uuid)
  val secondUserRef =
      database.reference
          .child("users")
          .child(challenge.senderUserUuid)
          .child("acceptedChallenges")
          .child(challenge.uuid)

  // First, ensure the challenge is accepted by the first user
  firstUserRef.setValue(challenge).addOnCompleteListener { task ->
    if (task.isSuccessful) {
      // If successful, proceed to set the challenge for the second user
      secondUserRef.setValue(challenge).addOnCompleteListener { task2 ->
        if (task2.isSuccessful) {
          // Both updates are successful, remove from pending list and add to global challenges list
          val challengeRef =
              database.reference
                  .child("users")
                  .child(challenge.challengedUserUuid)
                  .child("pendingChallenges")
                  .child(challenge.uuid)
          challengeRef.removeValue()

          val challengeListRef = database.reference.child("challenges").child(challenge.uuid)
          challengeListRef.setValue(challenge)
        } else {
          // Handle failure for second user update
          println("Failed to update challenge for the sender user: ${task2.exception?.message}")
        }
      }
    } else {
      // Handle failure for first user update
      println("Failed to update challenge for the challenged user: ${task.exception?.message}")
    }
  }
}

fun getTopChallenge(userId: String, callback: (ChallengeData?) -> Unit) {
  val database = FirebaseDatabase.getInstance()
  val challengeRef =
      database.reference.child("users").child(userId).child("acceptedChallenges").limitToFirst(1)
  challengeRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          dataSnapshot.children.forEach { childSnapshot ->
            val challenge = childSnapshot.getValue(ChallengeData::class.java)
            callback(challenge)
          }
        }

        override fun onCancelled(error: DatabaseError) {
          callback(null)
        }
      })
}

fun getChallenges(userId: String, callback: (List<ChallengeData>) -> Unit) {
  val challenges: MutableList<ChallengeData> = mutableListOf()
  val database = FirebaseDatabase.getInstance()
  val acceptedChallengesRef =
      database.reference.child("users").child(userId).child("acceptedChallenges")
  acceptedChallengesRef.addListenerForSingleValueEvent(
      object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
          for (snapshot in dataSnapshot.getChildren()) {
            val challenge = snapshot.getValue(ChallengeData::class.java)
            if (challenge != null) {
              challenges.add(challenge)
            }
          }
          callback(challenges)
        }

        override fun onCancelled(error: DatabaseError) {}
      })
}

fun getEndDate(startDate: Date, daysToAdd: Int): String {
  val calendar = Calendar.getInstance()
  calendar.time = startDate
  calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
  val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
  return dateFormat.format(calendar.time)
}

fun someChallengesCompleted(userId: String, callback: (Boolean) -> Unit) {
  Log.d("someChallengesCompleted", "userId: $userId")
  var challenges: List<ChallengeData>
  var result = false
  getChallenges(userId) { v ->
    challenges = v
    Log.d("someChallengesCompleted", "vListSize: ${v.size}")
    challenges.forEach { challenge ->
      Log.d("someChallengesCompleted", "Challenge: $challenge")
      if (challenge.type.completionFunction(challenge)) {
        result = true
        deleteCompletedChallenge(challenge)
      }
    }
    callback(result)
  }
}

fun deleteCompletedChallenge(challenge: ChallengeData) {
  Log.d(
      "deleteCompletedChallenge",
      "challengedUserUuid: ${challenge.challengedUserUuid}, UUID: ${challenge.uuid}")
  FirebaseDatabase.getInstance()
      .reference
      .child("users")
      .child(challenge.challengedUserUuid)
      .child("acceptedChallenges")
      .child(challenge.uuid)
      .ref
      .removeValue()
}
