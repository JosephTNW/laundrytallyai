package com.example.laundrytallyai.api

import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    var BASE_URL = if (DeviceUtils.isEmulator()) {
        "http://10.0.2.2:5000"
    } else {
        "https://ac30-111-94-188-102.ngrok-free.app"
    }

    val instance: ApiService by lazy {
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}