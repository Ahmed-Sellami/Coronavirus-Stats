package com.example.coronavirus_stats.models

data class CountryStatHistoryPerDay(
    val confirmed: Double,
    val deaths: Double,
    val recovered: Double
)