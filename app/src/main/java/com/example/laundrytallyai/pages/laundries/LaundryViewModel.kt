package com.example.laundrytallyai.pages.laundries

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laundrytallyai.api.RetrofitClient
import com.example.laundrytallyai.api.datastates.LaundryDataState
import com.example.laundrytallyai.api.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaundryViewModel @Inject constructor(
    application: Application
) : ViewModel() {
    private val _dataState = MutableStateFlow<LaundryDataState>(LaundryDataState.Loading)
    val dataState: StateFlow<LaundryDataState> = _dataState
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("auth", Context.MODE_PRIVATE)

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
                    val laundryData = response.body() ?: throw(Exception("No data found"))
                    _dataState.value = LaundryDataState.Success(laundryData)
                } else {
                    _dataState.value = LaundryDataState.Error("500", parseError(response)["error"] ?: "Unknown error")
                }
            } catch (e: Exception) {
                _dataState.value = LaundryDataState.Error("407",e.message ?: "Unknown error")
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