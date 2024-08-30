package com.github.se.stepquest.data.model

enum class NotificationType {
  FRIEND_REQUEST,
  CHALLENGE,
  TEMP
}

data class NotificationData(
    var text: String = "",
    var dateTime: String = "",
    var uuid: String = "",
    var userUuid: String = "",
    var senderUuid: String = "",
    val objectUuid: String = "",
    var type: NotificationType = NotificationType.TEMP
) {

  fun isNull(): Boolean {
    return text.isEmpty() || dateTime.isEmpty() || uuid.isEmpty() || userUuid.isEmpty()
  }
}
