package com.example.coronavirus_stats.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.coronavirus_stats.ui.fragments.ActiveCasesStatFragment
import com.example.coronavirus_stats.ui.fragments.OverallChartFragment

class ChartsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment =  when (position) {
        ACTIVE_CASES_POSITION -> ActiveCasesStatFragment()
        OVERALL_POSITION -> OverallChartFragment()
        else -> throw IllegalArgumentException("No item")
    }

    override fun getItemCount(): Int =
        VIEWS_NUMBER

    companion object{
        const val VIEWS_NUMBER = 2

        const val ACTIVE_CASES_POSITION = 0
        const val OVERALL_POSITION = 1
    }

}