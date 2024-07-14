package com.example.laundrytallyai.api

import android.graphics.Bitmap
import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.api.dataschemes.HomeData
import com.example.laundrytallyai.api.dataschemes.LaundererData
import com.example.laundrytallyai.api.dataschemes.LaundryData
import com.example.laundrytallyai.api.dataschemes.LaundryValData
import com.example.laundrytallyai.api.dataschemes.LoginRequest
import com.example.laundrytallyai.api.dataschemes.RegisterRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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
    ): Response<ResponseBody>

    @Multipart
    @PUT("/clothes")
    suspend fun editClothesData(
        @Header("Authorization") token: String,
        @Part("id") id: RequestBody,
        @Part("c_type") c_type: RequestBody,
        @Part("color") color: RequestBody,
        @Part("desc") desc: RequestBody,
        @Part cloth_pic: MultipartBody.Part?
    ): Response<ResponseBody>

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

    @PUT("/validate")
    suspend fun validateLaundryData(
        @Header("Authorization") token: String,
        @Body body: LaundryValData
    ): Response<ResponseBody>

    @POST("/laundry")
    suspend fun createLaundryData(
        @Header("Authorization") token: String,
        @Body clothes_ids: IntArray,
        @Body launderer_id: Int,
        @Body laundry_days: Int,
        @Body bill_pic: Bitmap
    ): Response<ResponseBody>

    // auth
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseBody>

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>
}