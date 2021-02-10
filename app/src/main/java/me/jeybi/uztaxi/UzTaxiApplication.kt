package me.jeybi.uztaxi

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.akexorcist.localizationactivity.core.LocalizationApplicationDelegate
import io.reactivex.plugins.RxJavaPlugins
import me.jeybi.uztaxi.database.UzTaxiDatabase
import me.jeybi.uztaxi.utils.Constants
import java.util.*

class UzTaxiApplication : Application() {


    lateinit var uzTaxiDatabase: UzTaxiDatabase
    lateinit var sharedPreferences : SharedPreferences

    private val localizationDelegate = LocalizationApplicationDelegate()

    override fun attachBaseContext(base: Context) {
        localizationDelegate.setDefaultLanguage(base, Locale.getDefault().displayLanguage)
        super.attachBaseContext(localizationDelegate.attachBaseContext(base))
    }

    override fun onCreate() {
        super.onCreate()
        RxJavaPlugins.setErrorHandler {}
        sharedPreferences = getSharedPreferences(Constants.APPLICATION_PREFERENCES, MODE_PRIVATE)
        uzTaxiDatabase = UzTaxiDatabase.getDatabasenIstance(this)
    }

}