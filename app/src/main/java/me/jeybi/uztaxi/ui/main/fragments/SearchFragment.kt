package me.jeybi.uztaxi.ui.main.fragments

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.bottom_sheet_search.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseFragment

class SearchFragment : BaseFragment(){

    override fun setLayoutId(): Int {
        return R.layout.bottom_sheet_search
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        recyclerViewSearchHistory.layoutManager = LinearLayoutManager(activity)


    }


}