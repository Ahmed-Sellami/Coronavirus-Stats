package com.example.coronavirus_stats.models

import com.example.coronavirus_stats.models.CountryStatHistoryPerDay

data class WorldStatHistoryResponse(
    val count: Double,
    val date: String,
    val result: CountryStatHistoryPerDay
)