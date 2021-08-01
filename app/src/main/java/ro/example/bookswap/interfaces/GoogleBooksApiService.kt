package ro.example.bookswap.interfaces

import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.internal.ApiKey
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import ro.example.bookswap.models.RequestModel

interface GoogleBooksApiService {


    @GET("/books/v1/volumes")
    fun getBooks(
        @Query("q") searchString: String,
        @Query("key") apiKey: String
    ): Observable<RequestModel.Result>

    companion object {
        fun create(): GoogleBooksApiService {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://www.googleapis.com")
                .build()

            return retrofit.create(GoogleBooksApiService::class.java)
        }
    }

}