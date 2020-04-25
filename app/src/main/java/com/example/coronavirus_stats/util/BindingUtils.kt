package com.example.coronavirus_stats.util

import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.databinding.BindingAdapter
import com.example.coronavirus_stats.R
import com.example.coronavirus_stats.models.CountryStatHistoryPerDay
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet


@BindingAdapter("currentStat")
fun setCurrentStat(textView: TextView, value: Double){
    textView.text = value.toInt().toStringWithCommas()
}


@BindingAdapter("simpleChartData")
fun setSimpleChartData(chart: LineChart, dataHistory: List<Double?>?) {

    val entries = ArrayList<Entry>()
    if ((dataHistory != null) && (dataHistory.size >= 5)) {
        for (index in 0..4) {
            dataHistory[index]?.toFloat()?.let { Entry((4 - index).toFloat(), it) }?.let { entries.add(it) }
        }
    }
    val dataSet = LineDataSet(entries.reversed(), "Label")
    dataSet.setDrawFilled(true)
    dataSet.setDrawValues(false)

    val color = if(chart.id == R.id.chart1)
        getColor(chart.context, R.color.yellow)
    else
        getColor(chart.context, R.color.blue)

    dataSet.color = color
    dataSet.setColors(color)
    for (index in 0..4) {
        dataSet.circleColors[0] = color
    }

    dataSet.fillDrawable = if(chart.id == R.id.chart1)
        getDrawable(chart.context, R.drawable.yellow_to_transparent_gradient)
    else
        getDrawable(chart.context, R.drawable.blue_to_transparent_gradient)

    val lineData = LineData(dataSet)
    chart.data = lineData
    chart.invalidate()
}


@BindingAdapter("chartData")
fun setChartData(chart: LineChart, dataHistory: List<CountryStatHistoryPerDay?>?) {
    val affectedEntries = ArrayList<Entry>()
    val deathsEntries = ArrayList<Entry>()
    val recoveredEntries = ArrayList<Entry>()

    if (dataHistory != null) {
        for (index in dataHistory.indices) {
            dataHistory[index]?.confirmed?.let { affectedEntries.addEntry(dataHistory, it, index) }
            dataHistory[index]?.deaths?.let { deathsEntries.addEntry(dataHistory, it, index) }
            dataHistory[index]?.recovered?.let { recoveredEntries.addEntry(dataHistory, it, index) }
        }
    }

    val dataSets: MutableList<ILineDataSet> = ArrayList()

    val affectedDataSet = LineDataSet(affectedEntries.reversed(), "Affected").customizeDataSet(affectedEntries, chart)
    val deathsDataSet = LineDataSet(deathsEntries.reversed(), "Deaths").customizeDataSet(deathsEntries, chart)
    val recoveredDataSet = LineDataSet(recoveredEntries.reversed(), "Recovered").customizeDataSet(recoveredEntries, chart)

    affectedDataSet.setDrawHighlightIndicators(false)
    deathsDataSet.setDrawHighlightIndicators(false)
    recoveredDataSet.setDrawHighlightIndicators(false)

    dataSets.add(affectedDataSet)
    dataSets.add(deathsDataSet)
    dataSets.add(recoveredDataSet)

    val lineData = LineData(dataSets)
    chart.data = lineData
    chart.invalidate()
}