package com.devesj.lifetimechart.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "times")
data class Time(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val startTime: Long,
    val elapsedTime: Long,
    val endTime: Long,
    val memo: String,
)