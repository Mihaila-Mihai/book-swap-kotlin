package ro.example.bookswap.models

class BookChecked(
    var id: String = "",
    var title: String = "",
    var authors: String = "",
    var description: String = "",
    var pageCount: String = "",
    var language: String = "",
    var imageSliderUris: String = "",
    var thumbnail: String = "",
    var owner: String = "",
    var checked: Boolean = false
) {
}