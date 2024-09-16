package com.devesj.lifetimechart.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TimeDao {
    @Insert
    suspend fun insertTime(time: Time)

    // 다중 데이터 삽입
    @Insert
    suspend fun insertAll(times: List<Time>)

    @Query("SELECT * FROM times")
    fun getAllTimes(): LiveData<List<Time>>

    @Query("SELECT * FROM times ORDER BY id DESC")
    fun getAllTimesReverse(): LiveData<List<Time>>

    @Query("SELECT * FROM times ORDER BY id DESC LIMIT 1")
    fun getLastRow(): Time?

    @Query("UPDATE times SET elapsedTime = elapsedTime + :addedTime, endTime = endTime + :addedTime WHERE id = :id")
    fun updateLastTime(addedTime: Long, id: Int)
}