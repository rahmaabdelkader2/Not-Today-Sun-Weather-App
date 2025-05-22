package com.example.not_today_sun.home.view

import android.Manifest
import android.content.Context
import android.content.Intent
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
        requireContext().getSharedPreferences("InitialSetupPrefs", Context.MODE_PRIVATE)
    }

    private val viewModel: HomeViewModel by viewModels {
        WeatherViewModelFactory((requireActivity() as MainActivity).weatherRepository)
    }

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
                    binding.root,
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
        checkAndTriggerLocation()
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
        val gpsEnabled = sharedPref.getBoolean("gps_enabled", false)
        if (gpsEnabled) {
            checkLocationPermission()
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
                        binding.root,
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

    private fun fetchWeatherWithFallback() {
        val sharedPref = requireActivity().getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        val latitude = sharedPref.getFloat("lastLat", 0f).toDouble()
        val longitude = sharedPref.getFloat("lastLon", 0f).toDouble()

        if (isValidLocation(latitude, longitude)) {
            fetchWeatherData(latitude, longitude)
        } else {
            with(sharedPref.edit()) {
                remove("lastLat")
                remove("lastLon")
                apply()
            }
            if (hasLocationPermission) {
                getCurrentLocation()
            } else {
                fetchWeatherData(51.5074, -0.1278) // Default to London
                Snackbar.make(
                    binding.root,
                    "Unable to get current location. Using London as default. Enable location for accurate weather.",
                    Snackbar.LENGTH_LONG
                ).setAction("Retry") {
                    checkLocationPermission()
                }.show()
            }
        }
    }

    private fun isValidLocation(latitude: Double, longitude: Double): Boolean {
        return latitude != 0.0 && longitude != 0.0 &&
                latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180
    }

    private fun saveLocation(latitude: Double, longitude: Double) {
        val sharedPref = requireActivity().getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putFloat("lastLat", latitude.toFloat())
            putFloat("lastLon", longitude.toFloat())
            apply()
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModel.fetchWeatherData(
            latitude = latitude,
            longitude = longitude,
            apiKey = "a8e1403c5d6bea0cc878a89714ec75ed"
        )
        Log.d("HomeFragment", "Fetching weather for: $latitude, $longitude")
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.weatherContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
        })

        viewModel.currentWeather.observe(viewLifecycleOwner, Observer { weather ->
            weather?.let {
                updateWeatherUI(it)
            }
        })

        viewModel.hourlyForecast.observe(viewLifecycleOwner, Observer { forecast ->
            forecast?.let {
                val calendar = Calendar.getInstance()
                val today = calendar.get(Calendar.DAY_OF_YEAR)
                val timezoneOffsetMillis = forecast.city.timezone * 1000L

                val todayForecasts = forecast.list?.filter { weatherData ->
                    calendar.timeInMillis = weatherData.dt * 1000L + timezoneOffsetMillis
                    calendar.get(Calendar.DAY_OF_YEAR) == today
                }?.take(24) ?: emptyList()

                hourlyForecastAdapter = HourlyForecastAdapter(
                    todayForecasts,
                    forecast.city.timezone.toLong(),
                    viewModel::formatHourlyTime
                )
                binding.rvHourlyForecast.adapter = hourlyForecastAdapter

                val dailyForecasts = forecast.list?.groupBy { weatherData ->
                    calendar.timeInMillis = weatherData.dt * 1000L + timezoneOffsetMillis
                    calendar.get(Calendar.DAY_OF_YEAR)
                }?.mapValues { entry ->
                    val temps = entry.value.map { it.main.temp }
                    DailyForecast(
                        date = entry.value.first().dt,
                        minTemp = temps.minOrNull() ?: 0f,
                        maxTemp = temps.maxOrNull() ?: 0f
                    )
                }?.values?.toList()?.drop(1) ?: emptyList()

                dailyForecastAdapter = DailyForecastAdapter(
                    dailyForecasts,
                    forecast.city.timezone.toLong()
                )
                binding.rvDailyForecast.adapter = dailyForecastAdapter
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun updateWeatherUI(weather: CurrentWeatherResponse) {
        binding.tvCityName.text = weather.cityName ?: "Unknown"
        binding.tvTemperature.text = String.format("%.1f°C", weather.main.temperature)

        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = weather.dateTime * 1000 + weather.timezone * 1000
        binding.tvDateTime.text = dateFormat.format(calendar.time)

        binding.tvHumidity.text = "Humidity: ${weather.main.humidity}%"
        binding.tvWind.text = "Wind: ${weather.wind.speed} m/s"
        binding.tvPressure.text = "Pressure: ${weather.main.pressure} hPa"
        binding.tvClouds.text = "Clouds: ${weather.clouds.cloudiness}%"

        weather.weather.firstOrNull()?.let { weatherDesc ->
            binding.tvWeatherDescription.text = weatherDesc.description?.replaceFirstChar {
                it.uppercaseChar()
            } ?: "N/A"

            weatherDesc.icon?.let { iconCode ->
                Glide.with(this)
                    .load("https://openweathermap.org/img/wn/$iconCode@2x.png")
                    .error(R.drawable.ic_unknown)
                    .into(binding.ivCurrentWeather)
            } ?: binding.ivCurrentWeather.setImageResource(R.drawable.ic_unknown)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }
}