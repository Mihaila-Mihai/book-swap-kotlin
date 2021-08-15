package ro.example.bookswap.models

import ro.example.bookswap.enums.Status

class Swap(
    val sender: String = "",
    val receiver: String = "",
    val senderBook: String = "",
    val receiverBook: String = "",
    val id: String = "",
    val status: Status = Status.IN_PROGRESS
    ) {
}