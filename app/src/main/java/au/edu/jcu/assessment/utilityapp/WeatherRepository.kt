package au.edu.jcu.assessment.utilityapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class HourForecast(val time: String, val temperature: Double, val description: String)

data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val description: String,
    val updatedAt: String,
    val hourly: List<HourForecast>
)

class WeatherRepository {
    suspend fun getWeather(): CurrentWeather = withContext(Dispatchers.IO) {
        val endpoint = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=-16.9186&longitude=145.7781" +
            "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m" +
            "&hourly=temperature_2m,precipitation_probability,weather_code" +
            "&forecast_days=2&timezone=Australia%2FBrisbane"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (connection.responseCode !in 200..299) throw IllegalStateException("Weather service returned ${connection.responseCode}.")
            val json = connection.inputStream.bufferedReader().use { it.readText() }
            parseWeather(JSONObject(json))
        } finally {
            connection.disconnect()
        }
    }

    private fun parseWeather(json: JSONObject): CurrentWeather {
        val current = json.getJSONObject("current")
        val hourly = json.getJSONObject("hourly")
        val times = hourly.getJSONArray("time")
        val temperatures = hourly.getJSONArray("temperature_2m")
        val codes = hourly.getJSONArray("weather_code")
        val now = LocalDateTime.parse(current.getString("time"))
        val startIndex = (0 until times.length()).firstOrNull {
            !LocalDateTime.parse(times.getString(it)).isBefore(now.withMinute(0).withSecond(0).withNano(0))
        } ?: 0
        val hours = (startIndex until minOf(startIndex + 6, times.length())).map { index ->
            val dateTime = LocalDateTime.parse(times.getString(index))
            HourForecast(dateTime.format(DateTimeFormatter.ofPattern("ha")), temperatures.getDouble(index), describe(codes.getInt(index)))
        }
        return CurrentWeather(
            temperature = current.getDouble("temperature_2m"),
            feelsLike = current.getDouble("apparent_temperature"),
            humidity = current.getInt("relative_humidity_2m"),
            windSpeed = current.getDouble("wind_speed_10m"),
            description = describe(current.getInt("weather_code")),
            updatedAt = now.format(DateTimeFormatter.ofPattern("h:mm a")),
            hourly = hours
        )
    }

    private fun describe(code: Int): String = when (code) {
        0 -> "Clear sky"
        1 -> "Mostly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Light drizzle"
        56, 57 -> "Freezing drizzle"
        61, 63 -> "Rain"
        65 -> "Heavy rain"
        66, 67 -> "Freezing rain"
        71, 73, 75, 77 -> "Snow"
        80, 81 -> "Rain showers"
        82 -> "Heavy showers"
        85, 86 -> "Snow showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Current conditions"
    }
}
