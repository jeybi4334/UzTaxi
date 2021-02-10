package me.jeybi.uztaxi.ui.main.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_language.*
import kotlinx.android.synthetic.main.bottomsheet_payment.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.PaymentMethod
import me.jeybi.uztaxi.ui.settings.SettingsActivity
import java.util.*

class LanguageSheet(val langTag : String) : BottomSheetDialogFragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_language,container,false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(langTag){
            "uz"->{
                checkOzbek.visibility = View.VISIBLE
            }
            "os"->{
                checkUzbek.visibility = View.VISIBLE
            }
            "kl"->{
                checkKarakalpak.visibility = View.VISIBLE
            }
            "ru"->{
                checkRussian.visibility = View.VISIBLE
            }
            "en"->{
                checkEnglish.visibility = View.VISIBLE
            }
        }

        linearOzbek.setOnClickListener {
            (activity as SettingsActivity).setLanguage(Locale.forLanguageTag("uz"))
            dismiss()
        }
        linearUzbek.setOnClickListener {
            (activity as SettingsActivity).setLanguage(Locale.forLanguageTag("os"))
            dismiss()
        }
        linearKarakalpak.setOnClickListener {
            (activity as SettingsActivity).setLanguage(Locale.forLanguageTag("kl"))
            dismiss()
        }
        linearRussian.setOnClickListener {
            (activity as SettingsActivity).setLanguage(Locale.forLanguageTag("ru"))
            dismiss()
        }
        linearEnglish.setOnClickListener {
            (activity as SettingsActivity).setLanguage(Locale.forLanguageTag("en"))
            dismiss()
        }




    }

}