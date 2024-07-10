package com.example.laundrytallyai.pages.home

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laundrytallyai.api.RetrofitClient
import com.example.laundrytallyai.api.dataschemes.HomeData
import com.example.laundrytallyai.api.datastates.HomeDataState
import com.example.laundrytallyai.api.parseError
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application
) : ViewModel() {
    private val _dataState = MutableStateFlow<HomeDataState>(HomeDataState.Loading)
    val dataState: StateFlow<HomeDataState> = _dataState
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun fetchData() {
        viewModelScope.launch {
            _dataState.value = HomeDataState.Loading
            try {
                val token = sharedPreferences.getString("token", null) ?: run {
                    _dataState.value = HomeDataState.Error("No auth token found")
                    return@launch
                }
                val response = RetrofitClient.instance.getHomeData(token)
                if (response.isSuccessful) {
                    val homeData = response.body() ?: throw(Exception("No data found"))
                    _dataState.value = HomeDataState.Success(homeData)
                } else {
                    _dataState.value = HomeDataState.Error(parseError(response)["code"] ?: "Unknown error")
                }
            } catch (e: Exception) {
                _dataState.value = HomeDataState.Error(e.message ?: "Unknown error")
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