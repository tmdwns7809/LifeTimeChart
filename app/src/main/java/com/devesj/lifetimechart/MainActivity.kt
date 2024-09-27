package com.devesj.lifetimechart

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.devesj.lifetimechart.chart.ChartFragment
import com.devesj.lifetimechart.databinding.ActivityMainBinding
import com.devesj.lifetimechart.history.HistoryFragment
import com.devesj.lifetimechart.time.TimeFragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE_NOTIFICATION_PERMISSION = 1001  // 권한 요청 코드
    private val REQUEST_CODE_HEALTH_PERMISSION = 1002  // 권한 요청 코드
    private val REQUEST_CODE_ACTIVITY_PERMISSION = 1003  // 권한 요청 코드
    private var currentFragment: Fragment? = null
    private val CURRENT_FRAGMENT_KEY: String = "current_fragment_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 화면 꺼짐 방지 플래그 설정
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 안드로이드 13(API 33) 이상에서 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                // 권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATION_PERMISSION
                )
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.FOREGROUND_SERVICE_HEALTH)
                != PackageManager.PERMISSION_GRANTED) {

                // 권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.FOREGROUND_SERVICE_HEALTH),
                    REQUEST_CODE_HEALTH_PERMISSION
                )
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {

                // 권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_CODE_ACTIVITY_PERMISSION
                )
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState != null) {
            // 저장된 프래그먼트 복원
            currentFragment =
                supportFragmentManager.getFragment(savedInstanceState, CURRENT_FRAGMENT_KEY)
        } else {
            // 초기 프래그먼트 설정
            currentFragment = TimeFragment()
            // 초기 화면 설정 (HomeFragment)
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, currentFragment as TimeFragment).commit()
        }

        // 메뉴 아이템 클릭 리스너 설정
        binding.bottomNavigation.setOnItemSelectedListener  { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_time -> selectedFragment = TimeFragment()
                R.id.nav_chart -> selectedFragment = ChartFragment()
                R.id.nav_history -> selectedFragment = HistoryFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment).commit()
            }
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 현재 프래그먼트 저장
        if (currentFragment != null && currentFragment!!.isAdded) {
            supportFragmentManager.putFragment(outState, CURRENT_FRAGMENT_KEY, currentFragment!!)
        }
    }

    override fun onResume() {
        super.onResume()

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.bottomNavigation.visibility = View.GONE
        } else {
            binding.bottomNavigation.visibility = View.VISIBLE
        }
    }

}