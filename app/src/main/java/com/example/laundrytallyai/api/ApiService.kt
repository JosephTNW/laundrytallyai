package com.example.laundrytallyai.api

import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.api.dataschemes.HomeData
import com.example.laundrytallyai.api.dataschemes.LaundererData
import com.example.laundrytallyai.api.dataschemes.LoginRequest
import com.example.laundrytallyai.api.dataschemes.RegisterRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/")
    suspend fun getHomeData(
        @Header("Authorization")
        token: String): Response<HomeData>
    @GET("/clothes")
    suspend fun getClothesData(@Header("Authorization") token: String): Response<List<ClothesData>>
    @GET("/launderer")
    suspend fun getLaundererData(@Header("Authorization") token: String): Response<List<LaundererData>>
    @GET("/launderer/search")
    suspend fun searchLaundererData(
        @Header("Authorization") token: String,
        @Query("query") query: String
    ): Response<List<LaundererData>>
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseBody>
    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>
}