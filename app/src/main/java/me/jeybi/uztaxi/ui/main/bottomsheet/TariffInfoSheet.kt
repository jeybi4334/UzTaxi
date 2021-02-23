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
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.bottom_tariff_info.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.ServiceTariff
import me.jeybi.uztaxi.ui.main.MainActivity
import me.jeybi.uztaxi.utils.Constants
import java.lang.StringBuilder


class TariffInfoSheet(val tariff: ServiceTariff) : BottomSheetDialogFragment(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_tariff_info, container, false)

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

        try {
            val jsonObject: JsonObject = JsonParser.parseString(tariff.name).asJsonObject

            if (jsonObject.isJsonObject){
                text1.text = jsonObject.get((context as MainActivity).getCurrentLanguage().toLanguageTag()).asString
            }
        }catch (exception : IllegalStateException){
            text1.text = tariff.name
        }

        text3.text = tariff.description

    }




}