package com.example.laundrytallyai.pages.clothes

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laundrytallyai.api.RetrofitClient
import com.example.laundrytallyai.api.datastates.ClothesDataState
import com.example.laundrytallyai.api.datastates.HomeDataState
import com.example.laundrytallyai.api.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClothesViewModel @Inject constructor(
    application: Application
) : ViewModel() {
    private val _dataState = MutableStateFlow<ClothesDataState>(ClothesDataState.Loading)
    val dataState: StateFlow<ClothesDataState> = _dataState
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun fetchData() {
        viewModelScope.launch {
            _dataState.value = ClothesDataState.Loading
            try {
                val token = sharedPreferences.getString("token", null) ?: run {
                    _dataState.value = ClothesDataState.Error("No auth token found")
                    return@launch
                }
                val response = RetrofitClient.instance.getClothesData(token)
                if (response.isSuccessful) {
                    val clothesData = response.body() ?: throw(Exception("No data found"))
                    _dataState.value = ClothesDataState.Success(clothesData)
                } else {
                    _dataState.value = ClothesDataState.Error(parseError(response)["code"] ?: "Unknown error")
                }
            } catch (e: Exception) {
                _dataState.value = ClothesDataState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun deleteToken() {
        sharedPreferences.edit().remove("token").apply()
    }
}