package com.example.laundrytallyai.pages.clothes

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laundrytallyai.api.RetrofitClient
import com.example.laundrytallyai.api.dataschemes.ApiResponse
import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.api.datastates.ClothesDataState
import com.example.laundrytallyai.api.datastates.ModifyDataState
import com.example.laundrytallyai.api.parseError
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ClothesViewModel @Inject constructor(
    application: Application
) : ViewModel() {
    private val _dataState = MutableStateFlow<ClothesDataState>(ClothesDataState.Loading)
    val dataState: StateFlow<ClothesDataState> = _dataState
    private val _modifyState = MutableStateFlow<ModifyDataState>(ModifyDataState.Idle)
    val modifyState: StateFlow<ModifyDataState> = _modifyState.asStateFlow()
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun fetchData() {
        viewModelScope.launch {
            _dataState.value = ClothesDataState.Loading
            try {
                val token = sharedPreferences.getString("token", null) ?: run {
                    _dataState.value = ClothesDataState.Error(code = "401", "No auth token found")
                    return@launch
                }
                val response = RetrofitClient.instance.getClothesData(token)
                if (response.isSuccessful) {
                    val clothesData = response.body() ?: throw (Exception("No data found"))
                    _dataState.value = ClothesDataState.Success(clothesData)
                } else {
                    _dataState.value = ClothesDataState.Error(
                        "500",
                        parseError(response)["error"] ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _dataState.value = ClothesDataState.Error("500", e.message ?: "Unknown error")
            }
        }
    }

    private val _selectedClothes = MutableStateFlow<ClothesData?>(null)
    val selectedClothes: StateFlow<ClothesData?> = _selectedClothes.asStateFlow()

    fun setSelectedClothes(clothes: ClothesData) {
        _selectedClothes.value = clothes
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun deleteToken() {
        sharedPreferences.edit().remove("token").apply()
    }

    fun deleteClothes(clothesId: Int) {
        viewModelScope.launch {
            try {
                _modifyState.value = ModifyDataState.Loading
                val token = sharedPreferences.getString("token", null) ?: run {
                    _modifyState.value = ModifyDataState.Error("401", "No auth token found")
                    return@launch
                }
                val response = RetrofitClient.instance.deleteClothesData(token, clothesId)
                if (response.isSuccessful) {
                    fetchData()
                    _modifyState.value = ModifyDataState.Success("Clothes deleted successfully")
                } else {
                    val parsedError = parseError(response)
                    _modifyState.value = ModifyDataState.Error(
                        parsedError["code"] ?: "000",
                        parsedError["error"] ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _modifyState.value = ModifyDataState.Error("500", e.message ?: "Unknown error")
            }
        }
    }

    fun editClothes(
        clothesId: Int,
        clothingType: String,
        clothingColor: String,
        clothingDesc: String,
        clothingImage: File?
    ) {
        viewModelScope.launch {
            try {
                _modifyState.value = ModifyDataState.Loading

                val token = getToken() ?: throw Exception("No auth token found")

                val idPart = clothesId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val typePart = clothingType.toRequestBody("text/plain".toMediaTypeOrNull())
                val colorPart = clothingColor.toRequestBody("text/plain".toMediaTypeOrNull())
                val descPart = clothingDesc.toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = clothingImage?.let {
                    MultipartBody.Part.createFormData(
                        "cloth_pic", it.name, it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                }

                val response = RetrofitClient.instance.editClothesData(
                    token = token,
                    id = idPart,
                    c_type = typePart,
                    color = colorPart,
                    desc = descPart,
                    cloth_pic = imagePart
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string()
                        try {
                            val apiResponse = Gson().fromJson(jsonString, ApiResponse::class.java)
                            _modifyState.value = ModifyDataState.Success(apiResponse.message)
                        } catch (e: Exception) {
                            _modifyState.value = ModifyDataState.Error("Parsing Error", "Failed to parse response: ${e.message}")
                        }
                    } else {
                        _modifyState.value =
                            ModifyDataState.Error("No response body", "Response body was null")
                    }
                } else {
                    _modifyState.value = ModifyDataState.Error(
                        response.code().toString(),
                        response.errorBody()?.string() ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _modifyState.value = ModifyDataState.Error("500", e.message ?: "Unknown error")
            }
        }
    }

    fun setModified(state: ModifyDataState) {
        _modifyState.value = state
    }
}