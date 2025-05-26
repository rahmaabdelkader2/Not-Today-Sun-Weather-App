package com.example.not_today_sun.notification.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager // Add this import
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.not_today_sun.MainActivity
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.FragmentNotificationBinding
import com.example.not_today_sun.notification.viewmodel.AlarmHelper
import com.example.not_today_sun.notification.viewmodel.NotificationViewModel
import java.util.*
import android.util.Log
import com.example.not_today_sun.model.pojo.Alarm

class NotificationFragment : Fragment() {
    companion object {
        private const val TAG = "AlarmDebug"
        private const val PREFS_NAME="WeatherSettings"
        private const val KEY_NOTIFICATION = "notification_enabled"
    }

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var alarmHelper: AlarmHelper
    private val viewModel: NotificationViewModel by viewModels {
        NotificationViewModelFactory(
            (requireActivity() as MainActivity).weatherRepository,
            alarmHelper,getSharedPreferences()
        )}

    private lateinit var selectedDate: Calendar
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var alarmDismissReceiver: BroadcastReceiver // Add receiver

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d(TAG, "Notification permission result: granted=$isGranted")
        if (!isGranted) {
            Toast.makeText(
                requireContext(),
                "Notification permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "NotificationFragment.onCreateView called")
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "NotificationFragment.onViewCreated called")

        alarmHelper = AlarmHelper(requireContext())

        // Initialize and register broadcast receiver
        alarmDismissReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "Received alarm dismissed broadcast")
                loadAlarms() // Refresh alarms list
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            alarmDismissReceiver,
            IntentFilter(DismissAlarmReceiver.ACTION_ALARM_DISMISSED)
        )

        // Check and request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "Checking notification permission: granted=$hasPermission")
            if (!hasPermission) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS permission")
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setupRecyclerView()
        setupClickListeners()
        viewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            Log.d(TAG, "Alarms updated: $alarms")
            alarmAdapter.submitList(alarms.toList())
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")
        alarmAdapter = AlarmAdapter(
            onDeleteClick = { alarm ->
                Log.d(TAG, "Delete clicked for alarm ID: ${alarm.id}")
                viewModel.deleteAlarm(alarm)
            },
            onAlarmSwitchChange = { alarm, isChecked ->
                Log.d(TAG, "Alarm switch changed for alarm ID: ${alarm.id}, enabled=$isChecked")
                viewModel.addAlarm(
                    Alarm(
                        id = alarm.id,
                        dateMillis = alarm.dateMillis,
                        fromTimeMillis = alarm.fromTimeMillis,
                        toTimeMillis = alarm.toTimeMillis,
                        alarmEnabled = isChecked,
                        notificationEnabled = alarm.notificationEnabled
                    )
                )
            },
            onNotificationSwitchChange = { alarm, isChecked ->
                viewModel.addAlarm(
                    Alarm(
                        id = alarm.id,
                        dateMillis = alarm.dateMillis,
                        fromTimeMillis = alarm.fromTimeMillis,
                        toTimeMillis = alarm.toTimeMillis,
                        alarmEnabled = alarm.alarmEnabled,
                        notificationEnabled = isChecked
                    )
                )
            }
        )
        binding.recyclerViewAlarms.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = alarmAdapter
        }
    }

    private fun setupClickListeners() {
        Log.d(TAG, "Setting up FAB click listener")
        binding.fab.setOnClickListener {
            Log.d(TAG, "FAB clicked, showing date picker")
            showDatePickerDialog()
        }
    }

    private fun loadAlarms() {
        viewModel.getAllAlarms()
    }

    private fun showDatePickerDialog() {
        Log.d(TAG, "Showing date picker dialog")
        selectedDate = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                Log.d(TAG, "Date selected: $year-${month + 1}-$day")
                selectedDate.set(year, month, day)
                showTimePickerDialog()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog() {
        Log.d(TAG, "Showing time picker dialog")
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_dual_time_picker)

        val fromTimePicker = dialog.findViewById<TimePicker>(R.id.fromTimePicker)
        val toTimePicker = dialog.findViewById<TimePicker>(R.id.toTimePicker)
        val alarmSwitch = dialog.findViewById<Switch>(R.id.alarmSwitch)
        val notificationSwitch = dialog.findViewById<Switch>(R.id.notificationSwitch)
        val confirmButton = dialog.findViewById<View>(R.id.confirmButton)

        val calendar = Calendar.getInstance()
        fromTimePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        fromTimePicker.minute = calendar.get(Calendar.MINUTE)
        toTimePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        toTimePicker.minute = calendar.get(Calendar.MINUTE)

        confirmButton.setOnClickListener {
            val fromTimeCal = Calendar.getInstance().apply {
                timeInMillis = selectedDate.timeInMillis
                set(Calendar.HOUR_OF_DAY, fromTimePicker.hour)
                set(Calendar.MINUTE, fromTimePicker.minute)
            }

            val toTimeCal = Calendar.getInstance().apply {
                timeInMillis = selectedDate.timeInMillis
                set(Calendar.HOUR_OF_DAY, toTimePicker.hour)
                set(Calendar.MINUTE, toTimePicker.minute)
            }

            Log.d(TAG, "Confirm clicked: fromTime=${fromTimeCal.timeInMillis}, toTime=${toTimeCal.timeInMillis}, alarmEnabled=${alarmSwitch.isChecked}, notificationEnabled=${notificationSwitch.isChecked}")

            // Validate time range
            if (toTimeCal.timeInMillis <= fromTimeCal.timeInMillis) {
                Log.w(TAG, "Invalid time range: toTime <= fromTime")
                Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Prevent both switches from being off or both on
            if (!alarmSwitch.isChecked && !notificationSwitch.isChecked) {
                Log.w(TAG, "Both switches are off")
                Toast.makeText(context, "At least one switch must be enabled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (alarmSwitch.isChecked && notificationSwitch.isChecked) {
                Log.w(TAG, "Both switches are on")
                Toast.makeText(context, "Alarm and Notification cannot both be enabled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addAlarm(
                Alarm(
                    dateMillis = selectedDate.timeInMillis,
                    fromTimeMillis = fromTimeCal.timeInMillis,
                    toTimeMillis = toTimeCal.timeInMillis,
                    alarmEnabled = alarmSwitch.isChecked,
                    notificationEnabled = notificationSwitch.isChecked
                )
            )
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showError(exception: Exception) {
        Log.e(TAG, "Error: ${exception.message}", exception)
        Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
    }
    private fun getSharedPreferences(): SharedPreferences {
        return requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onDestroyView() {
        Log.d(TAG, "NotificationFragment.onDestroyView called")
        // Unregister the broadcast receiver
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(alarmDismissReceiver)
        super.onDestroyView()
        _binding = null
    }
}