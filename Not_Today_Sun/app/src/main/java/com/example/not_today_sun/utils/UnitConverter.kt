package com.example.not_today_sun.utils


object UnitConverter {
    fun convertTemperature(value: Double, fromUnit: String, toUnit: String): Double {
        return when {
            fromUnit == toUnit -> value
            fromUnit == "metric" && toUnit == "imperial" -> value * 9 / 5 + 32 // C to F
            fromUnit == "metric" && toUnit == "standard" -> value + 273.15 // C to K
            fromUnit == "imperial" && toUnit == "metric" -> (value - 32) * 5 / 9 // F to C
            fromUnit == "imperial" && toUnit == "standard" -> (value - 32) * 5 / 9 + 273.15 // F to K
            fromUnit == "standard" && toUnit == "metric" -> value - 273.15 // K to C
            fromUnit == "standard" && toUnit == "imperial" -> (value - 273.15) * 9 / 5 + 32 // K to F
            else -> value
        }
    }

    fun convertWindSpeed(value: Double, fromUnit: String, toUnit: String): Double {
        return when {
            fromUnit == toUnit -> value
            fromUnit == "m/s" && toUnit == "mph" -> value * 2.23694 // m/s to mph
            fromUnit == "mph" && toUnit == "m/s" -> value / 2.23694 // mph to m/s
            else -> value
        }
    }

    fun getTemperatureUnitSymbol(unit: String): String {
        return when (unit) {
            "metric" -> "°C"
            "imperial" -> "°F"
            "standard" -> "K"
            else -> "°C"
        }
    }

    // Get unit symbol for wind speed
    fun getWindSpeedUnitSymbol(unit: String): String {
        return when (unit) {
            "m/s" -> "m/s"
            "mph" -> "mph"
            else -> "m/s"
        }
    }
}