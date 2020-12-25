package me.jeybi.uztaxi.ui.main.fragments

import android.os.Bundle
import kotlinx.android.synthetic.main.bottom_sheet_found_driver.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.main.MainActivity

class CarFoundFragment : BaseFragment() {
    override fun setLayoutId(): Int {
        return R.layout.bottom_sheet_found_driver
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {
        textViewCancel.setOnClickListener {
            (activity as MainActivity).onRideStarted()
        }
    }

}