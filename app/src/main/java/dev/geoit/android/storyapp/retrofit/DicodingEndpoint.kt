package dev.geoit.android.storyapp.retrofit

import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface DicodingEndpoint {

    @FormUrlEncoded
    @POST("login")
    fun postLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<JsonElement>

    @FormUrlEncoded
    @POST("register")
    fun postRegister(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
    ): Call<JsonElement>

    @GET("stories")
    fun getAllStories(
        @Header("Authorization") bearerToken: String?,
        @Query("page") page: Int? = 1,
        @Query("size") size: Int? = null,
        @Query("location") location: Int = 0,
    ): Call<JsonElement>

    @Multipart
    @POST("stories")
    fun postNewStories(
        @Header("Authorization") bearerToken: String?,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null,
    ): Call<JsonElement>
}
