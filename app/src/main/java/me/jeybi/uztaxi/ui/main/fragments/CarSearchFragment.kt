package me.jeybi.uztaxi.ui.main.fragments

import android.os.Bundle
import kotlinx.android.synthetic.main.bottom_sheet_car_search.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.main.MainActivity

class CarSearchFragment : BaseFragment() {
    override fun setLayoutId(): Int {
        return R.layout.bottom_sheet_car_search
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {
//        rvCancel.setOnClickListener {
//            (activity as MainActivity).onCancelSearchClicked()
//        }
    }


}