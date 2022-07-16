package dev.geoit.android.storyapp.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient(base_url: String) {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(base_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val instanceDicoding: DicodingEndpoint by lazy {
        retrofit.create(DicodingEndpoint::class.java)
    }

}