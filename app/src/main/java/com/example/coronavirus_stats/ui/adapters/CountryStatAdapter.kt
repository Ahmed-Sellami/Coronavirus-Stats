package com.example.coronavirus_stats.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.coronavirus_stats.databinding.ListItemBinding
import com.example.coronavirus_stats.models.CountryCurrentStat
import com.example.coronavirus_stats.util.SharedVariables.country
import com.example.coronavirus_stats.util.SortEnum
import com.example.coronavirus_stats.util.setCountriesFlagEmoji
import java.util.*


private val countriesFlagsEmoji =
    setCountriesFlagEmoji()

typealias ListChangeListener = () -> Unit
//Todo: Transform long types to typealias

class CountryStatAdapter(
    private val listChangeListener: ListChangeListener,
    private val clickListener: CountryCurrentStatListener?
) : ListAdapter<CountryCurrentStat, CountryStatAdapter.CountryCurrentStatViewHolder>(
    DiffCallback
) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CountryCurrentStatViewHolder {
        return CountryCurrentStatViewHolder(
            ListItemBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: CountryCurrentStatViewHolder, position: Int) {
        val countryStat = getItem(position)
        holder.bind(countryStat, clickListener!!)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CountryCurrentStat>() {
        override fun areItemsTheSame(
            oldItem: CountryCurrentStat,
            newItem: CountryCurrentStat
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: CountryCurrentStat,
            newItem: CountryCurrentStat
        ): Boolean {
            return oldItem.country == newItem.country
        }
    }

    class CountryCurrentStatViewHolder(private var binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(countryStat: CountryCurrentStat, listener: CountryCurrentStatListener) {
            binding.countryCurrentStat = countryStat

            val emoji = countriesFlagsEmoji[countryStat.country]
            binding.emoji.text = emoji

            binding.clickListener = listener
            binding.executePendingBindings()

            if(country == countryStat.country){
                binding.home.visibility = ViewGroup.VISIBLE
            } else {
                binding.home.visibility = ViewGroup.GONE
            }
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<CountryCurrentStat>,
        currentList: MutableList<CountryCurrentStat>
    ) {
        super.onCurrentListChanged(previousList, currentList)

        listChangeListener()
    }
}

class CountryCurrentStatListener(val clickListener: (countryCurrentStat: CountryCurrentStat) -> Unit) {
    fun onClick(country: CountryCurrentStat) {
        clickListener(country)
    }
}

fun MutableList<CountryCurrentStat?>.sortStatsBy(sortOrder: SortEnum): List<CountryCurrentStat?> {
    val list = this.toMutableList()
    val home = list[0]
    if (country != null){
        list.removeAt(0)
    }
    when (sortOrder) {
        SortEnum.AFFECTED -> {
            list.sortByDescending { it?.confirmed }
        }
        SortEnum.DEATHS -> {
            list.sortByDescending { it?.deaths }
        }
        SortEnum.RECOVERED -> {
            list.sortByDescending { it?.recovered}
        }
    }
    if (country != null){
        list.add(0, home)
    }
    return list
}

fun MutableList<CountryCurrentStat?>.search(query: String?): List<CountryCurrentStat?> {
    val list = this.toMutableList()
    if (!query.isNullOrEmpty()) {
        var index = 0
        while (index < list.size) {
            val item = list[index]

            val posOfQueryInsideCountryName =
                item?.country?.toLowerCase()?.indexOf(query.toLowerCase())

            if (posOfQueryInsideCountryName != 0) {
                list.removeAt(index)
                index--
            }
            index++
        }
    }

    return list
}