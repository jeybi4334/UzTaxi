package me.jeybi.uztaxi.utils

import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.Marker


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


    private fun bearingBetweenLocations(latLng1: LngLat, latLng2: LngLat): Double {
        val PI = 3.14159
        val lat1: Double = latLng1.latitude * PI / 180
        val long1: Double = latLng1.longitude * PI / 180
        val lat2: Double = latLng2.latitude * PI / 180
        val long2: Double = latLng2.longitude * PI / 180
        val dLon = long2 - long1
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - (Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon))
        var brng = Math.atan2(y, x)
        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360
        return brng
    }

//    private fun rotateMarker(marker: Marker, toRotation: Float) {
//        if (!isMarkerRotating) {
//            val handler = Handler()
//            val start = SystemClock.uptimeMillis()
//            val startRotation: Float = marker.getRotation()
//            val duration: Long = 1000
//            val interpolator: Interpolator = LinearInterpolator()
//            handler.post(object : Runnable {
//                override fun run() {
//                    isMarkerRotating = true
//                    val elapsed = SystemClock.uptimeMillis() - start
//                    val t: Float = interpolator.getInterpolation(elapsed.toFloat() / duration)
//                    val rot = t * toRotation + (1 - t) * startRotation
//                    marker.setRotation(if (-rot > 180) rot / 2 else rot)
//                    if (t < 1.0) {
//                        // Post again 16ms later.
//                        handler.postDelayed(this, 16)
//                    } else {
//                        isMarkerRotating = false
//                    }
//                }
//            })
//        }
//    }

}