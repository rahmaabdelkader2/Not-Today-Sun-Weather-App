package com.example.not_today_sun.home.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.HourlyForecastItemBinding
import com.example.not_today_sun.model.pojo.WeatherData

class HourlyForecastAdapter(
    private var forecastList: List<WeatherData>,
    private val timezone: Long,
    private val formatHourlyTime: (Long, Long) -> String
) : RecyclerView.Adapter<HourlyForecastAdapter.ForecastViewHolder>() {

    class ForecastViewHolder(private val binding: HourlyForecastItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: WeatherData, formatHourlyTime: (Long, Long) -> String, timezone: Long) {
            // Format timestamp to display time
            binding.tvHour.text = formatHourlyTime(forecast.dt, timezone)

            // Display temperature with proper formatting
            binding.tvTemperature.text = binding.root.context.getString(
                R.string.temperature_format,
                forecast.main.temp.toInt()
            )

            // Display weather description
            val description = forecast.weather.firstOrNull()?.description
            binding.tvWeatherDescription.text =
                description?.replaceFirstChar { it.uppercase() } ?: ""

            // Load weather icon with Glide
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
                    .error(R.drawable.ic_unknown)
                    .into(binding.ivWeatherIcon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = HourlyForecastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(forecastList[position], formatHourlyTime, timezone)
    }

    override fun getItemCount(): Int = forecastList.size

    fun updateData(newForecastList: List<WeatherData>) {
        forecastList = newForecastList
        notifyDataSetChanged()
    }
}