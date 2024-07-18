package com.example.laundrytallyai.pages.clothes

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laundrytallyai.api.RetrofitClient
import com.example.laundrytallyai.api.dataschemes.ApiResponse
import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.api.dataschemes.ClothesFormItemData
import com.example.laundrytallyai.api.dataschemes.ClothesPostData
import com.example.laundrytallyai.api.dataschemes.PredictionsData
import com.example.laundrytallyai.api.dataschemes.TypePredictResponse
import com.example.laundrytallyai.api.datastates.ClothesDataState
import com.example.laundrytallyai.api.datastates.ModifyDataState
import com.example.laundrytallyai.api.datastates.TypePredictionDataState
import com.example.laundrytallyai.api.parseError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private val _selectedClothes = MutableStateFlow<ClothesData?>(null)
    val selectedClothes: StateFlow<ClothesData?> = _selectedClothes.asStateFlow()
    private val _predictedClothes = MutableStateFlow<List<PredictionsData>>(emptyList())
    val predictedClothes: StateFlow<List<PredictionsData>> = _predictedClothes.asStateFlow()
    private val _predictedType = MutableStateFlow<TypePredictionDataState>(TypePredictionDataState.Loading)
    val predictedType: StateFlow<TypePredictionDataState> = _predictedType.asStateFlow()
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

    fun setSelectedClothes(clothes: ClothesData) {
        _selectedClothes.value = clothes
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
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

                val token = sharedPreferences.getString("token", null) ?: run {
                    _modifyState.value = ModifyDataState.Error("401", "No auth token found")
                    return@launch
                }
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
                            ModifyDataState.Error("400", "Response body was null")
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

    fun setPredictions(predictions: List<PredictionsData>) {
        _predictedClothes.value = predictions
    }

    fun predictClothingType(clothingImages: List<File>) {
        viewModelScope.launch {
            try {
                _predictedType.value = TypePredictionDataState.Loading
                val token = sharedPreferences.getString("token", null) ?: run {
                    _predictedType.value = TypePredictionDataState.Error("401", "No auth token found")
                    return@launch
                }
                val imageParts = clothingImages.map {
                    MultipartBody.Part.createFormData(
                        "cloth_pics", it.name, it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                }
                val response = RetrofitClient.instance.predictClothingType(token, imageParts)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val jsonString = responseBody.string()
                        try {
                            // Parse the JSON string
                            val gson = Gson()
                            val predictionResponse = gson.fromJson(jsonString, TypePredictResponse::class.java)
                            val predictedTypes: List<String> = predictionResponse.data
                            _predictedType.value = TypePredictionDataState.Success(predictedTypes)
                        } catch (e: Exception) {
                            _predictedType.value = TypePredictionDataState.Error("Parsing Error", "Failed to parse response: ${e.message}")
                        }
                    } else {
                        _predictedType.value = TypePredictionDataState.Error("No response body", "Response body was null")
                    }
                } else {
                    _predictedType.value = TypePredictionDataState.Error(
                        response.code().toString(),
                        response.errorBody()?.string() ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _predictedType.value = TypePredictionDataState.Error("500", e.message ?: "Unknown error")
            }
        }
    }

    fun clearPredictedTypesData() {
        _predictedType.value = TypePredictionDataState.Loading
        _predictedClothes.value = emptyList()
    }

    fun submitAllClothesData(clothesData: List<ClothesFormItemData>) {
        viewModelScope.launch {
            try {
                _modifyState.value = ModifyDataState.Loading
                val token = sharedPreferences.getString("token", null) ?: run {
                    _modifyState.value = ModifyDataState.Error("401", "No auth token found")
                    return@launch
                }
                var allParts = mutableListOf<MultipartBody.Part>()

                clothesData.forEachIndexed { index, item ->
                    // Convert Bitmap to File
                    val file = File.createTempFile("image", ".jpg")
                    file.outputStream().use { out ->
                        item.cloth_pic.value.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    }

                    val filePart = MultipartBody.Part.createFormData(
                        "file$index",
                        "image$index.jpg",
                        file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )

                    val cTypePart = MultipartBody.Part.createFormData("cType$index", item.c_type.value)
                    val colorPart = MultipartBody.Part.createFormData("color$index", item.color.value)
                    val descPart = MultipartBody.Part.createFormData("desc$index", item.desc.value)

                    allParts.addAll(listOf(filePart, cTypePart, colorPart, descPart))

                }

                val response = RetrofitClient.instance.addClothes(token, allParts)

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
                            ModifyDataState.Error("400", "Response body was null")
                    }
                } else {
                    _modifyState.value = ModifyDataState.Error(
                        response.code().toString(),
                        response.errorBody()?.string() ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _modifyState.value = ModifyDataState.Error("401", e.message.toString())
            }
        }
    }
}