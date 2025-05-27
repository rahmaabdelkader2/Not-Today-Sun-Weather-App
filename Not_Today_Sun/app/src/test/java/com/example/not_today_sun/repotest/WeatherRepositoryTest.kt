package com.example.not_today_sun.repotest

import com.example.not_today_sun.model.local.LocalDataSource
import com.example.not_today_sun.model.pojo.*
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse
import com.example.not_today_sun.model.remote.RemoteDataSource
import com.example.not_today_sun.model.repo.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.KMutableProperty1

class WeatherRepositoryTest {

    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var localDataSource: LocalDataSource
    private lateinit var repository: WeatherRepository

    // Mock data for HourlyForecastResponse
    private val mockHourlyForecastResponse = HourlyForecastResponse(
        id = 0,
        cod = "200",
        message = 0,
        cnt = 24,
        list = listOf(
            WeatherData(
                dt = 1661871600,
                main = Main(
                    temp = 296.76f, // Changed to Float to match Main
                    feelsLike = 296.98f, // Added, required by Main, Float
                    tempMin = 296.76f, // Added, required by Main, Float
                    tempMax = 297.87f, // Added, required by Main, Float
                    pressure = 1015,
                    seaLevel = 1015, // Added, required by Main
                    grndLevel = 933, // Added, required by Main
                    humidity = 69,
                    tempKf = -1.11f // Added, required by Main, Float
                ),
                weather = listOf(
                    Weather(
                        id = 500,
                        main = "Rain",
                        description = "light rain",
                        icon = "10d"
                    )
                ),
                clouds = Clouds(all = 100),
                wind = Wind(
                    speed = 0.62f, // Changed to Float to match Wind
                    deg = 349,
                    gust = 1.18f // Changed to Float to match Wind
                ),
                visibility = 10000,
                pop = 0.32f,
                sys = Sys(pod = "d"),
                dtTxt = "2022-08-30 15:00:00"
            )
        ),
        city = City(
            id = 3163858,
            name = "Zocca",
            coord = Coord(lat = 44.34f, lon = 10.99f), // Changed to Coord, Float
            country = "IT",
            population = 4593,
            timezone = 7200,
            sunrise = 1661834187,
            sunset = 1661882248
        )
    )

    // Mock data for CurrentWeatherResponse
    private val mockCurrentWeatherResponse = CurrentWeatherResponse(
        dbId = 0,
        coordinates = Coordinates(latitude = 44.34, longitude = 10.99), // Updated to match Coordinates
        weather = listOf(
            Weather(
                id = 500,
                main = "Rain",
                description = "light rain",
                icon = "10d"
            )
        ),
        base = "stations",
        main = CurrentMain(
            temperature = 296.76, // Changed from temp to temperature, Double
            feelsLike = 296.98, // Added, required by CurrentMain, Double
            tempMin = 296.76, // Added, required by CurrentMain, Double
            tempMax = 297.87, // Added, required by CurrentMain, Double
            pressure = 1015,
            humidity = 69,
            seaLevel = 1015, // Changed to nullable Int? in CurrentMain
            groundLevel = 933 // Changed from grndLevel to groundLevel, Int?
        ),
        visibility = 10000,
        wind = CurrentWind(
            speed = 0.62,
            direction = 349, // Changed from deg to direction
            gustSpeed = 1.18 // Changed from gust to gustSpeed, Double?
        ),
        clouds = CurrentClouds(cloudiness = 100), // Changed from all to cloudiness
        dateTime = 1661871600,
        sys = CurrentSys(
            type = 1,
            id = 201,
            country = "IT",
            sunrise = 1661834187,
            sunset = 1661882248
        ),
        timezone = 7200,
        cityId = 3163858,
        cityName = "Zocca",
        statusCode = 200
    )

    private fun resetRepoInstanceByReflection() {
        val companionInstance = WeatherRepository::class.companionObjectInstance
        val companion = WeatherRepository::class.companionObject

        if (companion != null && companionInstance != null) {
            val instanceProperty = companion.declaredMemberProperties
                .firstOrNull { it.name == "instance" } as? KMutableProperty1<Any, Any?>

            instanceProperty?.let {
                it.isAccessible = true
                it.set(companionInstance, null)
            }
        }
    }

    @Before
    fun setUp() {
        resetRepoInstanceByReflection()
        remoteDataSource = mockk(relaxed = true)
        localDataSource = mockk(relaxed = true)
        repository = WeatherRepository.getInstance(remoteDataSource, localDataSource)
    }

    @After
    fun tearDown() {
        resetRepoInstanceByReflection()
    }

    // Tests for getHourlyForecast
    @Test
    fun getHourlyForecast_withValidInputs_returns_success() = runTest {
        coEvery { remoteDataSource.getHourlyForecast(44.34, 10.99, "valid_api_key", "metric", "en", null) } returns Result.success(mockHourlyForecastResponse)

        val result = repository.getHourlyForecast(44.34, 10.99, "valid_api_key")

        Assertions.assertTrue(result.isSuccess)
        Assertions.assertEquals(mockHourlyForecastResponse, result.getOrNull())
        coVerify { remoteDataSource.getHourlyForecast(44.34, 10.99, "valid_api_key", "metric", "en", null) }
    }

    @Test
    fun getHourlyForecast_withInvalidCoordinates_returnsFailure() = runTest {
        val result = repository.getHourlyForecast(100.0, 10.99, "valid_api_key")

        Assertions.assertTrue(result.isFailure)
        Assertions.assertEquals("Invalid coordinates", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { remoteDataSource.getHourlyForecast(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun getHourlyForecast_withEmptyAPIKey_returnsFailure() = runTest {
        val result = repository.getHourlyForecast(44.34, 10.99, "")

        Assertions.assertTrue(result.isFailure)
        Assertions.assertEquals("API key cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { remoteDataSource.getHourlyForecast(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun getHourlyForecast_withRemoteException_returnsFailure() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { remoteDataSource.getHourlyForecast(44.34, 10.99, "valid_api_key", "metric", "en", null) } throws exception

        val result = repository.getHourlyForecast(44.34, 10.99, "valid_api_key")

        Assertions.assertTrue(result.isFailure)
        Assertions.assertEquals(exception, result.exceptionOrNull())
        coVerify { remoteDataSource.getHourlyForecast(44.34, 10.99, "valid_api_key", "metric", "en", null) }
    }

    // Tests for getCurrentWeather
    @Test
    fun getCurrentWeather_withValidInputs_returnsSuccess() = runTest {
        coEvery { remoteDataSource.getCurrentWeather(44.34, 10.99, "valid_api_key", "metric", "en") } returns Result.success(mockCurrentWeatherResponse)

        val result = repository.getCurrentWeather(44.34, 10.99, "valid_api_key")

        Assertions.assertTrue(result.isSuccess)
        Assertions.assertEquals(mockCurrentWeatherResponse, result.getOrNull())
        coVerify { remoteDataSource.getCurrentWeather(44.34, 10.99, "valid_api_key", "metric", "en") }
    }

    @Test
    fun getCurrentWeather_withInvalidCoordinates_returnsFailure() = runTest {
        val result = repository.getCurrentWeather(-100.0, 10.99, "valid_api_key")

        Assertions.assertTrue(result.isFailure)
        Assertions.assertEquals("Invalid coordinates", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { remoteDataSource.getCurrentWeather(any(), any(), any(), any(), any()) }
    }

    @Test
    fun getCurrentWeather_withEmptyAPIKey_returnsFailure() = runTest {
        val result = repository.getCurrentWeather(44.34, 10.99, "")

        Assertions.assertTrue(result.isFailure)
        Assertions.assertEquals("API key cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { remoteDataSource.getCurrentWeather(any(), any(), any(), any(), any()) }
    }

    @Test
    fun getCurrentWeather_withRemoteException_returnsFailure() = runTest {
        val exception = RuntimeException("Network error")
        coEvery { remoteDataSource.getCurrentWeather(44.34, 10.99, "valid_api_key", "metric", "en") } throws exception

        val result = repository.getCurrentWeather(44.34, 10.99, "valid_api_key")

        Assertions.assertTrue(result.isFailure)
        Assertions.assertEquals(exception, result.exceptionOrNull())
        coVerify { remoteDataSource.getCurrentWeather(44.34, 10.99, "valid_api_key", "metric", "en") }
    }
}