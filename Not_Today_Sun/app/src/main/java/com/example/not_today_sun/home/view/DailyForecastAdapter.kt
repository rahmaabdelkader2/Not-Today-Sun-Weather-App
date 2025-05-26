package com.example.not_today_sun.home.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.DailyForecastItemBinding
import java.text.SimpleDateFormat
import java.util.*

data class DailyForecast(
    val date: Long,
    val minTemp: Float,
    val maxTemp: Float,
    val windSpeed: Float
)

class DailyForecastAdapter(
    private val dailyForecasts: List<DailyForecast>,
    private val timezone: Long,
    private val context: Context
) : RecyclerView.Adapter<DailyForecastAdapter.DailyForecastViewHolder>() {

    class DailyForecastViewHolder(private val binding: DailyForecastItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: DailyForecast, timezone: Long, context: Context) {
            val sharedPref = context.getSharedPreferences("WeatherSettings", Context.MODE_PRIVATE)
            val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = forecast.date * 1000 + timezone * 1000
            binding.tvDay.text = dateFormat.format(calendar.time)

            val temperatureUnit = sharedPref.getString("temperature_unit", "standard") ?: "standard"
            val unitSymbol = when (temperatureUnit) {
                "metric" -> "°C"
                "imperial" -> "°F"
                "standard" -> "K"
                else -> "°C"
            }
            binding.tvMinTemp.text = context.getString(
                R.string.temperature_format,
                forecast.minTemp,
                unitSymbol
            )
            binding.tvMaxTemp.text = context.getString(
                R.string.temperature_format,
                forecast.maxTemp,
                unitSymbol
            )


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        val binding = DailyForecastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        holder.bind(dailyForecasts[position], timezone, context)
    }

    override fun getItemCount(): Int = dailyForecasts.size
}