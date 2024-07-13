package com.example.laundrytallyai.pages.clothes

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laundrytallyai.api.RetrofitClient
import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.api.datastates.ClothesDataState
import com.example.laundrytallyai.api.datastates.ModifyClothesState
import com.example.laundrytallyai.api.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClothesViewModel @Inject constructor(
    application: Application
) : ViewModel() {
    private val _dataState = MutableStateFlow<ClothesDataState>(ClothesDataState.Loading)
    val dataState: StateFlow<ClothesDataState> = _dataState
    private val _modifyState = MutableStateFlow<ModifyClothesState>(ModifyClothesState.Idle)
    val modifyState: StateFlow<ModifyClothesState> = _modifyState.asStateFlow()
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
                _modifyState.value = ModifyClothesState.Loading
                val token = sharedPreferences.getString("token", null) ?: run {
                    _modifyState.value = ModifyClothesState.Error("401", "No auth token found")
                    return@launch
                }
                val response = RetrofitClient.instance.deleteClothesData(token, clothesId)
                if (response.isSuccessful) {
                    fetchData()
                    _modifyState.value = ModifyClothesState.Success("Clothes deleted successfully")
                } else {
                    val parsedError = parseError(response)
                    _modifyState.value = ModifyClothesState.Error(
                        parsedError["code"] ?: "000",
                        parsedError["error"] ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _modifyState.value = ModifyClothesState.Error("500", e.message ?: "Unknown error")
            }
        }
    }
}