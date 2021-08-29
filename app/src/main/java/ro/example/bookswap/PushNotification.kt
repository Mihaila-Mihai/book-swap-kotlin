package ro.example.bookswap

data class PushNotification(
    val data: NotificationData,
    val to: String
)