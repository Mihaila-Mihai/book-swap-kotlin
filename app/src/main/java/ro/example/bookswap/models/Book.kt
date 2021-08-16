package ro.example.bookswap.models

import com.google.firebase.database.Exclude

class Book(
    var id: String = "",
    var title: String = "",
    var authors: String = "",
    var description: String = "",
    var pageCount: String = "",
    var language: String = "",
    var imageSliderUris: String = "",
    var thumbnail: String = "",
    var owner: String = ""
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "authors" to authors,
            "description" to description,
            "pageCount" to pageCount,
            "language" to language,
            "imageSliderUris" to imageSliderUris,
            "thumbnail" to thumbnail,
            "owner" to owner
        )
    }
}