package me.jeybi.uztaxi.ui.main.fragments

import android.os.Bundle
import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import kotlinx.android.synthetic.main.bottom_sheet_where.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.SearchItemModel
import me.jeybi.uztaxi.ui.BaseFragment
import me.jeybi.uztaxi.ui.adapters.CarsAdapter
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.ui.main.bottomsheet.BottomSheetOrderFilter


class WhereToFragment : BaseFragment(), CarsAdapter.SearchItemClickListener {
    override fun setLayoutId(): Int {
        return R.layout.bottom_sheet_where
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        recyclerViewCars.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        fillWithDummyData()

        rvOrder.setOnClickListener {
            (activity as MainActivity).startFindingCar()
        }

        imageViewFilterCar.setOnClickListener {
            BottomSheetOrderFilter().show(childFragmentManager,null)
        }

    }

    private fun fillWithDummyData() {
        val items = ArrayList<SearchItemModel>()

        items.add(SearchItemModel(R.drawable.car_1, "Econom", "from 4000 sum"))
        items.add(SearchItemModel(R.drawable.car_2, "Comfort", "from 7000 sum"))
        items.add(SearchItemModel(R.drawable.car_3, "Business", "from 12000 sum"))
        items.add(SearchItemModel(R.drawable.car_4, "Delivery", "from 15000 sum"))

        recyclerViewCars.adapter = CarsAdapter(items, this)
        val helper: SnapHelper = GravitySnapHelper(Gravity.START)
        helper.attachToRecyclerView(recyclerViewCars)

    }

    override fun onSearchClicked() {

    }


}