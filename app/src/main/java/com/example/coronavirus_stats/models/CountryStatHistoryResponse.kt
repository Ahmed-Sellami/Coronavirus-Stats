package com.example.coronavirus_stats.models

import com.example.coronavirus_stats.models.CountryStatHistoryPerDay

data class CountryStatHistoryResponse(
    val count: Double,
    val result: Map<String, CountryStatHistoryPerDay>
)