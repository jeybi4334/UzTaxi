package me.jeybi.uztaxi.utils

import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator


class Constants {

    companion object{
        val APPLICATION_PREFERENCES = "application_prefs"


        val PREF_AUTHENTICATED = "intro_shown"

        fun convertDpToPixel(dp: Float, context: Context): Float {
            return dp * (context.resources
                .displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }

        var MARKER_CAR_0 = "{ style: 'points', color: 'white', size: [9px, 18px], order: 1, collide: false }"

        var MARKER_CAR_1 = "{ style: 'points', color: 'white', size: [16px, 36px], order: 1, collide: false }"

        var MARKER_CAR_2 = "{ style: 'points', color: 'white', size: [24px, 48px], order: 1, collide: false }"

        var MARKER_CAR_3 = "{ style: 'points', color: 'white', size: [28px, 56px], order: 1, collide: false }"

        var MARKER_CAR_4 = "{ style: 'points', color: 'white', size: [43px, 86px], order: 1, collide: false }"

        var MARKER_CAR_5 = "{ style: 'points', color: 'white', size: [51px, 102px], order: 1, collide: false }"


        var MARKER_POINTS = "{ style: 'points', color: 'white', size: [50px, 50px], order: 2000, collide: false }"

        var MARKER_LINE = "{ style: 'lines', color: '#06a6d4', width: 5px, order: 2000 }"
        var MARKER_POLYGON = "{ style: 'polygons', color: '#06a6d4', width: 5px, order: 2000 }"


    }



}