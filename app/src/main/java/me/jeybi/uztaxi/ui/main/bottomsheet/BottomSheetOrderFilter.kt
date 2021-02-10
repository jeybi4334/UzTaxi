package me.jeybi.uztaxi.ui.main.bottomsheet

import android.os.Bundle
import android.util.Log
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

class BottomSheetOrderFilter(
    val options: ArrayList<TariffOption>,
    val COMMENT : String,
    val CHOSEN_OPTIONS : ArrayList<Long>,
    var OPTIONS_VALUE : Double,
    val optionChosenListener: OptionsChosenListener
) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_filter, container, false)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextDriverComment.setText(COMMENT)
        for (option in options) {
            val itemView =
                LayoutInflater.from(context).inflate(R.layout.item_filter, linearParent, false)
            itemView.findViewById<TextView>(R.id.textViewOptionName).text = option.name
            itemView.findViewById<TextView>(R.id.textViewOptionPrice).text = "${option.value.toInt()} ${getString(R.string.currency)}"
            if (CHOSEN_OPTIONS.contains(option.id)){
                itemView.findViewById<SwitchCompat>(R.id.switchFilter).isChecked = true
            }
            itemView.findViewById<SwitchCompat>(R.id.switchFilter)
                .setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked){
                        CHOSEN_OPTIONS.add(option.id)
                        OPTIONS_VALUE += option.value
                    }
                    else {
                        CHOSEN_OPTIONS.remove(option.id)
                        OPTIONS_VALUE -= option.value
                    }
                }

            itemView.findViewById<ConstraintLayout>(R.id.parentFilter).setOnClickListener {
                itemView.findViewById<SwitchCompat>(R.id.switchFilter).isChecked =
                    !itemView.findViewById<SwitchCompat>(R.id.switchFilter).isChecked

                if (itemView.findViewById<SwitchCompat>(R.id.switchFilter).isChecked){
                    CHOSEN_OPTIONS.add(option.id)
                    OPTIONS_VALUE += option.value
                }
                else {
                    CHOSEN_OPTIONS.remove(option.id)
                    OPTIONS_VALUE -= option.value
                }
            }

            linearOptions.addView(itemView)
        }

        rvSaveOptions.setOnClickListener {
            optionChosenListener.onOptionsChosen(editTextDriverComment.text.toString(),CHOSEN_OPTIONS,OPTIONS_VALUE)
            dismiss()
        }

    }

    interface OptionsChosenListener {
        fun onOptionsChosen(comment: String, options: ArrayList<Long>, optionsValue: Double)
    }

}