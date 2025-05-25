package com.example.not_today_sun.model.pojo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val fromTimeMillis: Long,
    val toTimeMillis: Long,
    val alarmEnabled: Boolean,
    val notificationEnabled: Boolean
)