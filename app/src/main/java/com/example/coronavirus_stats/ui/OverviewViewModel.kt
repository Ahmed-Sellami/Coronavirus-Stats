package com.example.coronavirus_stats.ui

import android.util.Log
import androidx.lifecycle.*
import com.example.coronavirus_stats.models.CountryCurrentStat
import com.example.coronavirus_stats.api.CoronavirusHistoryApi
import com.example.coronavirus_stats.api.CoronavirusMonitorApi
import com.example.coronavirus_stats.models.CountryStatHistoryPerDay
import com.example.coronavirus_stats.util.formatToBeShowed
import com.example.coronavirus_stats.util.getTheSixPreviousDays
import com.example.coronavirus_stats.util.setCountriesFlagEmoji
import com.example.coronavirus_stats.util.toStringWithCommas
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.set

class OverviewViewModel : ViewModel() {

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(
        viewModelJob + Dispatchers.Main
    )

    private val _countryCurrentStat = MutableLiveData<CountryCurrentStat>()
    val countryCurrentStat: LiveData<CountryCurrentStat>
        get() = _countryCurrentStat

    private val _countryHistoryStat = MutableLiveData<List<CountryStatHistoryPerDay?>>()
    val countryHistoryStat: LiveData<List<CountryStatHistoryPerDay?>>
        get() = _countryHistoryStat

    private val _countryHistoryAffected = MutableLiveData<List<Double?>>()
    val countryHistoryAffected: LiveData<List<Double?>>
        get() = _countryHistoryAffected

    private val _countryHistoryActiveCases = MutableLiveData<List<Double?>>()
    val countryHistoryActiveCases: LiveData<List<Double?>>
        get() = _countryHistoryActiveCases

    private var _countriesCurrentStat = MutableLiveData<MutableList<CountryCurrentStat?>>()
    val countriesCurrentStat: LiveData<MutableList<CountryCurrentStat?>>
        get() = _countriesCurrentStat

    private val _isListReady = MutableLiveData<Boolean>()
    val isListReady: LiveData<Boolean>
        get() = _isListReady

    private val _totalAffected = MutableLiveData<String?>()
    val totalAffected: LiveData<String?>
        get() = _totalAffected
    private val _totalDeaths = MutableLiveData<String?>()
    val totalDeaths: LiveData<String?>
        get() = _totalDeaths
    private val _totalRecovered = MutableLiveData<String?>()
    val totalRecovered: LiveData<String?>
        get() = _totalRecovered

    private val _newAffected = MutableLiveData<String?>()
    val newAffected: LiveData<String?>
        get() = _newAffected
    private val _newDeaths = MutableLiveData<String?>()
    val newDeaths: LiveData<String?>
        get() = _newDeaths
    private val _newRecovered = MutableLiveData<String?>()
    val newRecovered: LiveData<String?>
        get() = _newRecovered

    private val _totalAffectedSmallFormat = MutableLiveData<String?>()
    val totalAffectedSmallFormat: LiveData<String?>
        get() = _totalAffectedSmallFormat

    private val _totalActiveCases = MutableLiveData<String?>()
    val totalActiveCases: LiveData<String?>
        get() = _totalActiveCases

    private val _countryName = MutableLiveData<String?>()
    val countryName: LiveData<String?>
        get() = _countryName
    private val _countryFlag = MutableLiveData<String?>()
    val countryFlag: LiveData<String?>
        get() = _countryFlag

    var isGlobal = true

    private val countriesISO = setCountriesISOCode()
    private val countriesFlagsEmoji =
        setCountriesFlagEmoji()
    private val previousSixDays =
        getTheSixPreviousDays(
            "yyyy-MM-dd",
            false
        )


    init {
        viewModelScope.launch {
            getCountryHistoryStat(null)
            getCurrentStatForEachCountry()
        }
    }

    fun getCountryHistoryStat(countryCurrentStat: CountryCurrentStat?) {
        val countryHistoryStatDelegate = mutableListOf<CountryStatHistoryPerDay?>()
        val countryHistoryAffectedDelegate = mutableListOf<Double?>()
        val countryHistoryActiveCasesDelegate = mutableListOf<Double?>()
        var todayAffected: Double
        var todayDeaths: Double
        var todayRecovered: Double

        coroutineScope.launch {
            var countryCurrentStatRef: CountryCurrentStat?
            if (isGlobal) {
                try {
                    val worldStat = CoronavirusMonitorApi.retrofitService.getWorldStat().await()

                    countryCurrentStatRef =
                        CountryCurrentStat(
                            "Global",
                            worldStat[0].confirmed.toDouble(),
                            worldStat[0].deaths.toDouble(),
                            worldStat[0].recovered.toDouble()
                        )

                } catch (e: Exception) {
                    countryCurrentStatRef = null
                    Log.e(
                        "OverviewViewModel",
                        "Problem with getting current world stat: ${e.message}"
                    )
                }
            } else {
                countryCurrentStatRef = countryCurrentStat
            }
            _countryCurrentStat.value = countryCurrentStatRef

            val countryTodayStat: CountryStatHistoryPerDay? =
                CountryStatHistoryPerDay(
                    countryCurrentStatRef?.confirmed ?: 0.0,
                    countryCurrentStatRef?.deaths ?: 0.0,
                    countryCurrentStatRef?.recovered ?: 0.0
                )
            todayAffected = countryCurrentStatRef?.confirmed ?: 0.0
            todayDeaths = countryCurrentStatRef?.deaths ?: 0.0
            todayRecovered = countryCurrentStatRef?.recovered ?: 0.0
            countryHistoryAffectedDelegate.add(todayAffected)
            countryHistoryActiveCasesDelegate.add(todayAffected - todayDeaths - todayRecovered)


            if (!isGlobal) {
                _countryName.value = countryCurrentStatRef?.country
                _countryFlag.value = countriesFlagsEmoji[_countryName.value]
            } else {
                _countryName.value = "Global"
                _countryFlag.value = "\uD83C\uDF0E"
            }

            _totalAffectedSmallFormat.value =
                countryCurrentStatRef?.confirmed?.toInt()?.toStringWithCommas()?.formatToBeShowed()

            _totalActiveCases.value =
                countryHistoryActiveCasesDelegate[0]?.toInt()?.toStringWithCommas()
                    ?.formatToBeShowed()

            countryHistoryStatDelegate.add(countryTodayStat)

            var countryISO: String? = null
            if (!isGlobal) {
                countryISO = countriesISO[countryCurrentStat?.country]

                if (countryISO == null) {
                    countryISO = when (countryCurrentStat?.country) {
                        "USA" -> "USA"
                        "UK" -> "GBR"
                        "S. Korea" -> "KOR"
                        "UAE" -> "ARE"
                        "Bosnia & Herzegovina" -> "BIH"
                        else -> countryCurrentStat?.country
                    }
                }
            }

            var updateHeaderValues = true

            for (day in previousSixDays) {
                try {
                    val result: CountryStatHistoryPerDay? = if (isGlobal) {
                        val worldHistoryStatResponse =
                            CoronavirusHistoryApi.retrofitService.getWorldStatHistoryPerDay(day)
                                .await()

                        worldHistoryStatResponse.result
                    } else {
                        val countryStatHistoryResponse =
                            CoronavirusHistoryApi.retrofitService.getCountryStatHistoryPerDay(
                                countryISO,
                                day
                            ).await()

                        countryStatHistoryResponse.result[day]
                    }

                    if (updateHeaderValues) {
                        notifyHeaderValues(
                            todayAffected.toInt().toStringWithCommas(),
                            todayDeaths.toInt().toStringWithCommas(),
                            todayRecovered.toInt().toStringWithCommas(),
                            (todayAffected - result?.confirmed!!).toInt().toStringWithCommas(),
                            (todayDeaths - result.deaths).toInt().toStringWithCommas(),
                            (todayRecovered - result.recovered).toInt().toStringWithCommas()
                        )
                        updateHeaderValues = false
                    }

                    countryHistoryAffectedDelegate.add(result?.confirmed)
                    countryHistoryActiveCasesDelegate.add(result?.confirmed!! - result.deaths - result.recovered)
                    countryHistoryStatDelegate.add(result)

                } catch (e: Exception) {
                    continue
                }
            }
            _countryHistoryAffected.value = countryHistoryAffectedDelegate
            _countryHistoryActiveCases.value = countryHistoryActiveCasesDelegate
            _countryHistoryStat.value = countryHistoryStatDelegate
        }
    }

    private fun getCurrentStatForEachCountry() {
        coroutineScope.launch {

            val countriesStats = mutableListOf<CountryCurrentStat?>()
            for (country in countriesISO.keys) {
                try {
                    val countryName = when (country) {
                        "United States" -> "USA"
                        else -> country
                    }

                    val countryStat =
                        CoronavirusMonitorApi.retrofitService.getCountryStat(countryName).await()
                    countriesStats.add(countryStat[0])
                    countriesStats.sortByDescending { it?.confirmed }
                    _countriesCurrentStat.value = countriesStats

                } catch (e: Exception) {
                    Log.i(
                        "OverviewViewModel",
                        "Problem with $country | Error: " + e.message
                    )
                    continue
                }
            }

            _isListReady.value = true
        }
    }

    private fun notifyHeaderValues(
        totalCases: String?,
        totalDeath: String?,
        totalRecovered: String?,
        newCases: String?,
        newDeaths: String?,
        newRecovered: String?
    ) {
        _totalAffected.value = totalCases
        _totalDeaths.value = totalDeath
        _totalRecovered.value = totalRecovered

        _newAffected.value = newCases
        _newDeaths.value = newDeaths
        _newRecovered.value = newRecovered
    }

    private fun setCountriesISOCode(): MutableMap<String, String> {
        val countriesISO: MutableMap<String, String> = HashMap()

        for (iso in Locale.getISOCountries()) {
            val l = Locale("", iso)
            countriesISO[l.displayCountry] = l.isO3Country
        }
        return countriesISO
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}