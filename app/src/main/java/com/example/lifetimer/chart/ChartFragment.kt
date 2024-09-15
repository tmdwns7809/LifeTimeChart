package com.example.lifetimer.chart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lifetimer.R
import com.example.lifetimer.databinding.FragmentChartBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ChartFragment : Fragment() {

    // ViewBinding 변수를 선언
    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ViewBinding 초기화
        _binding = FragmentChartBinding.inflate(inflater, container, false)

        // Create dummy data entries for the chart
        val entries = ArrayList<Entry>()
        entries.add(Entry(1f, 10f))
        entries.add(Entry(2f, 20f))
        entries.add(Entry(3f, 15f))
        entries.add(Entry(4f, 30f))
        entries.add(Entry(5f, 25f))

        // Create a dataset and give it a type
        val dataSet = LineDataSet(entries, "Sample Data")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.purple)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.purple))

        // Create the data object with the dataset
        val lineData = LineData(dataSet)

        // Set data to the chart
        binding.lineChart.data = lineData

        // Customize chart legend
        val legend = binding.lineChart.legend
        legend.form = Legend.LegendForm.LINE
        legend.textSize = 12f

        // Refresh the chart
        binding.lineChart.invalidate()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 메모리 누수를 방지하기 위해 뷰가 파괴될 때 바인딩 해제
        _binding = null
    }

}