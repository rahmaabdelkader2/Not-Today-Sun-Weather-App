//package com.example.not_today_sun.OSM
//
//import android.content.Context
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import org.json.JSONObject
//import org.osmdroid.util.GeoPoint
//import java.io.IOException
//import java.util.concurrent.TimeUnit
//
//data class LocationData(
//    val latitude: Double? = null,
//    val longitude: Double? = null,
//    val placeName: String? = null
//)
//
//class MapMarkerViewModel : ViewModel() {
//
//    private val _locationData = MutableLiveData<LocationData>()
//    val locationData: LiveData<LocationData> = _locationData
//
//    private val _toastMessage = MutableLiveData<String>()
//    val toastMessage: LiveData<String> = _toastMessage
//
//    private val okHttpClient = OkHttpClient()
//    private var lastGeocodeTime = 0L
//    private val geocodeDebounceMs = TimeUnit.SECONDS.toMillis(2) // 2-second debounce
//
//    fun setLocation(position: GeoPoint, context: Context) {
//        _locationData.value = LocationData(
//            latitude = position.latitude,
//            longitude = position.longitude,
//            placeName = ""
//        )
//        reverseGeocode(position, context)
//    }
//
//    fun saveLocation(placeName: String, lat: String, lng: String) {
//        if (lat.isEmpty() || lng.isEmpty()) {
//            _toastMessage.value = "Please select a location"
//            return
//        }
//
//        try {
//            val latitude = lat.toDouble()
//            val longitude = lng.toDouble()
//            val displayName = if (placeName.isEmpty()) "Unknown" else placeName
//
//            // Here you would typically save to a database
//            _toastMessage.value = "Saved: $displayName\nLat: $latitude\nLng: $longitude"
//            _locationData.value = LocationData() // Clear data after saving
//        } catch (e: NumberFormatException) {
//            _toastMessage.value = "Invalid coordinates"
//        }
//    }
//
//    private fun reverseGeocode(position: GeoPoint, context: Context) {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastGeocodeTime < geocodeDebounceMs) {
//            _locationData.value = _locationData.value?.copy(placeName = "")
//            return
//        }
//        lastGeocodeTime = currentTime
//
//        viewModelScope.launch {
//            try {
//                // Check cache first
//                val cacheKey = "${position.latitude}_${position.longitude}"
//                val sharedPrefs = context.getSharedPreferences("geocode_cache", Context.MODE_PRIVATE)
//                val cachedPlaceName = sharedPrefs.getString(cacheKey, null)
//                if (cachedPlaceName != null) {
//                    _locationData.value = _locationData.value?.copy(placeName = cachedPlaceName)
//                    return@launch
//                }
//
//                // Perform network request
//                val placeName = withContext(Dispatchers.IO) {
//                    val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=${position.latitude}&lon=${position.longitude}&zoom=18&addressdetails=1"
//                    val request = Request.Builder()
//                        .url(url)
//                        .header("User-Agent", "TryMapApp/1.0 (abdelkaderrahma7@gmail.com)")
//                        .build()
//
//                    okHttpClient.newCall(request).execute().use { response ->
//                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
//                        val json = response.body?.string() ?: throw IOException("Empty response")
//                        val jsonObject = JSONObject(json)
//                        jsonObject.getString("display_name")
//                    }
//                }
//
//                // Cache the result
//                sharedPrefs.edit().putString(cacheKey, placeName).apply()
//                _locationData.value = _locationData.value?.copy(placeName = placeName)
//            } catch (e: Exception) {
//                _toastMessage.value = "Failed to get place name"
//                _locationData.value = _locationData.value?.copy(placeName = "")
//            }
//        }
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                okHttpClient.dispatcher.executorService.shutdown()
//                okHttpClient.connectionPool.evictAll()
//            } catch (e: Exception) {
//                // Log error to avoid crashes
//                e.printStackTrace()
//            }
//        }
//    }
//}