package com.example.not_today_sun.notification.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.not_today_sun.databinding.ItemAlarmBinding
import com.example.not_today_sun.model.pojo.Alarm
import java.text.SimpleDateFormat
import java.util.*

class AlarmAdapter(
    private val onDeleteClick: (Alarm) -> Unit,
    private val onAlarmSwitchChange: (Alarm, Boolean) -> Unit,
    private val onNotificationSwitchChange: (Alarm, Boolean) -> Unit
) : ListAdapter<Alarm, AlarmAdapter.AlarmViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Alarm>() {
        override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AlarmViewHolder(
        private val binding: ItemAlarmBinding,
        private val onDeleteClick: (Alarm) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: Alarm) {
            binding.apply {
                textViewDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(alarm.dateMillis))
                textViewFromTime.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(alarm.fromTimeMillis))
                textViewToTime.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(alarm.toTimeMillis))
                alertType.text = when (alarm.alarmEnabled) {
                    true -> "Alarm Enabled"
                    false -> "Notifications Enabled"
                }
                buttonDelete.setOnClickListener {
                    AlertDialog.Builder(binding.root.context)
                        .setTitle("Delete Location")
                        .setMessage("Are you sure you want to delete item ?")
                        .setPositiveButton("Yes") { _, _ ->
                            onDeleteClick(alarm)
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setCancelable(true)
                        .show()                }



            }
            }
        }
    }
