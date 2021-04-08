package me.jeybi.uztaxi

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
        localizationDelegate.setDefaultLanguage(base, Locale.forLanguageTag(getLang()))
        super.attachBaseContext(localizationDelegate.attachBaseContext(base))
    }

    private fun getLang(): String {
        return if (Locale.getDefault().language == "русский")
            "ru"
        else
            Locale.getDefault().language
    }

    override fun onCreate() {
        super.onCreate()
        RxJavaPlugins.setErrorHandler {}
        sharedPreferences = getSharedPreferences(Constants.APPLICATION_PREFERENCES, MODE_PRIVATE)
        uzTaxiDatabase = UzTaxiDatabase.getDatabasenIstance(applicationContext)
    }

}