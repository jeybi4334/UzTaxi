package me.jeybi.uztaxi.utils

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import me.jeybi.uztaxi.R

class CustomFragment(val position : Int,val scale : Float) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (container == null) {
            return null
        }
        val linearLayout = inflater.inflate(R.layout.item_card, container, false) as LinearLayout



        when(position) {
            0->linearLayout.findViewById<View>(R.id.layoutCard).setBackgroundResource(R.drawable.ic_card_type_1)
            1->linearLayout.findViewById<View>(R.id.layoutCard).setBackgroundResource(R.drawable.ic_card_type_5)
            2->linearLayout.findViewById<View>(R.id.layoutCard).setBackgroundResource(R.drawable.ic_card_type_2)
            3->linearLayout.findViewById<View>(R.id.layoutCard).setBackgroundResource(R.drawable.ic_card_type_3)
            4->linearLayout.findViewById<View>(R.id.layoutCard).setBackgroundResource(R.drawable.ic_card_type_4)

        }


        val root = linearLayout.findViewById<View>(R.id.item_root) as CustomLinearLayout
        root.setScaleBoth(scale)
        return linearLayout
    }


}