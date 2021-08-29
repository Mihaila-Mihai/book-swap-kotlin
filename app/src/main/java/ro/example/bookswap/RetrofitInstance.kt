package ro.example.bookswap

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ro.example.bookswap.Constants.Companion.BASE_URL
import ro.example.bookswap.interfaces.NotificationAPI

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        val api by lazy {
            retrofit.create(NotificationAPI::class.java)
        }
    }
}