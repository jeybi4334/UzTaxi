package me.jeybi.uztaxi.ui.address

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.activity_add_address.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.AddressType
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.ui.adapters.AddressTypeAdapter
import me.jeybi.uztaxi.utils.Constants
import java.io.FileInputStream


class AddAddressActivity : BaseActivity() {



    override fun setLayoutId(): Int {
        return R.layout.activity_add_address
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        rvBack.setOnClickListener {
                onBackPressed()
        }
        var bmp: Bitmap? = null
        val filename = intent.getStringExtra("image")
        try {
            val `is`: FileInputStream = openFileInput(filename)
            bmp = BitmapFactory.decodeStream(`is`)
            imageViewMap.setImageBitmap(bmp)
            `is`.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }


        loadAddressTypes()
    }

    fun loadAddressTypes(){

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

    }



}