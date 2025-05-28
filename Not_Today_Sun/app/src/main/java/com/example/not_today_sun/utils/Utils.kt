import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {

    fun formatDate(timestamp: Long, timezoneOffset: Long): String {
        return try {
            val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
            val adjustedTime = timestamp * 1000 + timezoneOffset * 1000
            dateFormat.format(Date(adjustedTime))
        } catch (e: Exception) {
            Log.e("TimeFormatter", "Error formatting date", e)
            "Unknown"
        }
    }
}