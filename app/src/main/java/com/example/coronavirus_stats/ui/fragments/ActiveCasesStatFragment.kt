package com.example.coronavirus_stats.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.coronavirus_stats.databinding.ActiveCasesStatFragmentBinding
import com.example.coronavirus_stats.ui.OverviewViewModel
import com.example.coronavirus_stats.util.minusToNullable
import com.example.coronavirus_stats.util.plusToNullable
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description


class ActiveCasesStatFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = ActiveCasesStatFragmentBinding.inflate(inflater)

        val viewModel = activity?.run {
            ViewModelProvider(this)[OverviewViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        customizeChart(binding.chart1)
        customizeChart(binding.chart2)

        viewModel.countryHistoryActiveCases.observe(viewLifecycleOwner, Observer {
            binding.spinKit.visibility = View.GONE
            binding.spinKit1.visibility = View.GONE

            val countryCurrentStat = viewModel.countryCurrentStat.value

            val activeCases = countryCurrentStat?.confirmed.minusToNullable(countryCurrentStat?.deaths.plusToNullable(countryCurrentStat?.recovered))
            val totalCases = countryCurrentStat?.confirmed ?: 0.0

            binding.circularProgress.setProgress(activeCases / totalCases * 100.0, 100.0)
        })

        binding.circularProgress.setProgressTextAdapter { currentProgress -> String.format("%.1f", currentProgress) + "%" }

        return binding.root
    }

    private fun customizeChart(chart: LineChart) {
        val desc = Description()
        desc.text = ""
        chart.description = desc
        chart.xAxis.isEnabled = false
        chart.axisLeft.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false

        chart.setTouchEnabled(false)
    }

}


