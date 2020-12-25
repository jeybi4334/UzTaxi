package me.jeybi.uztaxi.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import com.mapbox.mapboxsdk.Mapbox
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.UzTaxiApplication
import me.jeybi.uztaxi.utils.Constants


abstract class BaseActivity : FragmentActivity() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        sharedPreferences = (application as UzTaxiApplication).sharedPreferences
        setContentView(setLayoutId())
        onViewDidCreate(savedInstanceState)

    }


    @LayoutRes
    abstract fun setLayoutId() : Int

    abstract fun onViewDidCreate(savedInstanceState: Bundle?)

}