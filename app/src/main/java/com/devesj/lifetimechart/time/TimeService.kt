package com.devesj.lifetimechart.time

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.devesj.lifetimechart.db.Time
import com.devesj.lifetimechart.db.AppDatabase
import com.devesj.lifetimechart.repository.TimeRepository
import com.devesj.lifetimechart.util.ColorUtil
import com.devesj.lifetimechart.util.NameUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.time.Duration

class TimeService : Service() {

    private var handler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var isRunning = false
    private val timerRunnable = object : Runnable {
        override fun run() {
            setTime()

            handler.postDelayed(this, 1000 - (elapsedTime % 1000)) // 매초 업데이트
        }
    }

    private var name: String = NameUtil.CATEGORIES[0]
    private var color: Int = ColorUtil.getColorForValue(0, NameUtil.CATEGORIES.size-1)
    private var formattedTime: String = "00:00:00"
    private val channelId = "stopwatch_service_channel"
    private val notificationId = 1

    private lateinit var repository: TimeRepository

    private val binder = LocalBinder()
    // Binder 객체를 반환하는 클래스
    inner class LocalBinder : Binder() {
        fun getService(): TimeService = this@TimeService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Room 데이터베이스 인스턴스를 가져옴
        val db = AppDatabase.getDatabase(applicationContext)
        val timeDao = db.timeDao()

        // TimeDao를 전달하여 TimeRepository를 생성
        repository = TimeRepository(timeDao)

//        // 더미데이터 삽입
//        insertDummy()
    }

    // Foreground Service 시작
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                val newName = intent.getStringExtra("name") ?: ""
                var save = true
                if (newName == name) {
                    updateDatabase(elapsedTime)
                    save = false
                }
                resetTimer(save)
                name = newName
                color = intent.getIntExtra("color", 0)
                startTimer()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer() {
        if (isRunning) {
            return
        }

        isRunning = true
        startTime = System.currentTimeMillis() - elapsedTime
        setTime()
        updateElapsedTime()
    }

    private fun stopTimer(save: Boolean) {
        if (!isRunning) {
            return
        }

        isRunning = false
        handler.removeCallbacks(timerRunnable)
        setTime()
//        stopForeground(STOP_FOREGROUND_DETACH)
//        stopSelf()

        if (!save) {
            return
        }

        // db 저장
        val time = Time(name = name
            , startTime = startTime
            , elapsedTime = elapsedTime
            , endTime = startTime + elapsedTime)

        insertToDatabase(time)
    }

    private fun resetTimer(save: Boolean) {
        stopTimer(save)

        elapsedTime = 0L
        startTime = System.currentTimeMillis()

        setTime()
    }

    private fun setTime() {
        makeElapsedTime()

        startForeground(notificationId, getNotification(formattedTime))

        // 브로드캐스트로 시간 전송
        val broadcastIntent = Intent("TIME_UPDATED")
        broadcastIntent.putExtra("time", formattedTime)
        applicationContext.sendBroadcast(broadcastIntent)

    }

    // 경과 시간 업데이트
    private fun updateElapsedTime() {
        handler.postDelayed(timerRunnable, 0)
    }

    // Notification 생성
    private fun getNotification(time: String): Notification {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(name)
            .setContentText(time)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true) // 상시 알림 설정
            .build()

        return notification
    }

    // 경과 시간을 포맷하는 메소드 (hh:mm:ss)
    fun getFormatElapsedTime(): String {
        return formattedTime
    }
    fun getName(): String {
        return name
    }
    fun getColor(): Int {
        return color
    }

    private fun makeElapsedTime() {
        elapsedTime = System.currentTimeMillis() - startTime
        formattedTime = formatElapsedTime(elapsedTime)
    }

    // 경과 시간을 포맷하는 메소드 (hh:mm:ss)
    private fun formatElapsedTime(elapsedTime: Long): String {
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun insertToDatabase(time: Time) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insert(time)
        }
    }

    private fun updateDatabase(addedTime: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateLastTime(addedTime)
        }
    }

    private fun insertDummy() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.insertAll(listOf(
                Time(name = "A"
                    , startTime = convertToLongTime("2024-09-10 10:00:00")
                    , elapsedTime = convertElapsedTimeToMillis("01:00:00")
                    , endTime = convertToLongTime("2024-09-10 11:00:00")),
                Time(name = "A"
                    , startTime = convertToLongTime("2024-09-10 10:00:00")
                    , elapsedTime = convertElapsedTimeToMillis("01:00:00")
                    , endTime = convertToLongTime("2024-09-13 11:00:00")),
                Time(name = "A"
                    , startTime = convertToLongTime("2024-09-10 10:00:00")
                    , elapsedTime = convertElapsedTimeToMillis("01:00:00")
                    , endTime = convertToLongTime("2024-09-15 11:00:00")),
                Time(name = "\uD83D\uDE03"
                    , startTime = convertToLongTime("2024-09-10 10:00:00")
                    , elapsedTime = convertElapsedTimeToMillis("01:00:00")
                    , endTime = convertToLongTime("2024-09-11 11:00:00")),
                Time(name = "\uD83D\uDE03"
                    , startTime = convertToLongTime("2024-09-10 10:00:00")
                    , elapsedTime = convertElapsedTimeToMillis("01:00:00")
                    , endTime = convertToLongTime("2024-09-12 11:00:00")),
                Time(name = "\uD83D\uDE03"
                    , startTime = convertToLongTime("2024-09-10 10:00:00")
                    , elapsedTime = convertElapsedTimeToMillis("01:00:00")
                    , endTime = convertToLongTime("2024-09-14 11:00:00")),
            ))
        }
    }

    fun convertElapsedTimeToMillis(timeString: String): Long {
        val parts = timeString.split(":").map { it.toLong() }
        val hours = parts[0]
        val minutes = parts[1]
        val seconds = parts[2]

        return (hours * 3600 + minutes * 60 + seconds) * 1000
    }

    fun convertToLongTime(dateString: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date: Date = format.parse(dateString)!!
        return date.time
    }

    // Notification 채널 생성 (Android 8.0 이상 필요)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Stopwatch Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}