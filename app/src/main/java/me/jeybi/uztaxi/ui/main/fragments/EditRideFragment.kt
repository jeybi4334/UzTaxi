package me.jeybi.uztaxi.ui.main.fragments

import android.os.Bundle
import kotlinx.android.synthetic.main.bottom_edit_ride.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.main.MainActivity

class EditRideFragment : BaseFragment() {

    override fun setLayoutId(): Int {
        return R.layout.bottom_edit_ride
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {
        rvCancelRide.setOnClickListener {
            (activity as MainActivity).onCancelRideClicked()
        }

    }


}