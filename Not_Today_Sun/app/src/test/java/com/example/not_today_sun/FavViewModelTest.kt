package com.example.not_today_sun

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.not_today_sun.fakedata.MockLocalDataSource
import com.example.not_today_sun.fakedata.MockRemoteDataSource
import com.example.not_today_sun.fav.viewmodel.FavViewModel
import com.example.not_today_sun.model.pojo.FavoriteLocation
import com.example.not_today_sun.model.remote.CurrentWeatherResponse
import com.example.not_today_sun.model.pojo.Coordinates
import com.example.not_today_sun.model.pojo.CurrentClouds
import com.example.not_today_sun.model.pojo.CurrentMain
import com.example.not_today_sun.model.pojo.CurrentSys
import com.example.not_today_sun.model.pojo.CurrentWind
import com.example.not_today_sun.model.pojo.Weather
import com.example.not_today_sun.model.repo.WeatherRepository
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FavViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // For LiveData testing

    private lateinit var viewModel: FavViewModel
    private lateinit var repository: WeatherRepository
    private lateinit var mockLocalDataSource: MockLocalDataSource
    private lateinit var mockRemoteDataSource: MockRemoteDataSource
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private val testFavoriteLocation = FavoriteLocation(
        cityName = "Zocca",
        latitude = 44.34,
        longitude = 10.99,
        maxTemp = 297.87,
        minTemp = 296.76
    )

    private val mockCurrentWeatherResponse = CurrentWeatherResponse(
        dbId = 0,
        coordinates = Coordinates(latitude = 44.34, longitude = 10.99),
        weather = listOf(Weather(id = 500, main = "Rain", description = "light rain", icon = "10d")),
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

    @Before
    fun setup() {
        // Set the Main dispatcher to a TestDispatcher before each test
        Dispatchers.setMain(testDispatcher)

        mockLocalDataSource = MockLocalDataSource()
        mockRemoteDataSource = MockRemoteDataSource(
            currentWeathers = mapOf(Pair(44.34, 10.99) to mockCurrentWeatherResponse)
        )
        repository = WeatherRepository(mockRemoteDataSource.instance, mockLocalDataSource.instance)
        viewModel = FavViewModel(repository)
    }

    @After
    fun tearDown() {
        // Reset the Main dispatcher after each test to avoid interference
        Dispatchers.resetMain()
    }

    @Test
    fun getAllFavoriteLocations_emitsSuccess_when_repository_returns_data() = runTest {
        // Arrange
        mockLocalDataSource.addFavoriteLocationFromCityName("Zocca")
        coEvery { repository.getAllFavoriteLocations() } returns listOf(testFavoriteLocation)

        // Act
        viewModel.getAllFavoriteLocations()

        // Assert
        val result = viewModel.favoriteLocations.getOrAwaitValue()
        assertThat(result.size, `is`(1))
        assertThat(result[0].cityName, `is`("Zocca"))
        assertThat(viewModel.isLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.errorMessage.getOrAwaitValue(), nullValue()) // Use nullValue() instead of is(null)
    }

    @Test
    fun getAllFavoriteLocations_emitsError_when_repository_throws_exception() = runTest {
        // Arrange
        val testException = RuntimeException("Database error")
        coEvery { repository.getAllFavoriteLocations() } throws testException

        // Act
        viewModel.getAllFavoriteLocations()

        // Assert
        val errorMessage = viewModel.errorMessage.getOrAwaitValue()
        assertThat(errorMessage, `is`("Error loading locations: Database error"))
        assertThat(viewModel.isLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun addLocationToFavorites_emitsSuccess_when_repository_returns_data() = runTest {
        // Arrange
        coEvery { repository.getCurrentWeather(44.34, 10.99, any()) } returns Result.success(mockCurrentWeatherResponse)
        coEvery { repository.saveFavoriteLocation(any()) } returns Unit
        coEvery { repository.getAllFavoriteLocations() } returns listOf(testFavoriteLocation)

        // Act
        viewModel.addLocationToFavorites(44.34, 10.99)

        // Verify repository calls
        coVerify(exactly = 1) { repository.getCurrentWeather(44.34, 10.99, any()) }
        coVerify(exactly = 1) { repository.saveFavoriteLocation(any()) }
        coVerify(exactly = 1) { repository.getAllFavoriteLocations() }

        // Advance the test dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val result = viewModel.favoriteLocations.getOrAwaitValue()
        assertThat(result.size, `is`(1))
        assertThat(result[0].cityName, `is`("Zocca"))
        assertThat(viewModel.errorMessage.getOrAwaitValue(), nullValue())
    }

    @Test
    fun addLocationToFavorites_emitsError_when_repository_throws_exception() = runTest {
        // Arrange
        val testException = RuntimeException("Network error")
        coEvery { repository.getCurrentWeather(44.34, 10.99, any()) } throws testException

        // Act
        viewModel.addLocationToFavorites(44.34, 10.99)

        // Assert
        val errorMessage = viewModel.errorMessage.getOrAwaitValue()
        assertThat(errorMessage, `is`("Failed to fetch weather data"))
        assertThat(viewModel.isLoading.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun addNewLocation_triggersNavigation() = runTest {
        // Act
        viewModel.addNewLocation()

        // Assert
        assertThat(viewModel.navigateToMap.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun onNavigationComplete_resetsNavigation() = runTest {
        // Arrange
        viewModel.addNewLocation() // Set navigateToMap to true

        // Act
        viewModel.onNavigationComplete()

        // Assert
        assertThat(viewModel.navigateToMap.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun deleteLocation_emitsSuccess_when_repository_deletes_data() = runTest {
        // Arrange
        mockLocalDataSource.addFavoriteLocationFromCityName("Zocca")
        coEvery { repository.getAllFavoriteLocations() } returns listOf(testFavoriteLocation) andThen emptyList()
        coEvery { repository.deleteFavoriteLocation(testFavoriteLocation) } returns Unit

        // Act
        viewModel.deleteLocation(testFavoriteLocation)
        advanceUntilIdle() // Ensure all coroutines complete

        // Assert
        val result = viewModel.favoriteLocations.getOrAwaitValue()
        assertThat(result.size, `is`(0))

        assertThat(viewModel.errorMessage.getOrAwaitValue(), `is`(null))
        assertThat(viewModel.isLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun deleteLocation_emitsError_when_repository_throws_exception() = runTest {
        // Arrange
        val testException = RuntimeException("Delete error")
        // Explicitly mock the deleteFavoriteLocation call to throw an exception
        coEvery { repository.deleteFavoriteLocation(testFavoriteLocation) } throws testException

        // Act
        viewModel.deleteLocation(testFavoriteLocation)

        // Assert
        val errorMessage = viewModel.errorMessage.getOrAwaitValue()
        assertThat(errorMessage, `is`("Failed to delete location: Delete error"))
        assertThat(viewModel.isLoading.getOrAwaitValue(), `is`(true))
    }
}