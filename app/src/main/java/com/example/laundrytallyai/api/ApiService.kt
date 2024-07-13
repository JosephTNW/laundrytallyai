package com.example.laundrytallyai.api

import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.api.dataschemes.HomeData
import com.example.laundrytallyai.api.dataschemes.LaundererData
import com.example.laundrytallyai.api.dataschemes.LaundryData
import com.example.laundrytallyai.api.dataschemes.LoginRequest
import com.example.laundrytallyai.api.dataschemes.RegisterRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // /
    @GET("/")
    suspend fun getHomeData(
        @Header("Authorization")
        token: String
    ): Response<HomeData>

    // clothes
    @GET("/clothes")
    suspend fun getClothesData(@Header("Authorization") token: String): Response<List<ClothesData>>

    @DELETE("/clothes")
    suspend fun deleteClothesData(
        @Header("Authorization") token: String,
        @Query("id") id: Int
    ): Response<String>

    // launderer
    @GET("/launderer")
    suspend fun getLaundererData(@Header("Authorization") token: String): Response<List<LaundererData>>

    @GET("/launderer/search")
    suspend fun searchLaundererData(
        @Header("Authorization") token: String,
        @Query("query") query: String
    ): Response<List<LaundererData>>

    // laundry
    @GET("/laundry")
    suspend fun getLaundryData(@Header("Authorization") token: String): Response<List<LaundryData>>

    // auth
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseBody>

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>
}