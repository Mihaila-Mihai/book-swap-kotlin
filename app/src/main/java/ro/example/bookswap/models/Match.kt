package ro.example.bookswap.models

class Match(
    val userId1: String = "",
    val userId2: String = ""
) {

    override fun toString(): String {
        return "userId1: ${this.userId1} \n " +
                "userId2: ${this.userId2}"
    }
}