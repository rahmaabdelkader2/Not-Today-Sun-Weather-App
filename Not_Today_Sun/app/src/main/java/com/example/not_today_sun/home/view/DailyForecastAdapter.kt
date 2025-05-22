package com.example.not_today_sun.home.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.not_today_sun.databinding.DailyForecastItemBinding
import java.text.SimpleDateFormat
import java.util.*

data class DailyForecast(
    val date: Long,
    val minTemp: Float,
    val maxTemp: Float
)

class DailyForecastAdapter(
    private val dailyForecasts: List<DailyForecast>,
    private val timezone: Long
) : RecyclerView.Adapter<DailyForecastAdapter.DailyForecastViewHolder>() {

    class DailyForecastViewHolder(private val binding: DailyForecastItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: DailyForecast, timezone: Long) {
            // Format date
            val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = forecast.date * 1000 + timezone * 1000
            binding.tvDay.text = dateFormat.format(calendar.time)

            // Display min and max temperatures
            binding.tvMinTemp.text = String.format("%.1f°C", forecast.minTemp)
            binding.tvMaxTemp.text = String.format("%.1f°C", forecast.maxTemp)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        val binding = DailyForecastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        holder.bind(dailyForecasts[position], timezone)
    }

    override fun getItemCount(): Int = dailyForecasts.size
}