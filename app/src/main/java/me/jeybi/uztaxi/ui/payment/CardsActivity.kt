package me.jeybi.uztaxi.ui.payment

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_cards.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.utils.CustomPagerAdapter


class CardsActivity : BaseActivity() {

    var mAdapter: CustomPagerAdapter? = null


    override fun setLayoutId(): Int {
        return R.layout.activity_cards
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        rvBack.setOnClickListener {
            onBackPressed()
        }

        mAdapter = CustomPagerAdapter(this, this.supportFragmentManager)
        mViewPager!!.adapter = mAdapter!!
        mViewPager!!.setPageTransformer(false, mAdapter!!)

        // Set current item to the middle page so we can fling to both
        // directions left and right

        // Set current item to the middle page so we can fling to both
        // directions left and right
        mViewPager!!.currentItem = 0

        // Necessary or the mViewPager will only have one extra page to show
        // make this at least however many pages you can see

        // Necessary or the mViewPager will only have one extra page to show
        // make this at least however many pages you can see
        mViewPager!!.offscreenPageLimit = 3

        // Set margin for pages as a negative number, so a part of next and
        // previous pages will be showed

        // Set margin for pages as a negative number, so a part of next and
        // previous pages will be showed
        mViewPager!!.pageMargin = -280


    }

}