package com.example.laundrytallyai.pages.laundries

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laundrytallyai.api.RetrofitClient
import com.example.laundrytallyai.api.dataschemes.ClothesData
import com.example.laundrytallyai.api.dataschemes.LaundryData
import com.example.laundrytallyai.api.datastates.LaundryDataState
import com.example.laundrytallyai.api.parseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

    @RequiresApi(Build.VERSION_CODES.P)
    fun downloadImage(context: Context, imageUrl: String, onDownloaded: (Uri) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder().url(imageUrl).build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val file = File(context.cacheDir, "shared_image.jpg")
                val fos = FileOutputStream(file)
                fos.use { fos ->
                    response.body?.byteStream()?.copyTo(fos)
                }
                val fileUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                withContext(Dispatchers.Main) {
                    onDownloaded(fileUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}