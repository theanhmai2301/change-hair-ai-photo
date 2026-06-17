package com.exo.hairstyleai.data.api

import com.exo.hairstyleai.data.model.HairResult
import com.exo.hairstyleai.data.model.HairstylesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface HairApi {

    /** Catalog of the 28 presets. */
    @GET("v1/hairstyles")
    suspend fun getHairstyles(): HairstylesResponse

    /** Apply a preset to an uploaded photo (multipart file). */
    @Multipart
    @POST("v1/hair")
    suspend fun applyHairFile(
        @Part file: MultipartBody.Part,
        @Part("hair") hair: RequestBody,
        @Part("quality") quality: RequestBody,
    ): HairResult

    /** Apply a preset to a remote sample image (by URL). */
    @Multipart
    @POST("v1/hair")
    suspend fun applyHairUrl(
        @Part("image_url") imageUrl: RequestBody,
        @Part("hair") hair: RequestBody,
        @Part("quality") quality: RequestBody,
    ): HairResult
}
