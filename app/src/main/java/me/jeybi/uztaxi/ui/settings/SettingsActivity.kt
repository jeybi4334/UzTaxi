package me.jeybi.uztaxi.ui.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.utils.Constants

class SettingsActivity : BaseActivity() {
    override fun setLayoutId(): Int {
        return R.layout.activity_settings
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {

        switchSendLocation.isChecked = sharedPreferences.getBoolean(Constants.SETTINS_SHOW_LOCATION_TO_DRIVER,false)
        switchNotification.isChecked = sharedPreferences.getBoolean(Constants.SETTINGS_NOTIFICATIONS,true)
        switchComment.isChecked = sharedPreferences.getBoolean(Constants.SETTINGS_DEFAULT_COMMENT,false)
        switchWeatherAnimation.isChecked = sharedPreferences.getBoolean(Constants.SETTINGS_WEATHER_ANIMATION,true)
        switchMap3d.isChecked = sharedPreferences.getBoolean(Constants.SETTINGS_MAP_3D,false)
        switchDemoCar.isChecked = sharedPreferences.getBoolean(Constants.SETTINGS_DEMO_CAR,false)


        rvBack.setOnClickListener {
            onBackPressed()
        }


        rvLanguage.setOnClickListener {

        }

        rvSendLocationDriver.setOnClickListener {
            sharedPreferences.edit().putBoolean(Constants.SETTINS_SHOW_LOCATION_TO_DRIVER,!switchSendLocation.isChecked).apply()
            switchSendLocation.isChecked = !switchSendLocation.isChecked
        }
        rvNotifications.setOnClickListener {
            sharedPreferences.edit().putBoolean(Constants.SETTINGS_NOTIFICATIONS,!switchNotification.isChecked).apply()
            switchNotification.isChecked = !switchNotification.isChecked
        }
        rvDefaultComment.setOnClickListener {
            sharedPreferences.edit().putBoolean(Constants.SETTINGS_DEFAULT_COMMENT,!switchComment.isChecked).apply()
            switchComment.isChecked = !switchComment.isChecked
        }
        rvWeatherAnimation.setOnClickListener {
            sharedPreferences.edit().putBoolean(Constants.SETTINGS_WEATHER_ANIMATION,!switchWeatherAnimation.isChecked).apply()
            switchWeatherAnimation.isChecked = !switchWeatherAnimation.isChecked
        }
        rvMap3d.setOnClickListener {
            sharedPreferences.edit().putBoolean(Constants.SETTINGS_MAP_3D,!switchMap3d.isChecked).apply()
            switchMap3d.isChecked = !switchMap3d.isChecked
        }

        rvDemoCar.setOnClickListener {
            sharedPreferences.edit().putBoolean(Constants.SETTINGS_DEMO_CAR,!switchDemoCar.isChecked).apply()
            switchDemoCar.isChecked = !switchDemoCar.isChecked
        }




    }


}