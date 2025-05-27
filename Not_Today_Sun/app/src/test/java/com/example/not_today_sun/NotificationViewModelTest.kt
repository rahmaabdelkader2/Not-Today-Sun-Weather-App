//package com.example.not_today_sun
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import com.example.not_today_sun.model.pojo.Alarm
//import com.example.not_today_sun.model.repo.WeatherRepository
//import io.mockk.*
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.*
//import org.junit.*
//import org.junit.runner.RunWith
//import org.hamcrest.MatcherAssert.assertThat
//import org.hamcrest.Matchers.*
//import android.content.SharedPreferences
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.example.not_today_sun.notification.viewmodel.NotificationViewModel
//import com.example.not_today_sun.notification.viewmodel.AlarmHelper
//import org.junit.runners.JUnit4
//
//
//@RunWith(JUnit4::class)
//class NotificationViewModelTest {
//
//    @get:Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var viewModel: NotificationViewModel
//    private lateinit var repository: WeatherRepository
//    private lateinit var alarmHelper: AlarmHelper
//    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
//
//    private val testDispatcher = StandardTestDispatcher()
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
//
//        repository = mockk()
//        alarmHelper = mockk(relaxed = true)
//        sharedPreferences = mockk()
//        sharedPreferencesEditor = mockk()
//
//        every { sharedPreferences.edit() } returns sharedPreferencesEditor
//        every { sharedPreferencesEditor.putBoolean(any(), any()) } returns sharedPreferencesEditor
//        every { sharedPreferencesEditor.apply() } just Runs
//
//        viewModel = NotificationViewModel(repository, alarmHelper, sharedPreferences)
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//        unmockkAll()
//    }
//
//    @Test
//    fun addAlarmShouldCallRepository_setAlarmAndNotification_thenRefreshList() = runTest {
//        // Arrange
//        val testAlarm = Alarm(
//            id = 0,  // Auto-generated, set to 0
//            dateMillis = 1661871600L,
//            fromTimeMillis = 1661871600L,
//            toTimeMillis = 1661875200L, // 1 hour later
//            alarmEnabled = true,
//            notificationEnabled = true
//        )
//
//        val savedAlarm = testAlarm.copy(id = 1) // Simulate repository assigning ID
//
//        coEvery { repository.saveAlarm(any()) } returns 1
//        coEvery { repository.getAllAlarms() } returns listOf(savedAlarm)
//
//        // Act
//        viewModel.addAlarm(testAlarm)
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Assert
//        coVerify { repository.saveAlarm(testAlarm) }
//        verify {
//            alarmHelper.setAlarm(
//                match { alarm ->
//                    alarm.id == 1L &&
//                            alarm.dateMillis == 1661871600L &&
//                            alarm.fromTimeMillis == 1661871600L &&
//                            alarm.toTimeMillis == 1661875200L &&
//                            alarm.alarmEnabled &&
//                            alarm.notificationEnabled
//                }
//            )
//        }
//        verify {
//            alarmHelper.setNotification(
//                match { alarm ->
//                    alarm.id == 1L &&
//                            alarm.notificationEnabled
//                }
//            )
//        }
//        coVerify { repository.getAllAlarms() }
//    }
//
//    @Test
//    fun `deleteAlarm should call repository with correct ID and refresh list`() = runTest {
//        // Arrange
//        val testAlarm = Alarm(
//            id = 1L,  // Existing alarm
//            dateMillis = 1661871600L,
//            fromTimeMillis = 1661871600L,
//            toTimeMillis = 1661875200L,
//            alarmEnabled = true,
//            notificationEnabled = true
//        )
//
//        coEvery { repository.deleteAlarm(1L) } just Runs
//        coEvery { repository.getAllAlarms() } returns emptyList()
//
//        // Act
//        viewModel.deleteAlarm(testAlarm)
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Assert
//        coVerify { repository.deleteAlarm(1L) }
//        coVerify { repository.getAllAlarms() }
//    }
//
//    @Test
//    fun `getAllAlarms should return alarms with correct parameters`() = runTest {
//        // Arrange
//        val testAlarms = listOf(
//            Alarm(
//                id = 1L,
//                dateMillis = 1661871600L,
//                fromTimeMillis = 1661871600L,
//                toTimeMillis = 1661875200L,
//                alarmEnabled = true,
//                notificationEnabled = true
//            ),
//            Alarm(
//                id = 2L,
//                dateMillis = 1661958000L,
//                fromTimeMillis = 1661958000L,
//                toTimeMillis = 1661961600L,
//                alarmEnabled = true,
//                notificationEnabled = false
//            )
//        )
//
//        coEvery { repository.getAllAlarms() } returns testAlarms
//
//        // Act
//        viewModel.getAllAlarms()
//        testDispatcher.scheduler.advanceUntilIdle()
//
//        // Assert
//        assertThat(viewModel.alarms.value, hasSize(2))
//        assertThat(viewModel.alarms.value?.get(0)?.id, `is`(1L))
//        assertThat(viewModel.alarms.value?.get(0)?.toTimeMillis, `is`(1661875200L))
//        assertThat(viewModel.alarms.value?.get(1)?.notificationEnabled, `is`(false))
//    }
//
//    @Test
//    fun `updateNotificationPreference should update shared preferences`() {
//        // Arrange
//        val enabled = true
//
//        // Act
//        viewModel.updateNotificationPreference(enabled)
//
//        // Assert
//        verify { sharedPreferencesEditor.putBoolean("KEY_NOTIFICATIONS", enabled) }
//        verify { sharedPreferencesEditor.apply() }
//    }
//
//    @Test
//    fun `init should call getAllAlarms`() = runTest {
//        // Arrange
//        coEvery { repository.getAllAlarms() } returns emptyList()
//
//        // Act - happens automatically in setup
//
//        // Assert
//        coVerify { repository.getAllAlarms() }
//    }
//}