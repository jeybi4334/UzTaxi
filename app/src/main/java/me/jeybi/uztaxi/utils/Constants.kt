package me.jeybi.uztaxi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import com.mapbox.mapboxsdk.geometry.LatLng
import java.lang.Math.abs


class Constants {

    companion object{

        val APPLICATION_PREFERENCES = "application_prefs"

        val DATABASE_NAME = "uztaxi_database"

        const val DATABASE_VERSION = 1

        const val TABLE_ADDRESS = "table_address"

        val PREF_AUTHENTICATED = "intro_shown"

        val PREF_FCM_REGISTERED = "firebase_register"

        val BASE_URL = "https://office.uz.taxi:443/api/"

        val BASE_URL_MAPBOX = "https://api.mapbox.com/directions/v5/mapbox/"

        val BASE_URL_MILLENIUM = "https://millenium-tashkent.hivelogin.ru:444/api/"

        val BASE_URL_OPENWEATHER = "https://api.openweathermap.org/data/2.5/"

        val BASE_URL_MAPZEN = "https://api.stadiamaps.com/"


        val HIVE_MILLENIUM = "2e4129784c2e080a5b19bc01c495496d"

        val HIVE_PROFILE = "2a3ae3c1da9c04320737424a4dbd2683"




        val HIVE_USER_ID = "user_id"
        val HIVE_USER_TOKEN = "user_token"
        val FIREBASE_TOKEN = "firebase_token"


        val CONF_TYPE_SMS = "sms"
        val CONF_TYPE_VOICE = "voice"

        val GENDER_MALE = 0
        val GENDER_FEMALE = 1

        val STATUS_SUCCESSFUL = 200
        val STATUS_BAD_REQUEST = 400
        val STATUS_NOT_FOUND = 404
        val STATUS_SERVER_ERROR = 500

        val MODE_SEARCH_WHERE = 1001
        val MODE_DESTINATION_PICK = 1002
        val MODE_CREATE_ORDER = 1003
        val MODE_CAR_SEARCH = 10004


        val ORDER_STATUS_RECIEVER = "me.jeybi.uztaxi.utils.ORDER_STATUS_RECEIVER"

        val ORDER_STATUS = "order_status"
        val ORDER_ID = "order_id"


        val ORDER_STATUS_CREATED = "order-created"
        val ORDER_STATUS_CHANGED = "order-changed"
        val ORDER_STATUS_DRIVER_ASSIGNED = "order-assigned"
        val ORDER_STATUS_DRIVER_DELAY = "order-driver-delay"
        val ORDER_STATUS_DRIVER_ARRIVED = "order-driver-arrived"
        val ORDER_STATUS_EXECUTING = "order-executing"
        val ORDER_STATUS_DRIVER_UNASSIGNED = "order-unassigned"
        val ORDER_STATUS_ORDER_COMPLETED = "order-completed"
        val ORDER_STATUS_BONUS_ADDED = "order-bonus-deposit"
        val ORDER_STATUS_BONUS_WITHDRAWN = "order-bonus-withdrawal"
        val ORDER_STATUS_CANCELLED = "order-cancelled"
        val ORDER_STATUS_PAID_WAITING_BEGAN = "order-payed-waiting"
        val ORDER_STATUS_CHAT_REQUEST = "chat-join-request"


        val ORDER_STATE_CREATED = 1
        val ORDER_STATE_ASSIGNED = 2
        val ORDER_STATE_DRIVER_CAME = 3
        val ORDER_STATE_EXECUTING = 4
        val ORDER_STATE_COMPLETED = 5
        val ORDER_STATE_CANCELLED = 6
        val ORDER_STATE_BOOKED = 7






        val CAR_TYPE_SPARK = "carType_1"
        val CAR_TYPE_4 = "carType_4"
        val CAR_TYPE_DELIVERY = "carType_12"
        val CAR_TYPE_PEREGON = "carType_7"

        val ALIES_TYPE_BANK = 24
        val ALIES_TYPE_METRO = 23
        val ALIES_TYPE_KAFE_RESTAURANT = 4
        val ALIES_TYPE_STADIUM = 15
        val ALIES_TYPE_PARK = 20
        val ALIES_TYPE_OTHER = 11
        val ALIES_TYPE_SCHOOL = 13
        val ALIES_TYPE_AIRPORT = 12
        val ALIES_TYPE_HOTEL = 9
        val ALIES_TYPE_TRAIN = 10
        val ALIES_TYPE_AVTO = 14
        val ALIES_TYPE_BANYA = 8
        val ALIES_TYPE_GOVERMENT = 17
        val ALIES_TYPE_ZOOPARK = 19
//        val ALIES_TYPE_OFFICE = 20
        val ALIES_TYPE_CEMETERY = 26
        val ALIES_TYPE_ART = 18
        val ALIES_TYPE_HOSPITAL = 7
        val ALIES_TYPE_BEAUTY = 5
        val ALIES_TYPE_SHOP = 3
        val ALIES_TYPE_BUSSTOP = 1
//        val ALIES_TYPE_SPORT = 5
        val ALIES_TYPE_TOURIST = 16
        val ALIES_TYPE_GARDEN = 22
        val ALIES_TYPE_CROSSROAD = 2



        //// WEATHER IDs based on openweathermap.com

        val OPENWEATHERMAP_API = "ad768b06cd7c862d6506f1b192254369"

        val WEATHER_RAIN = arrayListOf<Int>(500, 501, 520)
        /*
            500 -> ///// light rain
            501 -> moderate rain
            502 -> heavy intensity rain
            520 -> light intensity shower rain

         */
        val WEATHER_CLOUDS = arrayListOf<Int>(801, 802, 803, 804)  /////  broken clouds
        /*
            801 -> few clouds
            804 -> overcast clouds
            802 -> scattered clouds
         */
        val WEATHER_MIST = 701   //// mist
        val WEATHER_CLEAR = 800 /// Sky is clear
        val WEATHER_HAZE = 721 //// haze
        val WEATHER_FOG = 741  /// fog
        val WEATHER_SAND = 751 /// sand
        val WEATHER_DUST = 761 /// dust


        val WEATHER_UNITS = "metric"
        val WEATHER_LANG_RU = "ru"

        fun convertDpToPixel(dp: Float, context: Context): Float {
            return dp * (context.resources
                .displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
        }


         fun getBitmap(drawable: Drawable): Bitmap? {
            val canvas = Canvas()
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            canvas.setBitmap(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(canvas)
            return bitmap
        }


        fun getRotation(start: LatLng, end: LatLng): Float {
            val latDifference: Double = abs(start.latitude - end.latitude)
            val lngDifference: Double = abs(start.longitude - end.longitude)
            var rotation = -1F
            when {
                start.latitude < end.latitude && start.longitude < end.longitude -> {
                    rotation = Math.toDegrees(kotlin.math.atan(lngDifference / latDifference)).toFloat()
                }
                start.latitude >= end.latitude && start.longitude < end.longitude -> {
                    rotation = (90 - Math.toDegrees(kotlin.math.atan(lngDifference / latDifference)) + 90).toFloat()
                }
                start.latitude >= end.latitude && start.longitude >= end.longitude -> {
                    rotation = (Math.toDegrees(kotlin.math.atan(lngDifference / latDifference)) + 180).toFloat()
                }
                start.latitude < end.latitude && start.longitude >= end.longitude -> {
                    rotation =
                        (90 - Math.toDegrees(kotlin.math.atan(lngDifference / latDifference)) + 270).toFloat()
                }
            }
            return rotation
        }

        fun RotateBitmap(source: Bitmap, angle: Float): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }

        fun roundAvoid(value: Double, places: Int): Double {
            val scale = Math.pow(10.0, places.toDouble())
            return Math.round(value * scale) / scale
        }

    }



}