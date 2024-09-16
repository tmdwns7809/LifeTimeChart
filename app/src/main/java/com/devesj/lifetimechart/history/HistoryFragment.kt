package com.devesj.lifetimechart.history

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.devesj.lifetimechart.databinding.FragmentHistoryBinding
import com.devesj.lifetimechart.time.TimeViewModel
import com.devesj.lifetimechart.time.TimeViewModelFactory

class HistoryFragment : Fragment() {

    private lateinit var timeItemAdapter: TimeItemAdapter
    // ViewBinding 변수를 선언
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!


    private val timeViewModel: TimeViewModel by viewModels {
        TimeViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ViewBinding 초기화
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        timeItemAdapter = TimeItemAdapter(emptyList())
        binding.recyclerView.apply {
            adapter = timeItemAdapter
            layoutManager = LinearLayoutManager(context)
        }

        timeViewModel.allTimesReverse.observe(viewLifecycleOwner) { data ->
            data?.let { timeItemAdapter.updateItems(it) }
        }

    }
}