package me.jeybi.uztaxi

import android.app.Application
import android.content.SharedPreferences
import io.reactivex.plugins.RxJavaPlugins
import me.jeybi.uztaxi.utils.Constants

class UzTaxiApplication : Application() {

    lateinit var sharedPreferences : SharedPreferences

    override fun onCreate() {
        super.onCreate()
        RxJavaPlugins.setErrorHandler {}
        sharedPreferences = getSharedPreferences(Constants.APPLICATION_PREFERENCES, MODE_PRIVATE)

    }

}