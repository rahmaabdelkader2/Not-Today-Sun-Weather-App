package com.example.not_today_sun.notification.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.not_today_sun.MainActivity
import com.example.not_today_sun.R
import com.example.not_today_sun.databinding.FragmentNotificationBinding
import com.example.not_today_sun.notification.viewmodel.NotificationViewModel
import java.util.*

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels {
        NotificationViewModelFactory((requireActivity() as MainActivity).weatherRepository)
    }
    private lateinit var selectedDate: Calendar
    private lateinit var alarmAdapter: AlarmAdapter

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
        setupRecyclerView()
        setupClickListeners()
        loadAlarms()
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onDeleteClick = { alarm ->
                viewModel.deleteAlarm(
                    alarm,
                    onSuccess = { loadAlarms() },
                    onError = { showError(it) }
                )
            },
            onSwitchChange = { alarm, isChecked ->
                viewModel.saveAlarm(
                    alarm.dateMillis,
                    alarm.fromTimeMillis,
                    alarm.toTimeMillis,
                    isChecked,
                    alarm.notificationEnabled,
                    onSuccess = { loadAlarms() },
                    onError = { showError(it) }
                )
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
        viewModel.getAllAlarms(
            onSuccess = { alarms ->
                alarmAdapter.submitList(alarms)
            },
            onError = { showError(it) }
        )
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
            val fromTime = Calendar.getInstance().apply {
                timeInMillis = selectedDate.timeInMillis
                set(Calendar.HOUR_OF_DAY, fromTimePicker.hour)
                set(Calendar.MINUTE, fromTimePicker.minute)
            }.timeInMillis

            val toTime = Calendar.getInstance().apply {
                timeInMillis = selectedDate.timeInMillis
                set(Calendar.HOUR_OF_DAY, toTimePicker.hour)
                set(Calendar.MINUTE, toTimePicker.minute)
            }.timeInMillis

            val dateMillis = Calendar.getInstance().apply {
                timeInMillis = selectedDate.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            viewModel.saveAlarm(
                dateMillis,
                fromTime,
                toTime,
                alarmSwitch.isChecked,
                notificationSwitch.isChecked,
                onSuccess = { loadAlarms() },
                onError = { showError(it) }
            )
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showError(exception: Exception) {
        Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}