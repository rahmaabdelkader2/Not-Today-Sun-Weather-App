package com.example.not_today_sun.home.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.not_today_sun.MainActivity
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.FragmentHomeBinding
import com.example.not_today_sun.home.HomeViewModel.HomeViewModel
import com.example.not_today_sun.key._apiKey
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var dailyForecastAdapter: DailyForecastAdapter
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var hasLocationPermission = false
    private val sharedPref by lazy {
        requireContext().getSharedPreferences("WeatherSettings", Context.MODE_PRIVATE)
    }
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((requireActivity() as MainActivity).weatherRepository)
    }
    private lateinit var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                hasLocationPermission = true
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                hasLocationPermission = true
                getCurrentLocation()
            }
            else -> {
                Snackbar.make(
                    binding.weatherContainer,
                    "Location permission denied. Using default location.",
                    Snackbar.LENGTH_LONG
                ).setAction("Retry") {
                    checkLocationPermission()
                }.show()
                fetchWeatherWithFallback()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupObservers()
        setupPreferenceChangeListener()
        checkAndTriggerLocation()
    }

    private fun setupPreferenceChangeListener() {
        preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "use_gps", "use_map", "map_lat", "map_lon", "temperature_unit", "wind_speed_unit", "language" -> {
                    Log.d("HomeFragment", "Preference changed: $key, refreshing weather data")
                    checkAndTriggerLocation()
                }
            }
        }
        sharedPref.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun setupRecyclerViews() {
        binding.rvHourlyForecast.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
        binding.rvDailyForecast.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    private fun checkAndTriggerLocation() {
        val useGps = sharedPref.getBoolean("use_gps", false)
        val useMap = sharedPref.getBoolean("use_map", false)
        if (useGps) {
            checkLocationPermission()
        } else if (useMap) {
            fetchWeatherWithMapLocation()
        } else {
            fetchWeatherWithFallback()
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasLocationPermission = true
                getCurrentLocation()
            }
            else -> {
                showPermissionRationale()
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Needed")
            .setMessage("This app needs location permission to show weather for your current location.")
            .setPositiveButton("OK") { _, _ ->
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                fetchWeatherWithFallback()
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(
                        binding.weatherContainer,
                        "Location permission permanently denied. Go to Settings to enable.",
                        Snackbar.LENGTH_LONG
                    ).setAction("Settings") {
                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", requireContext().packageName, null)
                        })
                    }.show()
                }
            }
            .create()
            .show()
    }

    private fun getCurrentLocation() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showEnableGpsDialog()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    saveLocation(location.latitude, location.longitude)
                    fetchWeatherData(location.latitude, location.longitude)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }

        if (hasLocationPermission) {
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        saveLocation(it.latitude, it.longitude)
                        fetchWeatherData(it.latitude, it.longitude)
                    }
                }.addOnFailureListener { exception ->
                    if (exception is SecurityException) {
                        Log.e("HomeFragment", "Location permission revoked", exception)
                        fetchWeatherWithFallback()
                    }
                }
            } catch (e: SecurityException) {
                Log.e("HomeFragment", "SecurityException on location request", e)
                fetchWeatherWithFallback()
            }
        } else {
            checkLocationPermission()
        }
    }

    private fun showEnableGpsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Enable GPS")
            .setMessage("GPS is required for accurate location")
            .setPositiveButton("Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                fetchWeatherWithFallback()
            }
            .show()
    }

    private fun fetchWeatherWithMapLocation() {
        val latitude = sharedPref.getFloat("map_lat", 0f).toDouble()
        val longitude = sharedPref.getFloat("map_lon", 0f).toDouble()

        if (isValidLocation(latitude, longitude)) {
            Log.d("HomeFragment", "Fetching weather with map location: $latitude, $longitude")
            fetchWeatherData(latitude, longitude)
        } else {
            Log.e("HomeFragment", "Invalid map location coordinates")
            with(sharedPref.edit()) {
                remove("map_lat")
                remove("map_lon")
                remove("map_location_name")
                apply()
            }
            fetchWeatherWithFallback()
        }
    }

    private fun fetchWeatherWithFallback() {
        fetchWeatherData(51.5074, -0.1278)
        Snackbar.make(
            binding.weatherContainer,
            "Unable to get location. Using London as default.",
            Snackbar.LENGTH_LONG
        ).setAction("Retry") {
            checkAndTriggerLocation()
        }.show()
    }

    private fun isValidLocation(latitude: Double, longitude: Double): Boolean {
        return latitude != 0.0 && longitude != 0.0 &&
                latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180
    }

    private fun saveLocation(latitude: Double, longitude: Double) {
        with(sharedPref.edit()) {
            putFloat("map_lat", latitude.toFloat())
            putFloat("map_lon", longitude.toFloat())
            apply()
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val temperatureUnit = sharedPref.getString("temperature_unit", "metric") ?: "metric"
        val language = sharedPref.getString("language", "en") ?: "en"

        viewModel.fetchWeatherData(
            latitude = latitude,
            longitude = longitude,
            apiKey = _apiKey,
            units = temperatureUnit,
            language = language
        )
        Log.d("HomeFragment", "Fetching weather for: $latitude, $longitude, units: $temperatureUnit, language: $language")
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.weatherContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
        })

        viewModel.currentWeather.observe(viewLifecycleOwner, Observer { weather ->
            weather?.let {
                updateWeatherUI(it)
                viewModel.saveCurrentWeatherToLocal(it)
            }
        })

        viewModel.hourlyForecast.observe(viewLifecycleOwner, Observer { forecast ->
            forecast?.let {
                Log.d("HomeFragment", "Raw hourly forecast count: ${forecast.list?.size ?: 0}")

                val calendar = Calendar.getInstance()
                val today = calendar.get(Calendar.DAY_OF_YEAR)
                val timezoneOffsetMillis = forecast.city.timezone * 1000L

                val todayForecasts = forecast.list?.let { list ->
                    val filtered = list.filter { weatherData ->
                        calendar.timeInMillis = weatherData.dt * 1000L + timezoneOffsetMillis
                        calendar.get(Calendar.DAY_OF_YEAR) == today
                    }
                    if (filtered.isNotEmpty()) filtered.take(24) else list.take(24)
                } ?: emptyList()

                Log.d("HomeFragment", "Displaying hourly forecast count: ${todayForecasts.size}")

                hourlyForecastAdapter = HourlyForecastAdapter(
                    todayForecasts,
                    forecast.city.timezone.toLong(),
                    viewModel::formatHourlyTime,
                    requireContext()
                )
                binding.rvHourlyForecast.adapter = hourlyForecastAdapter
                binding.rvHourlyForecast.visibility = View.VISIBLE

                val dailyForecasts = forecast.list?.groupBy { weatherData ->
                    calendar.timeInMillis = weatherData.dt * 1000L + timezoneOffsetMillis
                    calendar.get(Calendar.DAY_OF_YEAR)
                }?.mapValues { entry ->
                    val temps = entry.value.map { it.main.temp }
                    DailyForecast(
                        date = entry.value.first().dt,
                        minTemp = temps.minOrNull() ?: 0f,
                        maxTemp = temps.maxOrNull() ?: 0f,
                        windSpeed = entry.value.maxOfOrNull { it.wind.speed } ?: 0f
                    )
                }?.values?.toList()?.drop(1) ?: emptyList()

                dailyForecastAdapter = DailyForecastAdapter(
                    dailyForecasts,
                    forecast.city.timezone.toLong(),
                    requireContext()
                )
                binding.rvDailyForecast.adapter = dailyForecastAdapter

                viewModel.saveHourlyForecastToLocal(forecast)
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Snackbar.make(binding.weatherContainer, it, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun updateWeatherUI(weather: CurrentWeatherResponse) {
        binding.tvCityName.text = weather.cityName ?: "Unknown"
        val temperatureUnit = sharedPref.getString("temperature_unit", "metric") ?: "metric"
        val unitSymbol = when (temperatureUnit) {
            "metric" -> "°C"
            "imperial" -> "°F"
            "standard" -> "K"
            else -> "°C"
        }
        binding.tvTemperature.text = String.format("%.1f%s", weather.main.temperature, unitSymbol)
        binding.tvTemperature.text = String.format("%.1f%s", weather.main.temperature, unitSymbol)

        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val adjustedTime = weather.dateTime * 1000 + weather.timezone * 1000
        val formattedDate = dateFormat.format(Date(adjustedTime))
        val formattedTime = viewModel.formatHourlyTime(weather.dateTime, weather.timezone.toLong())
        binding.tvDateTime.text = "$formattedDate at $formattedTime"

        val windSpeedUnit = sharedPref.getString("wind_speed_unit", "m/s") ?: "m/s"
        val windSpeed = if (windSpeedUnit == "mph") {
            weather.wind.speed * 2.23694
        } else {
            weather.wind.speed
        }
        binding.tvWind.text = String.format("Wind: %.1f %s", windSpeed, windSpeedUnit)

        binding.tvHumidity.text = "Humidity: ${weather.main.humidity}%"
        binding.tvPressure.text = "Pressure: ${weather.main.pressure} hPa"
        binding.tvClouds.text = "Clouds: ${weather.clouds.cloudiness}%"

        weather.weather.firstOrNull()?.let { weatherDesc ->
            binding.tvWeatherDescription.text = weatherDesc.description?.replaceFirstChar {
                it.uppercaseChar()
            } ?: "N/A"

            weatherDesc.icon?.let { iconCode ->
                val iconUrl = "ic_$iconCode"
                val icon = context?.resources?.getIdentifier(
                    iconUrl, "drawable", context?.packageName
                ) ?: 0
                if (icon != 0) {
                    Glide.with(this)
                        .load(icon)
                        .error(R.drawable.ic_unknown)
                        .into(binding.ivCurrentWeather)
                } else {
                    Glide.with(this)
                        .load(R.drawable.ic_unknown)
                        .into(binding.ivCurrentWeather)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedPref.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }
}