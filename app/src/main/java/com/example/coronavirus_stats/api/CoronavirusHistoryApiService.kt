package com.example.coronavirus_stats.api

import com.example.coronavirus_stats.models.CountryStatHistoryResponse
import com.example.coronavirus_stats.models.WorldStatHistoryResponse
import com.example.coronavirus_stats.network.createNetworkClient
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL = "https://covidapi.info/api/v1/"

interface CoronavirusHistoryApiService{
    @GET("country/{country}/{date}")
    fun getCountryStatHistoryPerDay(@Path("country") country: String?, @Path("date") date: String):
            Deferred<CountryStatHistoryResponse>

    @GET("global/{date}")
    fun getWorldStatHistoryPerDay(@Path("date") date: String):
            Deferred<WorldStatHistoryResponse>
}

object CoronavirusHistoryApi {
    val retrofitService : CoronavirusHistoryApiService by lazy {
        createNetworkClient(BASE_URL)
            .create(CoronavirusHistoryApiService::class.java) }
}