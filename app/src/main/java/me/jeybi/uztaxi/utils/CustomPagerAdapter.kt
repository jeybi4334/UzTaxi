package me.jeybi.uztaxi.utils

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.database.CreditCardEntity


class CustomPagerAdapter(
    val fragmentManager: FragmentManager?,
    val cardData: ArrayList<CreditCardEntity>
) : FragmentPagerAdapter(fragmentManager!!,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT ), ViewPager.PageTransformer {

    var PAGES = 4
    var FIRST_PAGE = 0
    private var mScale = 0f

    val fragmentsList = ArrayList<CustomFragment>()

    fun clearAll(){
        for (frag in fragmentsList){
            fragmentManager?.beginTransaction()?.remove(frag)?.commit()
        }
        fragmentsList.clear()
    }

    override fun getItem(position: Int): Fragment {
        // make the first mViewPager bigger than others
        mScale = if (position == FIRST_PAGE) BIG_SCALE else SMALL_SCALE

        var newFragment : CustomFragment? = null



        if (cardData.size==0){
            newFragment = CustomFragment(position, mScale, Constants.CARD_TYPE_STYLE, 1000, null)
        }else{
            val creditCard = cardData[position]

            newFragment = when (creditCard.cardDesign) {
                1001 -> {
                    CustomFragment(position, mScale, Constants.CARD_TYPE_CREATE, 1001, null)
                }
                1003 -> {
                    CustomFragment(position, mScale, Constants.CARD_TYPE_WALLET, 1003, null)
                }
                else -> {
                    CustomFragment(
                        position,
                        mScale,
                        Constants.CARD_TYPE_PLASTIC,
                        creditCard.cardDesign,
                        creditCard
                    )
                }
            }
        }

        fragmentsList.add(position, newFragment)
        return newFragment
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
    }

    fun getCurentFragment(position: Int) : CustomFragment{
        return fragmentsList[position]
    }


    override fun getCount(): Int {
        return if (cardData.size==0) PAGES else cardData.size
    }

    override fun transformPage(page: View, position: Float) {
        val myLinearLayout = page.findViewById<View>(R.id.item_root) as CustomConstraintLayout
        var scale = BIG_SCALE
        scale = if (position > 0) {
            scale - position * DIFF_SCALE
        } else {
            scale + position * DIFF_SCALE
        }
        if (scale < 0) scale = 0f
        myLinearLayout.setScaleBoth(scale)
    }

    companion object {
        const val BIG_SCALE = 1.0f
        const val SMALL_SCALE = 0.7f
        const val DIFF_SCALE = BIG_SCALE - SMALL_SCALE
    }
}