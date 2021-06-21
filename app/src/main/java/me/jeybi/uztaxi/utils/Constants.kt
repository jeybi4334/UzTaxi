package me.jeybi.uztaxi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.util.DisplayMetrics
import android.view.View
import com.mapbox.mapboxsdk.geometry.LatLng
import me.jeybi.uztaxi.R
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class Constants {

    companion object{

        val APPLICATION_PREFERENCES = "application_prefs"

        val DATABASE_NAME = "uztaxi_db"

        const val DATABASE_VERSION = 1

        const val TABLE_ADDRESS = "table_address"
        const val TABLE_CARDS = "table_cards"

        const val PAYMENT_TYPE = "payment_type"

        const val PAYMENT_TYPE_CASH = "cash"
        const val PAYMENT_TYPE_CONTRACTOR = "contractor"
        const val PAYMENT_TYPE_NAME = "payment_type_name"
        const val PAYMENT_TYPE_ID = "payment_type_id"

        const val STILL_SAME_SERVICE = "same"

        const val CALL_CENTER_NUMBER = "1191"

        val PREF_AUTHENTICATED = "intro_shown"

        val PREF_LANGUAGE = "app_language"

        val PREF_FCM_REGISTERED = "firebase_register"

        val BASE_URL = "https://office.uz.taxi:443/api/"

        val BASE_URL_MAPBOX = "https://api.mapbox.com/directions/v5/mapbox/"

        val BASE_URL_MILLENIUM = "https://millenium-tashkent.hivelogin.ru:444/api/"

        val BASE_URL_OPENWEATHER = "https://api.openweathermap.org/data/2.5/"

//        val BASE_URL_MAPZEN = "https://api.stadiamaps.com/"
        val BASE_URL_UZ_TAXI_NAVIGATION = "http://map.uz.taxi:8989/"

//        val BASE_URL_NOMINATIM = "https://nominatim.openstreetmap.org/"
        val BASE_URL_NOMINATIM = "http://map.uz.taxi:7070/"

//        val BASE_URL_GEOCODE = "https://api.geocode.earth/v1/"
        val BASE_URL_GEOCODE = "http://map.uz.taxi:4000/v1/"

        val HIVE_MILLENIUM = "2e4129784c2e080a5b19bc01c495496d"

        val BASE_URL_PAYME = "https://checkout.paycom.uz/"

        val HIVE_PROFILE = "2a3ae3c1da9c04320737424a4dbd2683"

        val GEOCODE_TOKEN = "ge-8f137223ed5b405d"
        val GEOCODE_COUNTRY = "UZB"
        val GEOCODE_LIMIT = 15
        val GEOCODE_SOURCE = "osm"
        val GEOCODE_RADIUS = 40

        val LAST_KNOWN_LATITUDE = "last_lat"
        val LAST_KNOWN_LONGITUDE = "last_lon"


        val NOMINATIM_ZOOM_LVL = 18
        val NOMINATIM_ZOOM_FORMAT = "jsonv2"

        val NOMINATIM_LANGUAGE = "nominatim_language"

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
        val MODE_DESTINATION_PICK_STOP = 10021
        val MODE_DESTINATION_PICK_ADDRESS = 10022
        val MODE_DESTINATION_PICK_EDIT = 10023
        val MODE_DESTINATION_PICK_START = 10024
        val MODE_CREATE_ORDER = 1003
        val MODE_CAR_SEARCH = 10004
        val MODE_CAR_FOUND = 10005
        val MODE_DRIVER_CAME = 10006
        val MODE_RIDE_STARTED = 10007

        val MAIN_CREDIT_CARD = "main_credit_card"

        val ORDER_STATUS_RECIEVER = "me.jeybi.uztaxi.utils.ORDER_STATUS_RECEIVER"

        val ORDER_STATUS = "order_status"
        val ORDER_ID = "order_id"

        val DESTINATION_PICK_ORDEDR = 1
        val DESTINATION_PICK_STOP = 2
        val DESTINATION_PICK_ADDRESS = 3
        val DESTINATION_PICK_EDIT = 4
        val DESTINATION_PICK_START = 5

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

        val ORDER_STATE_NOT_CREATED = 0
        val ORDER_STATE_CREATED = 1
        val ORDER_STATE_ASSIGNED = 2
        val ORDER_STATE_DRIVER_CAME = 3
        val ORDER_STATE_EXECUTING = 4
        val ORDER_STATE_COMPLETED = 5
        val ORDER_STATE_CANCELLED = 6
        val ORDER_STATE_BOOKED = 7

        val ASSIGNE_CALL_DIRECT = "direct"
        val ASSIGNE_CALL_SERVER = "via server"
        val ASSIGNE_CALL_NOT = "no"

        val COST_TYPE_TOTAL = "total"
        val COST_TYPE_APPROXIMATE = "approximate"
        val COST_TYPE_MINIMAL = "minimum"

        val CAR_ALIAS_LACETTI = "Lacetti"
        val CAR_ALIAS_NEXIA = "Nexia"
        val CAR_ALIAS_MATIZ = "Matiz"
        val CAR_ALIAS_SPARK = "Spark"
        val CAR_ALIAS_GENTRA = "Gentra"
        val CAR_ALIAS_COBALT = "Cobalt"
        val CAR_ALIAS_EPICA = "Epica"
        val CAR_ALIAS_RAVON = "Ravon"
        val CAR_ALIAS_GRANTA = "Granta"
        val CAR_ALIAS_PRIORA = "Priora"
        val CAR_ALIAS_VESTA = "Vesta"
        val CAR_ALIAS_XRAY = "X-Ray"
        val CAR_ALIAS_CAPTIVA = "Captiva"
        val CAR_ALIAS_TOYOTA = "Toyota"
        val CAR_ALIAS_ISUZU = "Isuzu"
        val CAR_ALIAS_MERCEDES = "Mercedes"
        val CAR_ALIAS_MALIBU = "Malibu"
        val CAR_ALIAS_CHRYSLER = "Chrysler"
        val CAR_ALIAS_LINCOLN = "Lincoln"
        val CAR_ALIAS_LAND_ROVER = "Land Rover"
        val CAR_ALIAS_CADILLAC = "Cadillac"
        val CAR_ALIAS_ROLLSROYCE = "Rolls-Royce"

        val CAR_MODEL_MALIBU1 = "Malibu (E)"
        val CAR_MODEL_MALIBU2 = "Malibu Maxx"

        val CAR_MODEL_NEXIA1_1 = "Nexia (KLETN)"
        val CAR_MODEL_NEXIA1_2 = "Nexia Combi (KLETN)"
        val CAR_MODEL_NEXIA2_1 = "Nexia II (DOHC)"
        val CAR_MODEL_NEXIA2_2 = "Nexia II (SOHC)"

        val CAR_MODEL_NEXIA3 = "NEXIA R3"
        val CAR_MODEL_GENTRA = "GENTRA"
        val CAR_MODEL_SPARK = "R2"
        val CAR_MODEL_COBALT = "R4"
        val CAR_MODEL_MATIZ = "MATIZ"

        val CAR_MODEL_W221 = "S-klasse (W221)"
        val CAR_MODEL_W220 = "S-klasse (W220)"
        val CAR_MODEL_GELIK = "/8 (W114)"
        val CAR_MODEL_VITO = "Vito"

        val CAR_TYPE_1 = "carType_1"
        val CAR_TYPE_2 = "carType_2"
        val CAR_TYPE_3 = "carType_3"
        val CAR_TYPE_4 = "carType_4"
        val CAR_TYPE_5 = "carType_5"
        val CAR_TYPE_6 = "carType_6"
        val CAR_TYPE_7 = "carType_7"
        val CAR_TYPE_8 = "carType_8"
        val CAR_TYPE_9 = "carType_9"
        val CAR_TYPE_10 = "carType_10"
        val CAR_TYPE_11 = "carType_11"
        val CAR_TYPE_12 = "carType_12"

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

        val ALIES_TYPE_HOME = 101
        val ALIES_TYPE_WORK = 102
        val ALIES_TYPE_GYM = 103
        val ALIES_TYPE_MOSQUE = 104

        val CARD_TYPE_PLASTIC = 110010020
        val CARD_TYPE_WALLET = 110010021
        val CARD_TYPE_CREATE = 110010022
        val CARD_TYPE_STYLE = 110010023



        //// SETTINGS_CONSTANTS

        val SETTINS_SHOW_LOCATION_TO_DRIVER = "settings_show_location"
        val SETTINGS_NOTIFICATIONS = "settings_notifications"
        val SETTINGS_DEFAULT_COMMENT = "settings_default_comment"
        val SETTINGS_WEATHER_ANIMATION = "settings_weather_animation"
        val SETTINGS_MAP_3D = "settings_map_3d"
        val SETTINGS_DEMO_CAR = "settings_demo_car"

        //// DEFAULT VALUES
        val DEFAULT_TILT_MAP = 30.0



        //// WEATHER IDs based on openweathermap.com
        val OPENWEATHERMAP_API = "e133d7db9b14e753bf10f14791e4c270"

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

        fun getCurrentTime() : String{
            val dateFormat : DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            val date: Date = Date();
            return dateFormat.format(date)
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

        fun getBitmapFromView(view: View): Bitmap {
            val bitmap =
                Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }


        fun getRotation(start: LatLng, end: LatLng): Float {
            val latDifference: Double = kotlin.math.abs(start.latitude - end.latitude)
            val lngDifference: Double = kotlin.math.abs(start.longitude - end.longitude)
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


        fun distanceTo(lat1 : Double,lon1 : Double,lat2 : Double,lon2 : Double) : Float{
            val point1 = Location("point1")
            point1.latitude = lat1
            point1.longitude = lon1
            val point2 = Location("point2")
            point2.latitude = lat2
            point2.longitude = lon2

                return point1.distanceTo(point2)
        }

        fun roundAvoid(value: Double, places: Int): Double {
            val scale = Math.pow(10.0, places.toDouble())
            return Math.round(value * scale) / scale
        }

        fun getCardBankIcon(cardNumber: String) : Int{
            return when {
                cardNumber.replace(" ","").startsWith("860002") -> R.drawable.ic_bank_nbu
                cardNumber.replace(" ","").startsWith("860003") -> R.drawable.ic_bank_sqb
                cardNumber.replace(" ","").startsWith("860004") -> R.drawable.ic_bank_agro
                cardNumber.replace(" ","").startsWith("860005") -> R.drawable.ic_bank_mikrokredit
                cardNumber.replace(" ","").startsWith("860006") -> R.drawable.ic_bank_xalq
                cardNumber.replace(" ","").startsWith("860008") -> R.drawable.ic_bank_savdogar
                cardNumber.replace(" ","").startsWith("860009") -> R.drawable.ic_bank_qqb
                cardNumber.replace(" ","").startsWith("860011") -> R.drawable.ic_bank_turon
                cardNumber.replace(" ","").startsWith("860012") -> R.drawable.ic_bank_hamkor
                cardNumber.replace(" ","").startsWith("860013") -> R.drawable.ic_bank_asaka
                cardNumber.replace(" ","").startsWith("860014") -> R.drawable.ic_bank_ipak
                cardNumber.replace(" ","").startsWith("860020") -> R.drawable.ic_bank_ziraat
                cardNumber.replace(" ","").startsWith("860030") -> R.drawable.ic_bank_trast
                cardNumber.replace(" ","").startsWith("860031") -> R.drawable.ic_bank_aloqa
                cardNumber.replace(" ","").startsWith("860033") -> R.drawable.ic_bank_ipoteka
                cardNumber.replace(" ","").startsWith("860034") -> R.drawable.ic_bank_kdb
                cardNumber.replace(" ","").startsWith("860038") -> R.drawable.ic_bank_turkiston
                cardNumber.replace(" ","").startsWith("860043") -> R.drawable.ic_bank_saderat
                cardNumber.replace(" ","").startsWith("860048") -> R.drawable.ic_bank_universal
                cardNumber.replace(" ","").startsWith("860049") -> R.drawable.ic_bank_kapital
                cardNumber.replace(" ","").startsWith("860049") -> R.drawable.ic_bank_kapital
                cardNumber.replace(" ","").startsWith("860050") -> R.drawable.ic_bank_ravnaq
                cardNumber.replace(" ","").startsWith("860051") -> R.drawable.ic_bank_davr
                cardNumber.replace(" ","").startsWith("860053") -> R.drawable.ic_bank_infin
                cardNumber.replace(" ","").startsWith("860055") -> R.drawable.ic_bank_asia
                cardNumber.replace(" ","").startsWith("860056") -> R.drawable.ic_bank_hi_tech
                cardNumber.replace(" ","").startsWith("860057") -> R.drawable.ic_bank_ofb
                cardNumber.replace(" ","").startsWith("860058") -> R.drawable.ic_bank_madad
                cardNumber.replace(" ","").startsWith("860059") -> R.drawable.ic_bank_agroexpo
                cardNumber.replace(" ","").startsWith("860060") -> R.drawable.ic_bank_poytaxt
                cardNumber.replace(" ","").startsWith("860061") -> R.drawable.ic_bank_tenge
                cardNumber.replace(" ","").startsWith("860062") -> R.drawable.ic_bank_tbc

                else -> R.drawable.ic_logo_white
            }
        }

        fun getBankProviderIcon(cardNumber: String) : Int{
            return when{
                cardNumber.startsWith("8600") -> R.drawable.ic_uzcard
                cardNumber.startsWith("9860") -> R.drawable.ic_bank_humo
                else-> R.drawable.ic_uzcard
            }
        }

        fun getHumoBankIcon(cardNumber: String) : Int{
            return when {
                cardNumber.replace(" ","").startsWith("986001") -> R.drawable.ic_bank_ipoteka
                cardNumber.replace(" ","").startsWith("986002") -> R.drawable.ic_bank_sqb
                cardNumber.replace(" ","").startsWith("986003") -> R.drawable.ic_bank_agro
                cardNumber.replace(" ","").startsWith("986004") -> R.drawable.ic_bank_asaka
                cardNumber.replace(" ","").startsWith("986006") -> R.drawable.ic_bank_qqb
                cardNumber.replace(" ","").startsWith("986008") -> R.drawable.ic_bank_xalq
                cardNumber.replace(" ","").startsWith("986009") -> R.drawable.ic_bank_asia
                cardNumber.replace(" ","").startsWith("986010") -> R.drawable.ic_bank_kapital
                cardNumber.replace(" ","").startsWith("986012") -> R.drawable.ic_bank_nbu
                cardNumber.replace(" ","").startsWith("986013") -> R.drawable.ic_bank_mikrokredit
                cardNumber.replace(" ","").startsWith("986014") -> R.drawable.ic_bank_savdogar
                cardNumber.replace(" ","").startsWith("986015") -> R.drawable.ic_bank_turon
                cardNumber.replace(" ","").startsWith("986016") -> R.drawable.ic_bank_hamkor
                cardNumber.replace(" ","").startsWith("986017") -> R.drawable.ic_bank_ipak
                cardNumber.replace(" ","").startsWith("986018") -> R.drawable.ic_bank_trast
                cardNumber.replace(" ","").startsWith("986019") -> R.drawable.ic_bank_aloqa
                cardNumber.replace(" ","").startsWith("986020") -> R.drawable.ic_bank_kdb
                cardNumber.replace(" ","").startsWith("986021") -> R.drawable.ic_bank_turkiston
                cardNumber.replace(" ","").startsWith("986023") -> R.drawable.ic_bank_universal
                cardNumber.replace(" ","").startsWith("986024") -> R.drawable.ic_bank_ravnaq
                cardNumber.replace(" ","").startsWith("986025") -> R.drawable.ic_bank_davr
                cardNumber.replace(" ","").startsWith("986026") -> R.drawable.ic_bank_infin
                cardNumber.replace(" ","").startsWith("986027") -> R.drawable.ic_bank_ofb
                cardNumber.replace(" ","").startsWith("986028") -> R.drawable.ic_bank_hi_tech
                cardNumber.replace(" ","").startsWith("986029") -> R.drawable.ic_bank_ziraat
                cardNumber.replace(" ","").startsWith("986030") -> R.drawable.ic_bank_saderat
                cardNumber.replace(" ","").startsWith("986031") -> R.drawable.ic_bank_agroexpo
                cardNumber.replace(" ","").startsWith("986032") -> R.drawable.ic_bank_madad
                cardNumber.replace(" ","").startsWith("986033") -> R.drawable.ic_bank_poytaxt
                cardNumber.replace(" ","").startsWith("986034") -> R.drawable.ic_bank_tenge
                cardNumber.replace(" ","").startsWith("986035") -> R.drawable.ic_bank_tbc
                else -> R.drawable.ic_logo_white
            }
        }

        fun maskCardNumber(cardNumber: String, mask: String): String {
            
            // format the number
            var index = 0
            val maskedNumber = StringBuilder()
            for (i in 0 until mask.length) {
                val c = mask[i]
                when (c) {
                    '#' -> {
                        maskedNumber.append(cardNumber[index])
                        index++
                    }
                    '•' -> {
                        maskedNumber.append(" $c ")
                        index++
                    }
                    ' '->{
                        maskedNumber.append(" ")
                        index++
                    }
                    else -> {
                        maskedNumber.append(c)
                    }
                }
            }

            // return the masked number
            return maskedNumber.toString()
        }

        fun getFormattedPrice(price: Double) : String{
            val decimalFormat = DecimalFormat("###,###")
            return decimalFormat.format(price).replace(",", ".")
        }

        fun getRandomIcon() : String{
            val random = Random()
           return getCarIcon(random.nextInt(350).toFloat())
        }

        fun getCarIcon(rotation: Float) : String{
            val rotateFormatter = DecimalFormat("#.#")

           return when (abs(rotateFormatter.format(rotation).replace(",", ".").toFloat())) {
               in 0f..2.4f -> "fleet-0"
               in 2.5f..7.4f -> "fleet-5"
               in 7.5f..12.4f -> "fleet-10"
               in 12.5f..17.4f -> "fleet-15"
               in 17.5f..22.4f -> "fleet-20"
               in 22.5f..27.4f -> "fleet-25"
               in 27.5f..32.4f -> "fleet-30"
               in 32.5f..37.4f -> "fleet-35"
               in 37.5f..42.4f -> "fleet-40"
               in 42.5f..47.4f -> "fleet-45"
               in 47.5f..52.4f -> "fleet-50"
               in 52.5f..57.4f -> "fleet-55"
               in 57.5f..62.4f -> "fleet-60"
               in 62.5f..67.4f -> "fleet-65"
               in 67.5f..72.4f -> "fleet-70"
               in 72.5f..77.4f -> "fleet-75"
               in 77.5f..82.4f -> "fleet-80"
               in 82.5f..87.4f -> "fleet-85"
               in 87.5f..92.4f -> "fleet-90"
               in 92.5f..97.4f -> "fleet-95"
               in 97.5f..102.4f -> "fleet-100"
               in 102.5f..107.4f -> "fleet-105"
               in 107.5f..112.4f -> "fleet-110"
               in 112.5f..117.4f -> "fleet-115"
               in 117.5f..122.4f -> "fleet-120"
               in 122.5f..127.4f -> "fleet-125"
               in 127.5f..132.4f -> "fleet-130"
               in 132.5f..137.4f -> "fleet-135"
               in 137.5f..142.4f -> "fleet-140"
               in 142.5f..147.4f -> "fleet-145"
               in 147.5f..152.4f -> "fleet-150"
               in 152.5f..157.4f -> "fleet-155"
               in 157.5f..162.4f -> "fleet-160"
               in 162.5f..167.4f -> "fleet-165"
               in 167.5f..172.4f -> "fleet-170"
               in 172.5f..177.4f -> "fleet-175"
               in 177.5f..182.4f -> "fleet-180"
               in 182.5f..187.4f -> "fleet-185"
               in 187.5f..192.4f -> "fleet-190"
               in 192.5f..197.4f -> "fleet-195"
               in 197.5f..202.4f -> "fleet-200"
               in 202.5f..207.4f -> "fleet-205"
               in 207.5f..212.4f -> "fleet-210"
               in 212.5f..217.4f -> "fleet-215"
               in 217.5f..222.4f -> "fleet-220"
               in 222.5f..227.4f -> "fleet-225"
               in 227.5f..232.4f -> "fleet-230"
               in 232.5f..237.4f -> "fleet-235"
               in 237.5f..242.4f -> "fleet-240"
               in 242.5f..247.4f -> "fleet-245"
               in 247.5f..252.4f -> "fleet-250"
               in 252.5f..257.4f -> "fleet-255"
               in 257.5f..262.4f -> "fleet-260"
               in 262.5f..267.4f -> "fleet-265"
               in 267.5f..272.4f -> "fleet-270"
               in 272.5f..277.4f -> "fleet-275"
               in 277.5f..282.4f -> "fleet-280"
               in 282.5f..287.4f -> "fleet-285"
               in 287.5f..292.4f -> "fleet-290"
               in 292.5f..297.4f -> "fleet-295"
               in 297.5f..302.4f -> "fleet-300"
               in 302.5f..307.4f -> "fleet-305"
               in 307.5f..312.4f -> "fleet-310"
               in 312.5f..317.4f -> "fleet-315"
               in 317.5f..322.4f -> "fleet-320"
               in 322.5f..327.4f -> "fleet-325"
               in 327.5f..332.4f -> "fleet-330"
               in 332.5f..337.4f -> "fleet-335"
               in 337.5f..342.4f -> "fleet-340"
               in 342.5f..347.4f -> "fleet-345"
               in 347.5f..352.4f -> "fleet-350"
               in 352.5f..357.4f -> "fleet-355"
               in 357.5f..360.0f -> "fleet-0"
                else -> ""
            }
        }

    }



}