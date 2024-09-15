package com.devesj.lifetimechart.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.devesj.lifetimechart.db.Time
import com.devesj.lifetimechart.db.TimeDao

class TimeRepository(private val timeDao: TimeDao) {

    val allUsers: LiveData<List<Time>> = timeDao.getAllTimes()

    suspend fun insert(time: Time) {
        timeDao.insertTime(time)
    }

    suspend fun insertAll(list: List<Time>) {
        timeDao.insertAll(list)
    }
}