package me.jeybi.uztaxi.ui.main.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_filter.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.TariffOption

class BottomSheetOrderFilter(val options : ArrayList<TariffOption>,val optionChosenListener: OptionChosenListener) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_filter,container,false)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        for (option in options){
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_filter,linearParent,false)
            itemView.findViewById<TextView>(R.id.textViewOptionName).text = option.name
            itemView.findViewById<TextView>(R.id.textViewOptionPrice).text = "${option.value.toInt()} сум"
            itemView.findViewById<SwitchCompat>(R.id.switchFilter)
                .setOnCheckedChangeListener { buttonView, isChecked ->
                    optionChosenListener.onOptionChosen(option.id,option.value)
                }

                itemView.findViewById<ConstraintLayout>(R.id.parentFilter).setOnClickListener {
                    itemView.findViewById<SwitchCompat>(R.id.switchFilter).isChecked = !itemView.findViewById<SwitchCompat>(R.id.switchFilter).isChecked
                    optionChosenListener.onOptionChosen(option.id,option.value)
                }

            linearOptions.addView(itemView)
        }


    }

    interface OptionChosenListener{
        fun onOptionChosen(optionID : Long,optionValue : Double)
    }

}