package com.example.not_today_sun.home.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.HourlyForecastItemBinding
import com.example.not_today_sun.model.pojo.WeatherData

class HourlyForecastAdapter(
    private val forecastList: List<WeatherData>,
    private val timezone: Long,
    private val formatHourlyTime: (Long, Long) -> String,
    private val context: Context
) : RecyclerView.Adapter<HourlyForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(private val binding: HourlyForecastItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: WeatherData, formatHourlyTime: (Long, Long) -> String, timezone: Long, context: Context) {
            val sharedPref = context.getSharedPreferences("WeatherSettings", Context.MODE_PRIVATE)
            binding.tvHour.text = formatHourlyTime(forecast.dt, timezone)

            val temperatureUnit = sharedPref.getString("temperature_unit", "metric") ?: "metric"
            val unitSymbol = when (temperatureUnit) {
                "metric" -> "°C"
                "imperial" -> "°F"
                "standard" -> "K"
                else -> "°C"
            }
            binding.tvTemperature.text = context.getString(
                R.string.temperature_format,
                forecast.main.temp,
                unitSymbol
            )

            val windSpeedUnit = sharedPref.getString("wind_speed_unit", "m/s") ?: "m/s"
            val windSpeed = if (windSpeedUnit == "mph") {
                forecast.wind.speed * 2.23694f
            } else {
                forecast.wind.speed
            }
            binding.tvWindSpeed.text = context.getString(
                R.string.wind_speed_format,
                windSpeed,
                windSpeedUnit
            )

            val description = forecast.weather.firstOrNull()?.description
            binding.tvWeatherDescription.text =
                description?.replaceFirstChar { it.uppercase() } ?: ""

            val iconCode = forecast.weather.firstOrNull()?.icon ?: ""
            val iconUrl = "ic_$iconCode"
            val icon = binding.root.context.resources.getIdentifier(
                iconUrl, "drawable", binding.root.context.packageName
            ) ?: 0
            if (icon != 0) {
                Glide.with(binding.root.context)
                    .load(icon)
                    .error(R.drawable.ic_unknown)
                    .into(binding.ivWeatherIcon)
            } else {
                Glide.with(binding.root.context)
                    .load(R.drawable.ic_unknown)
                    .into(binding.ivWeatherIcon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = HourlyForecastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(forecastList[position], formatHourlyTime, timezone, context)
    }

    override fun getItemCount(): Int = forecastList.size
}