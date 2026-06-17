package com.exo.hairstyleai.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    const val BASE_URL = "https://exostudio24--exo-image-ai-fastapi-app.modal.run/"

    // Demo key for this prototype.
    private const val API_KEY = "a8F3xK9mP2qW7rT4vY1nC6dL0sH5jBzE"

    /** Shared OkHttp client — long read timeout because generation takes a while. */
    val okHttp: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("X-API-Key", API_KEY)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .callTimeout(210, TimeUnit.SECONDS)
            .build()
    }

    val api: HairApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HairApi::class.java)
    }
}
