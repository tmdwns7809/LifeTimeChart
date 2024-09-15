package com.example.lifetimer.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TimeDao {
    @Insert
    suspend fun insertTime(time: Time)

    @Query("SELECT * FROM times")
    fun getAllTimes(): LiveData<List<Time>>
}