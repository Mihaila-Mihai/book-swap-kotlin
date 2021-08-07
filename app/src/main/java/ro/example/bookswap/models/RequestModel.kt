package ro.example.bookswap.models

object RequestModel {
    data class Result(val items: ArrayList<Items>)
    data class Items(val id: String, val volumeInfo: VolumeInfo)
    data class VolumeInfo(
        val title: String,
        val authors: ArrayList<String>,
        val description: String,
        val pageCount: String,
        val categories: ArrayList<String>,
        val imageLinks: ImageLinks,
        val previewLink: String,
        val language: String
    )

    data class ImageLinks(val smallThumbnail: String, val thumbnail: String)
}