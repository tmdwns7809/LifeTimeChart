package com.devesj.lifetimechart.chart

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.devesj.lifetimechart.R
import com.devesj.lifetimechart.databinding.FragmentChartBinding
import com.devesj.lifetimechart.db.Time
import com.devesj.lifetimechart.time.TimeViewModel
import com.devesj.lifetimechart.time.TimeViewModelFactory
import com.devesj.lifetimechart.util.ColorUtil
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChartFragment : Fragment() {

    // ViewBinding 변수를 선언
    private var _binding: FragmentChartBinding? = null
    private val binding get() = _binding!!

    private val timeViewModel: TimeViewModel by viewModels {
        TimeViewModelFactory(requireActivity().application)
    }

    // 1일을 밀리초로 계산 (24시간 * 60분 * 60초 * 1000밀리초)
    val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
    val GMTOffsetInMillis = TimeZone.getDefault().let { (it.rawOffset + it.dstSavings).toLong() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ViewBinding 초기화
        _binding = FragmentChartBinding.inflate(inflater, container, false)

        // ViewModel의 LiveData 관찰
        timeViewModel.allTimes.observe(viewLifecycleOwner) { data ->
            drawChart(data)
        }

        // X축 설정
        val xAxis = binding.lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        // XAxis의 ValueFormatter를 사용하여 초 단위를 시간 형식으로 변환
        xAxis.valueFormatter = object : ValueFormatter() {
            private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            override fun getFormattedValue(value: Float): String {
                val millis  = value.toLong() * 1000 // 초 단위를 다시 밀리초로 변환
                return dateFormat.format(Date(millis))
            }
        }
        xAxis.textColor = Color.WHITE

        // Y축 설정
        val yAxis = binding.lineChart.axisRight
        yAxis.valueFormatter = object : ValueFormatter() {
            private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            override fun getFormattedValue(value: Float): String {
                val millis  = value.toLong()
                return dateFormat.format(Date(millis))
            }
        }
        yAxis.textColor = Color.WHITE

        binding.lineChart.axisLeft.isEnabled = false

        return binding.root
    }

    private fun drawChart(data: List<Time>) {
        if (data.isEmpty()) {
            return
        }

        val map = HashMap<String, ArrayList<Entry>>()
        val startDay = stripTimeFromMillis(data[0].endTime)
        var endDay = 0L
        for (time in data) {
            if (!map.containsKey(time.name)) {
                map[time.name] = ArrayList()
            }

            val entries = map[time.name]!!
            val nowDay = stripTimeFromMillis(time.endTime)
            if (entries.isEmpty()) {
                fillZeroBetweenDays(startDay, nowDay, entries)
            }

            fillZeroUntilLast(nowDay, entries)

            entries.last().y += time.elapsedTime

            endDay = nowDay
        }

        val dataSets: MutableList<ILineDataSet> = arrayListOf<LineDataSet>() as MutableList<ILineDataSet>
        for ((i, pair) in map.entries.withIndex()) {
            val entries = pair.value

            fillZeroUntilLast(endDay, entries)

            val dataSet = LineDataSet(entries, pair.key)
            dataSet.color = ColorUtil.getColorForValue(i, map.size - 1)
            dataSet.lineWidth = 2f
            dataSet.circleRadius = 4f
            dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.purple))

            dataSets.add(dataSet)
        }

        // Create the data object with the dataset
        val lineData = LineData(dataSets)

        // Set data to the chart
        binding.lineChart.data = lineData

        // Customize chart legend
        val legend = binding.lineChart.legend
        legend.form = Legend.LegendForm.LINE
        legend.textSize = 12f
        legend.textColor = Color.WHITE

        // Refresh the chart
        binding.lineChart.invalidate()
    }

    private fun fillZeroUntilLast(end: Long, entries: ArrayList<Entry>) {
        var lastDay = entries.last().x.toLong() * 1000
        if (lastDay != end) {
            lastDay += ONE_DAY_IN_MILLIS
            fillZeroBetweenDays(lastDay, end, entries)
        }
    }

    private fun fillZeroBetweenDays(start: Long, end: Long, entries: ArrayList<Entry>) {
        for (i in start..end step ONE_DAY_IN_MILLIS) {
            entries.add(Entry((i/1000).toFloat(), 0f))
        }
    }

    private fun stripTimeFromMillis(timeInMillis: Long): Long {
        // 하루 단위로 값을 계산
        return ((timeInMillis + GMTOffsetInMillis) / ONE_DAY_IN_MILLIS) * ONE_DAY_IN_MILLIS
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 메모리 누수를 방지하기 위해 뷰가 파괴될 때 바인딩 해제
        _binding = null
    }

}