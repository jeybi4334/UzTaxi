package me.jeybi.uztaxi.ui.main.fragments

import android.os.Bundle
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_ride_finish.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseFragment

class RideFinishedFragment : BaseFragment() {
    override fun setLayoutId(): Int {
        return R.layout.bottom_sheet_ride_finish
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        imageStar1.setOnClickListener {
            imageStar1.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.star_pop))
            imageStar1.setImageResource(R.drawable.ic_star_selected)
            imageStar2.setImageResource(R.drawable.ic_star_unselected)
            imageStar3.setImageResource(R.drawable.ic_star_unselected)
            imageStar4.setImageResource(R.drawable.ic_star_unselected)
            imageStar5.setImageResource(R.drawable.ic_star_unselected)
        }
        imageStar2.setOnClickListener {
            imageStar2.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.star_pop))
            imageStar2.setImageResource(R.drawable.ic_star_selected)
            imageStar1.setImageResource(R.drawable.ic_star_selected)
            imageStar3.setImageResource(R.drawable.ic_star_unselected)
            imageStar4.setImageResource(R.drawable.ic_star_unselected)
            imageStar5.setImageResource(R.drawable.ic_star_unselected)
        }
        imageStar3.setOnClickListener {
            imageStar3.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.star_pop))
            imageStar3.setImageResource(R.drawable.ic_star_selected)
            imageStar1.setImageResource(R.drawable.ic_star_selected)
            imageStar2.setImageResource(R.drawable.ic_star_selected)
            imageStar4.setImageResource(R.drawable.ic_star_unselected)
            imageStar5.setImageResource(R.drawable.ic_star_unselected)
        }
        imageStar4.setOnClickListener {
            imageStar4.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.star_pop))
            imageStar4.setImageResource(R.drawable.ic_star_selected)
            imageStar1.setImageResource(R.drawable.ic_star_selected)
            imageStar2.setImageResource(R.drawable.ic_star_selected)
            imageStar3.setImageResource(R.drawable.ic_star_selected)
            imageStar5.setImageResource(R.drawable.ic_star_unselected)
        }
        imageStar5.setOnClickListener {
            imageStar5.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.star_pop))
            imageStar5.setImageResource(R.drawable.ic_star_selected)
            imageStar1.setImageResource(R.drawable.ic_star_selected)
            imageStar2.setImageResource(R.drawable.ic_star_selected)
            imageStar3.setImageResource(R.drawable.ic_star_selected)
            imageStar4.setImageResource(R.drawable.ic_star_selected)
        }
    }


}