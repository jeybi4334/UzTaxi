package me.jeybi.uztaxi.ui.main.bottomsheet

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_use_bonus.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.utils.Constants
import java.lang.StringBuilder


class UserBonusSheet(val balance: Double, val tariffID: Long) : BottomSheetDialogFragment(),
    View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_use_bonus, container, false)

        return view
    }

    val USED_BONUS = StringBuilder()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.setOnShowListener { dialog -> // In a previous life I used this method to get handles to the positive and negative buttons
            // of a dialog in order to change their Typeface. Good ol' days.
            val d = dialog as BottomSheetDialog

            // This is gotten directly from the source of BottomSheetDialog
            // in the wrapInBottomSheet() method
            val bottomSheet =
                d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?

            // Right here!
            if (bottomSheet != null)
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
        }


        textViewBonus.text = Constants.getFormattedPrice(balance)


        textViewNumber1.setOnClickListener(this)
        textViewNumber2.setOnClickListener(this)
        textViewNumber3.setOnClickListener(this)
        textViewNumber4.setOnClickListener(this)
        textViewNumber5.setOnClickListener(this)
        textViewNumber6.setOnClickListener(this)
        textViewNumber7.setOnClickListener(this)
        textViewNumber8.setOnClickListener(this)
        textViewNumber9.setOnClickListener(this)
        textViewNumber11.setOnClickListener(this)
        textViewNumber12.setOnClickListener(this)

        textOrderWithoutBonus.setOnClickListener(this)
        rvOrder.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.textViewNumber1 -> {
                USED_BONUS.append("1")
            }
            R.id.textViewNumber2 -> {
                USED_BONUS.append("2")
            }
            R.id.textViewNumber3 -> {
                USED_BONUS.append("3")
            }
            R.id.textViewNumber4 -> {
                USED_BONUS.append("4")
            }
            R.id.textViewNumber5 -> {
                USED_BONUS.append("5")
            }
            R.id.textViewNumber6 -> {
                USED_BONUS.append("6")
            }
            R.id.textViewNumber7 -> {
                USED_BONUS.append("7")
            }
            R.id.textViewNumber8 -> {
                USED_BONUS.append("8")
            }
            R.id.textViewNumber9 -> {
                USED_BONUS.append("9")
            }
            R.id.textViewNumber11 -> {
                USED_BONUS.append("0")
            }
            R.id.textViewNumber12 -> {
                if (USED_BONUS.isNotEmpty()) {
                    USED_BONUS.setLength(USED_BONUS.length - 1)
                }
            }
            R.id.textOrderWithoutBonus -> {
                (activity as MainActivity).createOrder(0.0, tariffID)
                dismiss()
            }
            R.id.rvOrder -> {
                var bon = 0.0
                if (USED_BONUS.isNotEmpty()) {
                    bon = USED_BONUS.toString().toDouble()
                }
                if (bon <= balance) {
                    if (bon >= 1000) {
                        (activity as MainActivity).createOrder(bon, tariffID)
                        dismiss()
                    } else {
                        Toast.makeText(
                            activity,
                            getString(R.string.minimum_bonus_to_use),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else
                    Toast.makeText(
                        activity,
                        getString(R.string.not_enough_bonus),
                        Toast.LENGTH_SHORT
                    ).show()
            }

        }

        if (USED_BONUS.isNotEmpty())
            textEnteredBalance.text = Constants.getFormattedPrice(USED_BONUS.toString().toDouble())
        else
            textEnteredBalance.text = "0"
    }


}