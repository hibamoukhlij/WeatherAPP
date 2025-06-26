package com.example.myweatherapp

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import com.google.android.gms.location.LocationServices
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class WeatherViewModel : ViewModel() {
    private val _weatherData = mutableStateOf<WeatherResponse?>(null)
    val weatherData: State<WeatherResponse?> = _weatherData

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchWeatherByCity(city: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = WeatherApi.retrofitService.getWeatherByCity(city)
                if (response.isSuccessful) {
                    _weatherData.value = response.body()
                } else {
                    _error.value = when (response.code()) {
                        401 -> "Invalid API key"
                        404 -> "City not found"
                        429 -> "Too many requests"
                        500 -> "Server error"
                        else -> "Failed to fetch weather: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = when (e) {
                    is SocketTimeoutException -> "Request timed out"
                    is UnknownHostException -> "No internet connection"
                    else -> "Failed to fetch weather: ${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchWeatherByLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _weatherData.value = WeatherApi.retrofitService.getWeatherByLocation(lat, lon)
            } catch (e: Exception) {
                _error.value = "Failed to fetch weather: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun FetchLocation(context: Context) {
        val locationClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            locationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    fetchWeatherByLocation(it.latitude, it.longitude)
                }
            }.addOnFailureListener { e ->
                _error.value = "Location error: ${e.message}"
            }
        } catch (e: SecurityException) {
            _error.value = "Location permission denied"
        }
    }
}