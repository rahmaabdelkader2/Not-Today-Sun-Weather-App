package com.example.not_today_sun

import com.example.not_today_sun.fakedata.MockLocalDataSource
import com.example.not_today_sun.fakedata.MockRemoteDataSource
import com.example.not_today_sun.model.local.LocalDataSource
import com.example.not_today_sun.model.remote.RemoteDataSource
import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.model.pojo.City
import com.example.not_today_sun.model.pojo.Clouds
import com.example.not_today_sun.model.pojo.Coord
import com.example.not_today_sun.model.pojo.Coordinates
import com.example.not_today_sun.model.pojo.CurrentClouds
import com.example.not_today_sun.model.pojo.CurrentMain
import com.example.not_today_sun.model.pojo.CurrentSys
import com.example.not_today_sun.model.pojo.CurrentWind
import com.example.not_today_sun.model.pojo.FavoriteLocation
import com.example.not_today_sun.model.pojo.Main
import com.example.not_today_sun.model.pojo.Sys
import com.example.not_today_sun.model.pojo.Weather
import com.example.not_today_sun.model.pojo.WeatherData
import com.example.not_today_sun.model.pojo.Wind
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.remote.HourlyForecastResponse
import com.example.not_today_sun.model.repo.WeatherRepository
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test

class WeatherRepositoryTest {

    private lateinit var localDataSource: LocalDataSource
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var repository: WeatherRepository

    private val mockHourlyForecastResponse = HourlyForecastResponse(
        id = 0,
        cod = "200",
        message = 0,
        cnt = 24,
        list = listOf(
            WeatherData(
                dt = 1661871600,
                main = Main(
                    temp = 296.76f,
                    feelsLike = 296.98f,
                    tempMin = 296.76f,
                    tempMax = 297.87f,
                    pressure = 1015,
                    seaLevel = 1015,
                    grndLevel = 933,
                    humidity = 69,
                    tempKf = -1.11f
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
                    speed = 0.62f,
                    deg = 349,
                    gust = 1.18f
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
            coord = Coord(lat = 44.34f, lon = 10.99f),
            country = "IT",
            population = 4593,
            timezone = 7200,
            sunrise = 1661834187,
            sunset = 1661882248
        )
    )

    private val mockCurrentWeatherResponse = CurrentWeatherResponse(
        dbId = 0,
        coordinates = Coordinates(latitude = 44.34, longitude = 10.99),
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
            temperature = 296.76,
            feelsLike = 296.98,
            tempMin = 296.76,
            tempMax = 297.87,
            pressure = 1015,
            humidity = 69,
            seaLevel = 1015,
            groundLevel = 933
        ),
        visibility = 10000,
        wind = CurrentWind(
            speed = 0.62,
            direction = 349,
            gustSpeed = 1.18
        ),
        clouds = CurrentClouds(cloudiness = 100),
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

    private val testFavoriteLocation = FavoriteLocation(
        cityName = "Zocca",
        latitude = 44.34,
        longitude = 10.99,
        maxTemp = 297.87,
        minTemp = 296.76
    )

    private val testAlarm = Alarm(
        id = 0,
        dateMillis = 1661871600,
        fromTimeMillis = 1661871600,
        toTimeMillis = 1661875200,
        alarmEnabled = true,
        notificationEnabled = true
    )

    @Before
    fun setup() {
        val mockRemote = MockRemoteDataSource(
            hourlyForecasts = mapOf(Pair(44.34, 10.99) to mockHourlyForecastResponse),
            currentWeathers = mapOf(Pair(44.34, 10.99) to mockCurrentWeatherResponse)
        )
        remoteDataSource = mockRemote.instance

        val mockLocal = MockLocalDataSource()
        localDataSource = mockLocal.instance

        repository = WeatherRepository(remoteDataSource, localDataSource)
    }

    @Test
    fun getCurrentWeather_validCoordinates_returnsSuccess() = runTest {
        val result = repository.getCurrentWeather(
            latitude = 44.34,
            longitude = 10.99,
            apiKey = "valid_api_key"
        )
        assertThat(result.isSuccess, `is`(true))
        result.onSuccess { response ->
            assertThat(response.cityName, `is`("Zocca"))
            assertThat(response.main.temperature, `is`(296.76))
        }
    }

    @Test
    fun getHourlyForecast_validCoordinates_returnsSuccess() = runTest {
        val result = repository.getHourlyForecast(
            latitude = 44.34,
            longitude = 10.99,
            apiKey = "valid_api_key"
        )
        assertThat(result.isSuccess, `is`(true))
        result.onSuccess { response ->
            assertThat(response.city.name, `is`("Zocca"))
            assertThat(response.list[0].main.temp, `is`(296.76f))
        }
    }

    @Test
    fun saveHourlyForecastToLocal_savesCorrectly() = runTest {
        repository.saveHourlyForecastToLocal(mockHourlyForecastResponse)
        val retrieved = localDataSource.getHourlyForecast()
        assertThat(retrieved?.city?.name, `is`("Zocca"))
    }

    @Test
    fun saveCurrentWeatherToLocal_savesCorrectly() = runTest {
        repository.saveCurrentWeatherToLocal(mockCurrentWeatherResponse)
        val retrieved = localDataSource.getCurrentWeather()
        assertThat(retrieved?.cityName, `is`("Zocca"))
    }

    @Test
    fun favoriteLocationOperations_workCorrectly() = runTest {
        // Test insert
        repository.saveFavoriteLocation(testFavoriteLocation)
        val locations = repository.getAllFavoriteLocations()
        assertThat(locations.size, `is`(1))
        assertThat(locations[0].cityName, `is`("Zocca"))

        // Test delete - pass the city name instead of the object
        repository.deleteFavoriteLocation(testFavoriteLocation)
        val updatedLocations = repository.getAllFavoriteLocations()
        assertThat(updatedLocations.size, `is`(0))
    }

    @Test
    fun alarmOperations_workCorrectly() = runTest {
        // Test save
        val alarmId = repository.saveAlarm(testAlarm)
        val alarms = repository.getAllAlarms()
        assertThat(alarms.size, `is`(1))
        assertThat(alarms[0].dateMillis, `is`(1661871600L))

        // Test get by id
        val retrievedAlarm = repository.getAlarmById(alarmId)
        assertThat(retrievedAlarm?.dateMillis, `is`(1661871600L))

        // Test delete
        repository.deleteAlarm(alarmId)
        val updatedAlarms = repository.getAllAlarms()
        assertThat(updatedAlarms.size, `is`(0))
    }
}