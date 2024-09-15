package com.example.lifetimer.time

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.lifetimer.databinding.FragmentTimeBinding
import com.example.lifetimer.db.Time


class TimeFragment : Fragment() {

    // ViewBinding 변수를 선언
    private var _binding: FragmentTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var timeReceiver: BroadcastReceiver

    // 뷰모델은 ChartFragment랑 HistoryFragment에서 사용하면 될 듯
    private val timeViewModel: TimeViewModel by viewModels {
        TimeViewModelFactory(requireActivity().application)
    }

    private var timeService: TimeService? = null
    private var isBound = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as TimeService.LocalBinder
            timeService = binder.getService()
            isBound = true
            updateUIWithServiceData() // 서비스에서 데이터 가져와서 UI 업데이트
        }

        override fun onServiceDisconnected(name: ComponentName) {
            timeService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 브로드캐스트 리시버 설정
        timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val time = intent.getStringExtra("time")
                binding.tvTimer.text = time // UI 업데이트
            }
        }
        val intentFilter = IntentFilter("TIME_UPDATED")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 안드로이드 12 이상
            requireContext().registerReceiver(
                timeReceiver,
                intentFilter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            // 안드로이드 12 미만
            ContextCompat.registerReceiver(
                requireContext(),
                timeReceiver,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ViewBinding 초기화
        _binding = FragmentTimeBinding.inflate(inflater, container, false)

        binding.btnStart.setOnClickListener {
            val startIntent = Intent(requireActivity(), TimeService::class.java)
            startIntent.action = "START"
            requireActivity().startService(startIntent)  // Android O 이하
        }

        binding.btnPause.setOnClickListener {
            val stopIntent = Intent(requireActivity(), TimeService::class.java)
            stopIntent.action = "STOP"
            requireActivity().startService(stopIntent)
        }

        binding.btnReset.setOnClickListener {
            val stopIntent = Intent(requireActivity(), TimeService::class.java)
            stopIntent.action = "RESET"
            requireActivity().startService(stopIntent)
        }

        // ViewModel의 LiveData 관찰
        timeViewModel.allTimes.observe(viewLifecycleOwner) { data ->
            var a = data.size
        }

        // 서비스 바인딩
        val intent = Intent(requireContext(), TimeService::class.java)
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        return binding.root
    }

    private fun updateUIWithServiceData() {
        // 서비스에서 데이터를 가져와서 UI 업데이트
        timeService?.let {
            val currentTime = it.getFormatElapsedTime()
            // 가져온 데이터를 텍스트뷰 등 UI에 반영
            binding.tvTimer.text = currentTime
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 메모리 누수를 방지하기 위해 뷰가 파괴될 때 바인딩 해제
        _binding = null

        requireActivity().unregisterReceiver(timeReceiver)

        if (isBound) {
            requireActivity().unbindService(serviceConnection)
            isBound = false
        }
    }
}