package me.jeybi.uztaxi.ui

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import me.jeybi.uztaxi.UzTaxiApplication

abstract class BaseActivity : FragmentActivity() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = (application as UzTaxiApplication).sharedPreferences
        setContentView(setLayoutId())
        onViewDidCreate(savedInstanceState)

    }


    @LayoutRes
    abstract fun setLayoutId() : Int

    abstract fun onViewDidCreate(savedInstanceState: Bundle?)


}