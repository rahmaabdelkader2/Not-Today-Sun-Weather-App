//package com.example.not_today_sun
//
//import android.content.Context
//import android.content.SharedPreferences
//import android.content.res.Configuration
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.example.not_today_sun.settings.viewmodel.SettingsViewModel
//import io.mockk.every
//import io.mockk.mockk
//import io.mockk.verify
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.hamcrest.MatcherAssert.assertThat
//import org.hamcrest.Matchers.`is`
//import java.util.Locale
//
//@RunWith(AndroidJUnit4::class)
//class SettingsViewModelTest {
//
//    private lateinit var viewModel: SettingsViewModel
//    private lateinit var mockContext: Context
//    private lateinit var mockSharedPreferences: SharedPreferences
//    private lateinit var mockEditor: SharedPreferences.Editor
//
//    @Before
//    fun setup() {
//        // Create mocks
//        mockContext = mockk<Context>(relaxed = true)
//        mockSharedPreferences = mockk<SharedPreferences>(relaxed = true)
//        mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)
//
//        // Stub the SharedPreferences behavior
//        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
//        every { mockSharedPreferences.edit() } returns mockEditor
//        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
//        every { mockEditor.putString(any(), any()) } returns mockEditor
//        every { mockEditor.putFloat(any(), any()) } returns mockEditor
//        every { mockEditor.remove(any()) } returns mockEditor
//        every { mockEditor.apply() } returns Unit
//
//        viewModel = SettingsViewModel()
//    }
//
//    @Test
//    fun getNotificationPreference_returnsFalseByDefault() {
//        // Given - default value is false
//        every { mockSharedPreferences.getBoolean(SettingsViewModel.KEY_NOTIFICATIONS, false) } returns false
//
//        // When
//        val result = viewModel.getNotificationPreference(mockContext)
//
//        // Then
//        assertThat(result, `is`(false))
//        assertThat(viewModel._notificationEnabled.getOrAwaitValue(), `is`(false))
//    }
//
//    @Test
//    fun saveNotificationPreference_updatesLiveData() {
//        // When
//        viewModel.saveNotificationPreference(mockContext, true)
//
//        // Then
//        verify { mockEditor.putBoolean(SettingsViewModel.KEY_NOTIFICATIONS, true) }
//        verify { mockEditor.apply() }
//        assertThat(viewModel._notificationEnabled.getOrAwaitValue(), `is`(true))
//    }
//
//    @Test
//    fun saveLocationPreference_withMapDisabled_clearsMapLocation() {
//        // When
//        viewModel.saveLocationPreference(mockContext, useGps = true, useMap = false)
//
//        // Then
//        verify { mockEditor.putBoolean(SettingsViewModel.KEY_LOCATION, true) }
//        verify { mockEditor.putBoolean(SettingsViewModel.KEY_MAP, false) }
//        verify { mockEditor.remove(SettingsViewModel.KEY_MAP_LAT) }
//        verify { mockEditor.remove(SettingsViewModel.KEY_MAP_LON) }
//        verify { mockEditor.remove(SettingsViewModel.KEY_MAP_LOCATION_NAME) }
//        verify { mockEditor.apply() }
//    }
//
//    @Test
//    fun saveMapLocation_savesAllParameters() {
//        // When
//        viewModel.saveMapLocation(mockContext, 12.34f, 56.78f, "Test Location")
//
//        // Then
//        verify { mockEditor.putFloat(SettingsViewModel.KEY_MAP_LAT, 12.34f) }
//        verify { mockEditor.putFloat(SettingsViewModel.KEY_MAP_LON, 56.78f) }
//        verify { mockEditor.putString(SettingsViewModel.KEY_MAP_LOCATION_NAME, "Test Location") }
//        verify { mockEditor.putBoolean(SettingsViewModel.KEY_LOCATION, false) }
//        verify { mockEditor.putBoolean(SettingsViewModel.KEY_MAP, true) }
//        verify { mockEditor.apply() }
//    }
//
//    @Test
//    fun getTemperatureUnit_returnsStandardByDefault() {
//        // Given
//        every { mockSharedPreferences.getString(SettingsViewModel.KEY_TEMPERATURE_UNIT, "standard") } returns "standard"
//
//        // When
//        val result = viewModel.getTemperatureUnit(mockContext)
//
//        // Then
//        assertThat(result, `is`("standard"))
//    }
//
//    @Test
//    fun saveTemperatureUnit_savesCorrectValue() {
//        // When
//        viewModel.saveTemperatureUnit(mockContext, "metric")
//
//        // Then
//        verify { mockEditor.putString(SettingsViewModel.KEY_TEMPERATURE_UNIT, "metric") }
//        verify { mockEditor.apply() }
//    }
//
//    @Test
//    fun getWindSpeedUnit_returnsMsByDefault() {
//        // Given
//        every { mockSharedPreferences.getString(SettingsViewModel.KEY_WIND_SPEED_UNIT, "m/s") } returns "m/s"
//
//        // When
//        val result = viewModel.getWindSpeedUnit(mockContext)
//
//        // Then
//        assertThat(result, `is`("m/s"))
//    }
//
//    @Test
//    fun saveLanguage_updatesLocale() {
//        // Given
//        val realContext = ApplicationProvider.getApplicationContext<Context>()
//        val mockResources = mockk<Configuration>(relaxed = true)
//        every { realContext.resources.configuration } returns mockResources
//
//        // When
//        viewModel.saveLanguage(realContext, "fr")
//
//        // Then
//        assertThat(Locale.getDefault().language, `is`("fr"))
//    }
//}