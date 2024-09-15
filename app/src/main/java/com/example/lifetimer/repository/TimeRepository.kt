package com.example.lifetimer.repository

import androidx.lifecycle.LiveData
import com.example.lifetimer.db.Time
import com.example.lifetimer.db.TimeDao

class TimeRepository(private val timeDao: TimeDao) {

    val allUsers: LiveData<List<Time>> = timeDao.getAllTimes()

    suspend fun insert(time: Time) {
        timeDao.insertTime(time)
    }
}