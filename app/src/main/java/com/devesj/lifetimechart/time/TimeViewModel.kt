package com.devesj.lifetimechart.time

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.devesj.lifetimechart.db.AppDatabase
import com.devesj.lifetimechart.db.Time
import com.devesj.lifetimechart.repository.TimeRepository
import kotlinx.coroutines.launch

class TimeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimeRepository
    val allTimes: LiveData<List<Time>>

    init {
        val timeDao = AppDatabase.getDatabase(application).timeDao()
        repository = TimeRepository(timeDao)
        allTimes = repository.allUsers
    }

    fun insert(time: Time) = viewModelScope.launch {
        repository.insert(time)
    }
}