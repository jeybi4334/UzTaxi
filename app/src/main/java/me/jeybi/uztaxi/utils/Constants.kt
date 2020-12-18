package me.jeybi.uztaxi.utils

import android.content.Context
import android.util.DisplayMetrics




class Constants {

    companion object{
        val APPLICATION_PREFERENCES = "application_prefs"


        val PREF_AUTHENTICATED = "intro_shown"

        fun convertDpToPixel(dp: Float, context: Context): Float {
            return dp * (context.resources
                .displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }


    }

}