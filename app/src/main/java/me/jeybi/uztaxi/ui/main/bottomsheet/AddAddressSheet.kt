package me.jeybi.uztaxi.ui.main.bottomsheet

import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_add_address.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.UzTaxiApplication
import me.jeybi.uztaxi.model.AddressType
import me.jeybi.uztaxi.model.PaymentMethod
import me.jeybi.uztaxi.ui.adapters.AddressTypeAdapter
import me.jeybi.uztaxi.ui.settings.SettingsActivity
import me.jeybi.uztaxi.utils.Constants
import java.util.*

class AddAddressSheet(val bitmap: Bitmap, val onAddressAddListener: OnAddressAddListener) :
    BottomSheetDialogFragment(),
    AddressTypeAdapter.OnAddressTypeChosenListener {

    var ALIES_TYPE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottomsheet_add_address, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        imageViewMap.setImageBitmap(bitmap)

        editTextAddressName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 0) {
                    rvAddAddress.setBackgroundResource(R.drawable.bc_button_purple_disabled)
                    rvAddAddress.isClickable = false
                } else {
                    rvAddAddress.setBackgroundResource(R.drawable.bc_button_purple)
                    rvAddAddress.isClickable = true
                }
            }

        })

        loadAddressTypes()

        rvAddAddress.setOnClickListener {
            onAddressAddListener.onAddClicked(
                editTextAddressName.text.toString(),
                ALIES_TYPE,
                editTextInstructions.text.toString(),
                this
            )
        }
    }

    override fun onAliesChosen(alies: Int) {
        ALIES_TYPE = alies
    }


    interface OnAddressAddListener {
        fun onAddClicked(addressName: String, alias: Int, instuctions: String,dialog : BottomSheetDialogFragment)
    }

    fun loadAddressTypes() {

        val addresTypes = ArrayList<AddressType>()

        addresTypes.add(AddressType(Constants.ALIES_TYPE_HOME, R.drawable.ic_address_home, "Дома"))
        addresTypes.add(
            AddressType(
                Constants.ALIES_TYPE_WORK,
                R.drawable.ic_address_work,
                "Работа"
            )
        )
        addresTypes.add(
            AddressType(
                Constants.ALIES_TYPE_SCHOOL,
                R.drawable.ic_address_school,
                "Школа"
            )
        )
        addresTypes.add(
            AddressType(
                Constants.ALIES_TYPE_SHOP,
                R.drawable.ic_address_shop,
                "Магазин"
            )
        )
        addresTypes.add(
            AddressType(
                Constants.ALIES_TYPE_PARK,
                R.drawable.ic_address_park,
                "Парк развлечений"
            )
        )
        addresTypes.add(
            AddressType(
                Constants.ALIES_TYPE_GYM,
                R.drawable.ic_address_gym,
                "Спортзал"
            )
        )

        recyclerViewAddressTypes.layoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.HORIZONTAL
        )
        recyclerViewAddressTypes.adapter = AddressTypeAdapter(addresTypes, this)

    }


}