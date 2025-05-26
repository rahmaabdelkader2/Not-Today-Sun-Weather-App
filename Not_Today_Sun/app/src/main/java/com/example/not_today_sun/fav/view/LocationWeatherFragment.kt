package com.example.not_today_sun.fav.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.not_today_sun.MainActivity
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.FragmentHomeBinding
import com.example.not_today_sun.home.HomeViewModel.HomeViewModel
import com.example.not_today_sun.home.view.DailyForecast
import com.example.not_today_sun.home.view.DailyForecastAdapter
import com.example.not_today_sun.home.view.HomeViewModelFactory
import com.example.not_today_sun.home.view.HourlyForecastAdapter
import com.example.not_today_sun.key._apiKey
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.repo.WeatherRepository
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class LocationWeatherFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var repository: WeatherRepository
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var dailyForecastAdapter: DailyForecastAdapter
    private val sharedPref by lazy {
        requireContext().getSharedPreferences("WeatherSettings", Context.MODE_PRIVATE)
    }

    // Factory companion object
    companion object {
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"
        private const val ARG_CITY_NAME = "cityName"

        fun newInstance(latitude: Double, longitude: Double, cityName: String?): LocationWeatherFragment {
            val args = Bundle().apply {
                putDouble(ARG_LATITUDE, latitude)
                putDouble(ARG_LONGITUDE, longitude)
                putString(ARG_CITY_NAME, cityName)
            }
            return LocationWeatherFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = (requireActivity() as MainActivity).weatherRepository
        viewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(repository)
        ).get(HomeViewModel::class.java)

        // Retrieve arguments safely
        val latitude = arguments?.getDouble(ARG_LATITUDE) ?: 0.0
        val longitude = arguments?.getDouble(ARG_LONGITUDE) ?: 0.0
        val cityName = arguments?.getString(ARG_CITY_NAME)

        setupRecyclerViews()

        // Load weather data with preferences
        val temperatureUnit = sharedPref.getString("temperature_unit", "metric") ?: "metric"
        val language = sharedPref.getString("language", "en") ?: "en"
        viewModel.fetchWeatherData(latitude, longitude, _apiKey, units = temperatureUnit, language = language)



        setupObservers()
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

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.weatherContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.currentWeather.observe(viewLifecycleOwner) { weather ->
            weather?.let {
                updateWeatherUI(it)
                viewModel.saveCurrentWeatherToLocal(it)
            }
        }

        viewModel.hourlyForecast.observe(viewLifecycleOwner) { forecast ->
            forecast?.let {
                val calendar = Calendar.getInstance()
                val today = calendar.get(Calendar.DAY_OF_YEAR)
                val timezoneOffsetMillis = forecast.city.timezone * 1000L

                // Hourly forecasts
                val todayForecasts = forecast.list.let { list ->
                    val filtered = list.filter { weatherData ->
                        calendar.timeInMillis = weatherData.dt * 1000L + timezoneOffsetMillis
                        calendar.get(Calendar.DAY_OF_YEAR) == today
                    }
                    if (filtered.isNotEmpty()) filtered.take(24) else list.take(24)
                }

                hourlyForecastAdapter = HourlyForecastAdapter(
                    todayForecasts,
                    forecast.city.timezone.toLong(),
                    viewModel::formatHourlyTime,
                    requireContext()
                )
                binding.rvHourlyForecast.adapter = hourlyForecastAdapter
                binding.rvHourlyForecast.visibility = View.VISIBLE

                // Daily forecasts derived from hourly data
                val dailyForecasts = forecast.list.groupBy { weatherData ->
                    calendar.timeInMillis = weatherData.dt * 1000L + timezoneOffsetMillis
                    calendar.get(Calendar.DAY_OF_YEAR)
                }.mapValues { entry ->
                    val temps = entry.value.map { it.main.temp }
                    val windSpeeds = entry.value.map { it.wind.speed }
                    DailyForecast(
                        date = entry.value.first().dt,
                        minTemp = temps.minOrNull() ?: 0f,
                        maxTemp = temps.maxOrNull() ?: 0f,
                        windSpeed = windSpeeds.maxOrNull() ?: 0f // Compute max wind speed for the day
                    )
                }.values.toList().drop(1)

                dailyForecastAdapter = DailyForecastAdapter(
                    dailyForecasts,
                    forecast.city.timezone.toLong(),
                    requireContext()
                )
                binding.rvDailyForecast.adapter = dailyForecastAdapter

                viewModel.saveHourlyForecastToLocal(forecast)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.weatherContainer, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun updateWeatherUI(weather: CurrentWeatherResponse) {
        binding.tvCityName.text = weather.cityName ?: "Unknown"

        // Apply temperature unit from SharedPreferences
        val temperatureUnit = sharedPref.getString("temperature_unit", "metric") ?: "metric"
        val unitSymbol = when (temperatureUnit) {
            "metric" -> "°C"
            "imperial" -> "°F"
            "standard" -> "K"
            else -> "°C"
        }
        binding.tvTemperature.text = String.format("%.1f%s", weather.main.temperature, unitSymbol)
        // Format date and time with location's timezone
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        val adjustedTime = weather.dateTime * 1000 + weather.timezone * 1000
        binding.tvDateTime.text = dateFormat.format(Date(adjustedTime))

        // Apply wind speed unit from SharedPreferences
        val windSpeedUnit = sharedPref.getString("wind_speed_unit", "m/s") ?: "m/s"
        val windSpeed = if (windSpeedUnit == "mph") {
            weather.wind.speed * 2.23694 // Convert m/s to mph
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
                    binding.ivCurrentWeather.setImageResource(R.drawable.ic_unknown)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}