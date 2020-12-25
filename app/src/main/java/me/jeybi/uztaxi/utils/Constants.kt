package me.jeybi.uztaxi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap


class Constants {

    companion object{
        val APPLICATION_PREFERENCES = "application_prefs"


        val PREF_AUTHENTICATED = "intro_shown"

        val BASE_URL = "https://office.uz.taxi:443/api/"

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



    }





}