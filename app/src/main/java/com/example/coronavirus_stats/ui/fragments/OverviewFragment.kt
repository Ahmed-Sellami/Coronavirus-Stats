package com.example.coronavirus_stats.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coronavirus_stats.*
import com.example.coronavirus_stats.databinding.FragmentOverviewBinding
import com.example.coronavirus_stats.models.CountryCurrentStat
import com.example.coronavirus_stats.ui.OverviewViewModel
import com.example.coronavirus_stats.ui.adapters.*
import com.example.coronavirus_stats.util.MarginItemDecoration
import com.example.coronavirus_stats.util.SharedVariables.country
import com.example.coronavirus_stats.util.SortEnum
import com.example.coronavirus_stats.util.hideKeyboard
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*


class OverviewFragment : Fragment(){

    private lateinit var viewModel : OverviewViewModel

    private lateinit var binding : FragmentOverviewBinding

    private var adapter =
        CountryStatAdapter({}, null)

    private var adapterOriginalList = mutableListOf<CountryCurrentStat?>()

    private val coroutineScope = CoroutineScope(
        Job() + Dispatchers.Main
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentOverviewBinding.inflate(inflater)

        viewModel = activity?.run {
            ViewModelProvider(this)[OverviewViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        // Allow Data Binding to Observe LiveData with the lifecycle of this Fragment's view
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        binding.viewPager.adapter =
            ChartsPagerAdapter(this)


        setupTabLayout()
        setupSpinner()
        setupRecyclerView()

        adapter = binding.recyclerView.adapter as CountryStatAdapter

        setupObservers()
        setupSearching()
        setupClickListener()

        return binding.root
    }


    private fun setupRecyclerView(){
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.item_margin).toInt()
                )
            )

            adapter =
                CountryStatAdapter(
                    { binding.recyclerView.layoutManager?.scrollToPosition(0) },
                    CountryCurrentStatListener { innerItem ->
                        coroutineScope.launch {
                            viewModel.getCountryHistoryStat(innerItem)
                        }
                        viewModel.isGlobal = false
                        binding.globe.visibility = View.VISIBLE
                        context.hideKeyboard(this)
                        binding.root.postDelayed(
                            {
                                binding.root.scrollTo(0, binding.root.top)
                            }, 100
                        )
                        binding.spinKit.visibility = View.VISIBLE
                    }
                )
        }
    }

    private fun setupObservers(){
        viewModel.countriesCurrentStat.observe(viewLifecycleOwner, Observer {
            adapterOriginalList = it.toMutableList()
            if (country != null){
                var index = -1
                do {
                    index++
                } while ((adapterOriginalList[index]?.country != country) && (index < adapterOriginalList.size - 1))
                val home = adapterOriginalList[index]
                adapterOriginalList.removeAt(index)
                adapterOriginalList.add(0, home)
            }
            adapter.submitList(adapterOriginalList)
        })


        viewModel.isListReady.observe(viewLifecycleOwner, Observer {
            if(it){
                binding.spinKit1.visibility = View.GONE
            }
        })
        viewModel.countryHistoryStat.observe(viewLifecycleOwner, Observer {
            if(it != null){
                binding.spinKit.visibility = View.GONE
            }
        })

    }

    private fun setupSearching(){
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.submitList(adapterOriginalList.search(query))
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                adapter.submitList(adapterOriginalList.search(query))
                binding.root.scrollTo(0, binding.recyclerView.bottom)
                return false
            }

        })
    }

    private fun setupClickListener(){
        binding.globe.setOnClickListener {
            it.visibility = View.GONE
            viewModel.isGlobal = true
            coroutineScope.launch {
                viewModel.getCountryHistoryStat(null)
            }
            binding.spinKit.visibility = View.VISIBLE
        }
    }

    private fun setupSpinner(){
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_choices_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinner.adapter = adapter
        }

        var selected : SortEnum

        binding.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selected = when(pos){
                    1 -> SortEnum.DEATHS
                    2 -> SortEnum.RECOVERED
                    else -> SortEnum.AFFECTED
                }

                if (adapter.itemCount != 0) {
                    adapter.submitList(adapter.currentList.sortStatsBy(selected))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selected = SortEnum.AFFECTED
            }
        }
    }

    private fun setupTabLayout(){
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { currentTab, currentPosition ->
            currentTab.text = when (currentPosition) {
                ChartsPagerAdapter.ACTIVE_CASES_POSITION -> getString(
                    R.string.active_cases
                )
                ChartsPagerAdapter.OVERALL_POSITION -> getString(
                    R.string.overall
                )
                else -> getString(R.string.no_item)
            }
        }.attach()

        for (i in 0 until binding.tabLayout.tabCount) {
            val tab = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(20, 0, 20, 0)
            tab.requestLayout()
        }
    }

}