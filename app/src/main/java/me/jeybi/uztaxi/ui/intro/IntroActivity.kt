package me.jeybi.uztaxi.ui.intro

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import kotlinx.android.synthetic.main.activity_intro.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.auth.AuthenticationActivity
import me.jeybi.uztaxi.ui.intro.pages.IntroPagerTransforMer
import me.jeybi.uztaxi.ui.intro.pages.IntroType1
import me.jeybi.uztaxi.ui.main.MainActivity


class IntroActivity : FragmentActivity(),IntroController.view {

    val INTRO_COUNT = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        imageViewGoNext.setOnClickListener {
            imageViewGoNext.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_pop))
            if (viewPagerIntro.currentItem+1!=INTRO_COUNT)
            viewPagerIntro.currentItem = viewPagerIntro.currentItem + 1
            else{
                startActivity(Intent(this@IntroActivity,AuthenticationActivity::class.java))
                finish()
            }
        }

        viewPagerIntro.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position+1!=INTRO_COUNT) {
                    progressBarIntro.progress = (position + 1) * 100 / INTRO_COUNT
                    imageViewGoNext.setImageResource(R.drawable.btn_intro)
                }else{
                    progressBarIntro.progress= 100
                    imageViewGoNext.setImageResource(R.drawable.intro_done)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })

        viewPagerIntro.adapter = ScreenSlidePagerAdapter(this)
        viewPagerIntro.setPageTransformer(IntroPagerTransforMer())
        progressBarIntro.progress = 100/INTRO_COUNT

    }

    private inner class ScreenSlidePagerAdapter(activity: IntroActivity) : FragmentStateAdapter(
        activity
    ) {
        override fun getItemCount(): Int = INTRO_COUNT

        override fun createFragment(position: Int): Fragment{

            return  IntroType1(position)
        }
    }


    override fun onBackPressed() {
        if (viewPagerIntro.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPagerIntro.currentItem = viewPagerIntro.currentItem - 1
        }
    }



}