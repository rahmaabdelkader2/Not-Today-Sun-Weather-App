package com.example.not_today_sun.notification.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.not_today_sun.databinding.ItemAlarmBinding
import com.example.not_today_sun.model.pojo.Alarm
import java.text.SimpleDateFormat
import java.util.*

class AlarmAdapter(
    private val onDeleteClick: (Alarm) -> Unit,
    private val onSwitchChange: (Alarm, Boolean) -> Unit
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
        return AlarmViewHolder(binding, onDeleteClick, onSwitchChange)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AlarmViewHolder(
        private val binding: ItemAlarmBinding,
        private val onDeleteClick: (Alarm) -> Unit,
        private val onSwitchChange: (Alarm, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: Alarm) {
            binding.apply {
                textViewDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(alarm.dateMillis))
                textViewFromTime.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(alarm.fromTimeMillis))
                textViewToTime.text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(alarm.toTimeMillis))
                switchAlarm.isChecked = alarm.alarmEnabled
                switchNotification.isChecked = alarm.notificationEnabled

                buttonDelete.setOnClickListener { onDeleteClick(alarm) }
                switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                    onSwitchChange(alarm, isChecked)
                }
            }
        }
    }
}