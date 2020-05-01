package com.example.coronavirus_stats.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.coronavirus_stats.R
import com.example.coronavirus_stats.databinding.OverallChartFragmentBinding
import com.example.coronavirus_stats.ui.OverviewViewModel
import com.example.coronavirus_stats.util.CustomMarkerView
import com.example.coronavirus_stats.util.getTheSixPreviousDays
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class OverallChartFragment : Fragment() {

    private var previousSixDays =
        getTheSixPreviousDays("MM-dd", true)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = OverallChartFragmentBinding.inflate(inflater)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = activity?.run {
            ViewModelProvider(this)[OverviewViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        customizeChart(binding.chart)

        return binding.root
    }

    private fun customizeChart(chart: LineChart) {
        val desc = Description()
        desc.text = ""
        chart.description = desc
        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        previousSixDays = previousSixDays.reversed() as MutableList<String>
        previousSixDays.add(0, "0")

        chart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return previousSixDays.getOrNull(value.toInt()) ?: value.toString()
            }
        }
        chart.axisLeft.valueFormatter = object : LargeValueFormatter(){}
        chart.axisLeft.axisMinimum = 0f
        chart.legend.setDrawInside(true)
        chart.legend.yOffset = 104f

        chart.isHighlightPerTapEnabled = true
        chart.setDrawMarkers(true)
        chart.setTouchEnabled(true)

        chart.marker = CustomMarkerView(
            context,
            R.layout.marker
        )

    }
}