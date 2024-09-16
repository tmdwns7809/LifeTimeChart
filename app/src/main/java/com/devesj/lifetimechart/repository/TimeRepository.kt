package com.devesj.lifetimechart.repository

import androidx.lifecycle.LiveData
import com.devesj.lifetimechart.db.Time
import com.devesj.lifetimechart.db.TimeDao

class TimeRepository(private val timeDao: TimeDao) {

    val allTimes: LiveData<List<Time>> = timeDao.getAllTimes()
    val allTimesReverse: LiveData<List<Time>> = timeDao.getAllTimesReverse()

    suspend fun insert(time: Time) {
        timeDao.insertTime(time)
    }

    suspend fun insertAll(list: List<Time>) {
        timeDao.insertAll(list)
    }

    suspend fun updateLastTime(addedTime: Long) {
        val lastRow = timeDao.getLastRow()

        lastRow?.let {
            timeDao.updateLastTime(addedTime, it.id)
        }
    }
}