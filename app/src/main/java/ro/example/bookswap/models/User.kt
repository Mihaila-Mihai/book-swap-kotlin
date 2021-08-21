package ro.example.bookswap.models

class User(
    var username: String = "",
    var password: String = "",
    var email: String = "",
    var description: String = "",
    var provider: String = "",
    var imageUrl: String = "",
    var id: String = "",
    val location: LocationModel,
    val distanceToUser: String = "100000.0"
) {
    constructor() : this("","","","","", "", "", LocationModel("", ""))

}