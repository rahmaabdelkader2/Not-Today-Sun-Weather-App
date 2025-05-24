package com.example.not_today_sun.fav.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.not_today_sun.key._apiKey
import com.example.not_today_sun.model.pojo.FavoriteLocation
import com.example.not_today_sun.model.repo.WeatherRepository
import kotlinx.coroutines.launch

class FavViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val TAG = "FavViewModel"
    private val _favoriteLocations = MutableLiveData<List<FavoriteLocation>>()
    val favoriteLocations: LiveData<List<FavoriteLocation>> = _favoriteLocations

    private val _navigateToMap = MutableLiveData<Boolean>()
    val navigateToMap: LiveData<Boolean> = _navigateToMap

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    fun getAllFavoriteLocations() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAllFavoriteLocations()
                _favoriteLocations.value = result
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error loading locations: ${e.message}"
            }finally {
                _isLoading.value = false
            }
        }
    }

    fun addLocationToFavorites(longitude: Double, latitude: Double) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getCurrentWeather(latitude, longitude, _apiKey)
                result.getOrNull()?.let {
                    val location = FavoriteLocation(
                        cityName = it.cityName,
                        latitude = latitude,
                        longitude = longitude,
                        maxTemp = it.main?.tempMax,
                        minTemp = it.main?.tempMin
                    )
                    repository.saveFavoriteLocation(location)
                    getAllFavoriteLocations()
                } ?: run {
                    _errorMessage.value = "Failed to fetch weather data"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch weather data: ${e.message}"
            }
        }
    }

    fun addNewLocation() {
        _navigateToMap.value = true
    }

    fun onNavigationComplete() {
        _navigateToMap.value = false
    }

    fun deleteLocation(location: FavoriteLocation) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.deleteFavoriteLocation(location)
                getAllFavoriteLocations()
                _successMessage.value = "${location.cityName} deleted successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete location: ${e.message}"
            }
        }
    }



//    fun loadFavoriteLocationsWithWeather() {
//        _isLoading.value = true
//        viewModelScope.launch {
//            try {
//                Log.e(TAG, "Starting to load locations")
//                val result = repository.getAllFavoriteLocations()
//                if (result.isSuccess) {
//                    val locations = result.getOrThrow()
//                    Log.e(TAG, "Retrieved ${locations.size} locations from DB")
//
////                    // Optionally refresh weather data for each location
////                    val updatedLocations = locations.map { location ->
////                        fetchWeatherForLocation(location, apiKey)
////                    }
//
////                    _favoriteLocations.value = updatedLocations
////                    Log.e(TAG, "Updated LiveData with ${updatedLocations.size} items")
//                } else {
//                    Log.e(TAG, "Failed to load locations", result.exceptionOrNull())
//                    _errorMessage.value = "Failed to load locations"
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Critical error in loadFavoriteLocationsWithWeather", e)
//                _errorMessage.value = "Error loading locations: ${e.message}"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }

//    private suspend fun fetchWeatherForLocation(location: FavoriteLocation, apiKey: String): FavoriteLocation {
//        return try {
//            val weatherResult = repository.getCurrentWeather(
//                latitude = location.latitude,
//                longitude = location.longitude,
//                apiKey = apiKey
//            )
//            if (weatherResult.isSuccess) {
//                val weather = weatherResult.getOrThrow()
//                location.copy(
//                    maxTemp = weather.main?.tempMax,
//                    minTemp = weather.main?.tempMin
//                )
//            } else {
//                Log.w(TAG, "Failed to fetch weather for ${location.cityName}", weatherResult.exceptionOrNull())
//                location // Return original if failed
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error fetching weather for ${location.cityName}", e)
//            location // Return original if failed
//        }
//    }
}