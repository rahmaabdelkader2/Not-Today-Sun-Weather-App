package com.example.not_today_sun.home.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.DailyForecastItemBinding
import com.example.not_today_sun.model.pojo.WeatherData

class DailyForecastAdapter(
    private val timezone: Long,
    private val context: Context
) : ListAdapter<WeatherData, DailyForecastAdapter.DailyForecastViewHolder>(WeatherDataDiffCallback()) {

    class DailyForecastViewHolder(
        private val binding: DailyForecastItemBinding,
        private val context: Context,
        private val timezone: Long
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(weatherData: WeatherData) {
            val sharedPref = context.getSharedPreferences("WeatherSettings", Context.MODE_PRIVATE)

            binding.tvDay.text = TimeFormatter.formatDate(weatherData.dt, timezone)

            val temperatureUnit = sharedPref.getString("temperature_unit", "metric") ?: "metric"
            val unitSymbol = when (temperatureUnit) {
                "metric" -> "°C"
                "imperial" -> "°F"
                "standard" -> "K"
                else -> "°C"
            }

            binding.tvMinTemp.text = context.getString(
                R.string.temperature_format,
                weatherData.main.tempMin,
                unitSymbol
            )
            binding.tvMaxTemp.text = context.getString(
                R.string.temperature_format,
                weatherData.main.tempMax,
                unitSymbol
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        val binding = DailyForecastItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DailyForecastViewHolder(binding, context, timezone)
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WeatherDataDiffCallback : DiffUtil.ItemCallback<WeatherData>() {
        override fun areItemsTheSame(oldItem: WeatherData, newItem: WeatherData): Boolean {
            return oldItem.dt == newItem.dt
        }

        override fun areContentsTheSame(oldItem: WeatherData, newItem: WeatherData): Boolean {
            return oldItem == newItem
        }
    }
}