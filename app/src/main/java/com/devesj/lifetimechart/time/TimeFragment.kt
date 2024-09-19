package com.devesj.lifetimechart.time

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devesj.lifetimechart.databinding.FragmentTimeBinding
import com.devesj.lifetimechart.util.ColorUtil
import com.devesj.lifetimechart.util.NameUtil


class TimeFragment : Fragment(), TimeNameItemAdapter.OnItemClickListener {

    // ViewBinding 변수를 선언
    private var _binding: FragmentTimeBinding? = null
    private val binding get() = _binding!!
    private lateinit var timeReceiver: BroadcastReceiver

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

    private lateinit var adapter: TimeNameItemAdapter
    private var selectedItem: String? = null
    private var color: Int = 0

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

        // 서비스 바인딩
        val intent = Intent(requireContext(), TimeService::class.java)
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        // 이름 목록 생성
        adapter = TimeNameItemAdapter(NameUtil.CATEGORIES, this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.btnStart.setOnClickListener {
            selectedItem?.let {
                // 선택된 아이템의 이름을 사용하여 작업 수행
                binding.name.text = it
                val startIntent = Intent(requireActivity(), TimeService::class.java)
                startIntent.action = "START"
                startIntent.putExtra("name", it)  // 문자열 데이터 추가
                startIntent.putExtra("color", color)
                requireActivity().startService(startIntent)  // Android O 이하
            } ?: run {
                binding.name.text = "아이템이 선택되지 않았습니다."
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onItemClick(name: String, color: Int) {
        selectedItem = name
        this.color = color
        // 아이템의 색상으로 Fragment 배경색 변경
        binding.fragmentLayout.setBackgroundColor(color)
    }

    private fun updateUIWithServiceData() {
        // 서비스에서 데이터를 가져와서 UI 업데이트
        timeService?.let {
            // 가져온 데이터를 텍스트뷰 등 UI에 반영
            binding.tvTimer.text = it.getFormatElapsedTime()
            selectedItem = it.getName()
            binding.name.text = selectedItem
            color = it.getColor()
            binding.fragmentLayout.setBackgroundColor(color)
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