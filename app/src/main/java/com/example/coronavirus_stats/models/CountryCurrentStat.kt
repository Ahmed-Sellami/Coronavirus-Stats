package com.example.coronavirus_stats.models

data class CountryCurrentStat (
    val country: String,
    val confirmed: Double,
    val deaths: Double,
    val recovered: Double
)