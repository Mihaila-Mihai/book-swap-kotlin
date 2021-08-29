package ro.example.bookswap.interfaces


import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import ro.example.bookswap.BuildConfig
import ro.example.bookswap.Constants.Companion.CONTENT_TYPE

import ro.example.bookswap.PushNotification


interface NotificationAPI {
    @Headers("Authorization: key=${BuildConfig.SERVER_KEY}", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}