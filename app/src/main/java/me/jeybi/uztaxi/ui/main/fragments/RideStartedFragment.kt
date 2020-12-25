package me.jeybi.uztaxi.ui.main.fragments

import android.os.Bundle
import kotlinx.android.synthetic.main.bottom_ride_start.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.main.MainActivity

class RideStartedFragment : BaseFragment() {
    override fun setLayoutId(): Int {
        return R.layout.bottom_ride_start
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {
        rvEditRide.setOnClickListener {
            (activity as MainActivity).editRideClicked()
        }
    }

}