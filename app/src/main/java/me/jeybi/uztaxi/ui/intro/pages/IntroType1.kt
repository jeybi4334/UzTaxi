package me.jeybi.uztaxi.ui.intro.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.intro_1.*
import me.jeybi.uztaxi.R

class IntroType1(val position : Int) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.intro_1,container,false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(position){
            1->{
                imageViewIntro.setImageResource(R.drawable.page_2)
                textViewIntroTitle.text = getString(R.string.intro_2_title)
                textViewIntroContent.text = getString(R.string.intro_2_content)
            }
            2->{
                imageViewIntro.setImageResource(R.drawable.page_3)
                textViewIntroTitle.text = getString(R.string.intro_3_title)
                textViewIntroContent.text = getString(R.string.intro_3_content)
            }
            3->{
                imageViewIntro.setImageResource(R.drawable.page_4)
                textViewIntroTitle.text = getString(R.string.intro_4_title)
                textViewIntroContent.text = getString(R.string.intro_4_content)
            }
        }
    }

}