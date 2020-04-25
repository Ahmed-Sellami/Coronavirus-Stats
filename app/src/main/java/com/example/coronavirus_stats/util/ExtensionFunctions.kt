package com.example.coronavirus_stats.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.example.coronavirus_stats.R
import com.example.coronavirus_stats.models.CountryStatHistoryPerDay
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet


fun String.formatToBeShowed(): String{
    var str = this
    if(this.indexOf(",") != -1) {
        str = str.replace(",", ".")
        str = if (str.indexOf(".", 4) == -1)
            str.substring(0, 5) + 'K'
        else
            str.substring(0, 5) + 'M'
    }
    return str
}


fun Int.toStringWithCommas() : String{
    var str = this.toString()
    val isNegative = str[0] == '-'
    if (isNegative) {str = str.replace("-", "")}
    var step = 3
    for(i in str.length-3 downTo 1 step step){
        str = str.substring(0, i) + ',' + str.substring(i, str.length)
        step++
    }
    if(isNegative) {str = "-$str"}
    return str
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun ArrayList<Entry>.addEntry(dataHistory: List<CountryStatHistoryPerDay?>, stat: Double, index: Int){
    Entry((dataHistory.size - index).toFloat(), stat.toFloat()).let { this.add(it) }
}


fun LineDataSet.customizeDataSet(entries: ArrayList<Entry>, chart: LineChart): LineDataSet {
    this.setDrawFilled(true)
    this.setDrawValues(false)

    val color = when(this.label){
        "Affected" -> ContextCompat.getColor(chart.context,
            R.color.yellow
        )
        "Deaths" -> ContextCompat.getColor(chart.context,
            R.color.red
        )
        else -> ContextCompat.getColor(chart.context,
            R.color.green
        )
    }

    this.color = color

    this.fillDrawable = when(this.label){
        "Affected" -> ContextCompat.getDrawable(
            chart.context,
            R.drawable.yellow_to_transparent_gradient
        )
        "Deaths" -> ContextCompat.getDrawable(chart.context,
            R.drawable.red_to_transparent_gradient
        )
        else -> ContextCompat.getDrawable(chart.context,
            R.drawable.green_to_transparent_gradient
        )
    }

    this.setColors(color)

    for (index in 0..entries.size - 2) {
        this.circleColors[0] = color
    }
    return this
}