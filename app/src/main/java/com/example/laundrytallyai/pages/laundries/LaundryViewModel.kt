package com.example.laundrytallyai.pages.laundries

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laundrytallyai.api.RetrofitClient
import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.api.dataschemes.LaundryData
import com.example.laundrytallyai.api.dataschemes.LaundryValData
import com.example.laundrytallyai.api.datastates.LaundryDataState
import com.example.laundrytallyai.api.datastates.ModifyDataState
import com.example.laundrytallyai.api.parseError
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
class LaundryViewModel @Inject constructor(
    application: Application
) : ViewModel() {
    private val _dataState = MutableStateFlow<LaundryDataState>(LaundryDataState.Loading)
    val dataState: StateFlow<LaundryDataState> = _dataState
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun fetchData() {
        viewModelScope.launch {
            _dataState.value = LaundryDataState.Loading
            try {
                val token = sharedPreferences.getString("token", null) ?: run {
                    _dataState.value = LaundryDataState.Error("401", "No auth token found")
                    return@launch
                }
                val response = RetrofitClient.instance.getLaundryData(token)
                if (response.isSuccessful) {
                    val laundryData = response.body() ?: throw (Exception("No data found"))
                    _dataState.value = LaundryDataState.Success(laundryData)
                } else {
                    _dataState.value = LaundryDataState.Error(
                        "500",
                        parseError(response)["error"] ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _dataState.value = LaundryDataState.Error("407", e.message ?: "Unknown error")
            }
        }
    }

    private val _selectedLaundry = MutableStateFlow<LaundryData?>(null)
    val selectedLaundry: StateFlow<LaundryData?> = _selectedLaundry.asStateFlow()

    fun setSelectedLaundry(laundry: LaundryData) {
        _selectedLaundry.value = laundry
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun deleteToken() {
        sharedPreferences.edit().remove("token").apply()
    }

    fun getUsername() {
        sharedPreferences.getString("username", null)
    }

    private val _validationState = MutableStateFlow<ModifyDataState>(ModifyDataState.Idle)
    val validationState: StateFlow<ModifyDataState> = _validationState.asStateFlow()

    fun validateLaundry(laundryId: Int, clothesIds: IntArray) {
        viewModelScope.launch {
            _validationState.value = ModifyDataState.Loading
            try {
                val token = sharedPreferences.getString("token", null) ?: run {
                    _validationState.value = ModifyDataState.Error("401", "No auth token found")
                    return@launch
                }
                val response =
                    RetrofitClient.instance.validateLaundryData(
                        token,
                        LaundryValData(clothesIds, laundryId)
                    )
                if (response.isSuccessful) {
                    _validationState.value =
                        ModifyDataState.Success("Laundry validated successfully")
                } else {
                    _validationState.value = ModifyDataState.Error(
                        "500",
                        parseError(response)["error"] ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _validationState.value = ModifyDataState.Error("407", e.message ?: "Unknown error")
            }
        }
    }

    fun setValidationState(state: ModifyDataState) {
        _validationState.value = state
    }

    private val _creationState = MutableStateFlow<ModifyDataState>(ModifyDataState.Idle)
    val creationState: StateFlow<ModifyDataState> = _creationState.asStateFlow()

    fun createLaundry(
        laundererId: Int,
        clothesIds: IntArray,
        laundryDays: Int,
        billPic: File?
    ) {
        viewModelScope.launch {
            _creationState.value = ModifyDataState.Loading
            try {
                val token = sharedPreferences.getString("token", null) ?: run {
                    _creationState.value = ModifyDataState.Error("401", "No auth token found")
                    return@launch
                }

                val laundererIdPart =
                    laundererId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val clothesIdsPart =
                    clothesIds.joinToString(",").toRequestBody("text/plain".toMediaTypeOrNull())
                val laundryDaysPart =
                    laundryDays.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                Log.d("LaundryViewModel:", "clothesIdsPart: $clothesIdsPart")
                Log.d("LaundryViewModel:", "clothesIdsJoin: ${clothesIds.joinToString(",")}")

                val billPicPart = billPic?.let {
                    MultipartBody.Part.createFormData(
                        "bill_pic", it.name, it.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                }

                val response = RetrofitClient.instance.createLaundryData(
                    token = token,
                    clothes_ids = clothesIdsPart,
                    launderer_id = laundererIdPart,
                    laundry_days = laundryDaysPart,
                    bill_pic = billPicPart
                )
                if (response.isSuccessful) {
                    val laundryData = response.body() ?: throw (Exception("No data found"))
                } else {
                    _creationState.value = ModifyDataState.Error(
                        "500",
                        parseError(response)["error"] ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _creationState.value = ModifyDataState.Error("407", e.message ?: "Unknown error")
            }
        }
    }

    private val _selectedClothesState = MutableStateFlow<List<ClothesData?>>(emptyList())
    val selectedClothesState: StateFlow<List<ClothesData?>> = _selectedClothesState.asStateFlow()

    fun setSelectedClothes(clothesList: List<ClothesData?>) {
        _selectedClothesState.value = clothesList
    }
}