package me.jeybi.uztaxi.ui

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
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

//        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }
        window.statusBarColor = Color.TRANSPARENT


        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        sharedPreferences = (application as UzTaxiApplication).sharedPreferences
        setContentView(setLayoutId())
        onViewDidCreate(savedInstanceState)

    }


    @LayoutRes
    abstract fun setLayoutId() : Int

    abstract fun onViewDidCreate(savedInstanceState: Bundle?)

}