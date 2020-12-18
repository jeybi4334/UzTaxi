package me.jeybi.uztaxi.ui.intro.pages

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import me.jeybi.uztaxi.R

class IntroPagerTransforMer : ViewPager2.PageTransformer {

    lateinit var imageView : ImageView
    lateinit var textViewTitle : TextView
    lateinit var textViewContent : TextView


    override fun transformPage(page: View, position: Float) {
        imageView = page.findViewById(R.id.imageViewIntro)
        textViewTitle = page.findViewById(R.id.textViewIntroTitle)
        textViewContent = page.findViewById(R.id.textViewIntroContent)

        val pageWidth: Int = page.getWidth()

        if (position < -1) { // [-Infinity,-1)

        } else if (position <= 1) { // [-1,1]

            imageView.translationX = (position) * (pageWidth / 4)
            textViewTitle.translationX = (position) * (pageWidth / 1.5f)
            textViewContent.translationX = (position) * (pageWidth / 0.6f)

        } else { // (1,+Infinity]


        }

    }

}