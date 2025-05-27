package com.example.not_today_sun.notification.view

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.FragmentNotificationBinding
import com.example.not_today_sun.model.pojo.Alarm
import com.example.not_today_sun.notification.viewmodel.AlarmHelper
import com.example.not_today_sun.notification.viewmodel.NotificationViewModel
import com.example.not_today_sun.MainActivity

import java.util.*

class NotificationFragment : Fragment() {

    override fun onStart() {
        super.onStart()
        binding.fab.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()
        binding.fab.visibility = View.GONE
    }

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var alarmHelper: AlarmHelper
    private val viewModel: NotificationViewModel by viewModels {
        NotificationViewModelFactory(
            (requireActivity() as MainActivity).weatherRepository,
            alarmHelper
        )
    }
    private lateinit var selectedDate: Calendar
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var alarmDismissReceiver: BroadcastReceiver

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
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
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alarmHelper = AlarmHelper(requireContext())

        // Initialize and register broadcast receiver
        alarmDismissReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                loadAlarms()
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            alarmDismissReceiver,
            IntentFilter(AlarmReceiver.ACTION_ALARM_DISMISSED)
        )

        // Check and request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setupRecyclerView()
        setupClickListeners()
        viewModel.alarms.observe(viewLifecycleOwner) { alarms ->
            alarmAdapter.submitList(alarms.toList())
        }
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onDeleteClick = { alert ->
                viewModel.deleteAlarm(alert)
            }
        )
        binding.recyclerViewAlarms.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = alarmAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fab.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun loadAlarms() {
        viewModel.getAllAlarms()
    }

    private fun showDatePickerDialog() {
        selectedDate = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(year, month, day)
                showTimePickerDialog()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePickerDialog() {
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

            // Validate time range
            if (toTimeCal.timeInMillis <= fromTimeCal.timeInMillis) {
                Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Prevent both switches from being off or both on
            if (!alarmSwitch.isChecked && !notificationSwitch.isChecked) {
                Toast.makeText(context, "At least one switch must be enabled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (alarmSwitch.isChecked && notificationSwitch.isChecked) {
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

    override fun onDestroyView() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(alarmDismissReceiver)
        super.onDestroyView()
        _binding = null
    }
}