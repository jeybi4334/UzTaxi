package me.jeybi.uztaxi.ui.main

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.gms.common.util.Base64Utils
import com.mapbox.geojson.Point
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.*
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.NaiveHmacSigner
import me.jeybi.uztaxi.utils.PolyLineUtils
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets.UTF_8
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainPresenter(val view: MainActivity) : MainController.presenter {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1


    override fun checkIfAuthenticated() {
        val token = view.sharedPreferences.getString(Constants.HIVE_USER_TOKEN, "")
        if (token == "")
            view.onUserNotAuthenticated()
        else
            view.onUserApproved()
    }

    override fun requestPermissions() {

        requestPermissionsIfNecessary(
            arrayOf(
                // if you need to show the current location, uncomment the line below
                Manifest.permission.ACCESS_FINE_LOCATION,
                // WRITE_EXTERNAL_STORAGE is required in order to show the map
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                ACCESS_COARSE_LOCATION
            )
        )

    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(view, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                view,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }


    override fun checkGPS() {


        val provider = Settings.Secure.getString(
            view.contentResolver,
            Settings.Secure.LOCATION_PROVIDERS_ALLOWED
        )
        if (!provider.contains("gps")) {
            view.buildAlertMessageNoGps()
        } else {
            view.onHasGPS()
        }


    }

    override fun registerFCMToken(token: String): Disposable {
        var registerTokenDisposable: Disposable? = null

        registerTokenDisposable = RetrofitHelper.apiService(Constants.BASE_URL)
            .registerFCM(
                Constants.HIVE_PROFILE,
                null,
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    view.HIVE_USER_ID,
                    view.HIVE_TOKEN,
                    "POST",
                    "/api/client/mobile/1.0/registration/fcm"
                ),


                RegisterFCMRequest(token)
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful) {
                    view.sharedPreferences.edit().putBoolean(Constants.PREF_FCM_REGISTERED, true)
                        .apply()
                }
            }, {

            })

        return registerTokenDisposable
    }


    override fun reverseGeocode(latitude: Double, longitude: Double): Disposable {
        return RetrofitHelper.apiService(Constants.BASE_URL_NOMINATIM)
            .reverseGeocode(
                latitude,
                longitude,
                Constants.NOMINATIM_ZOOM_LVL,
                Constants.NOMINATIM_ZOOM_FORMAT,
                view.getCurrentLanguage().toLanguageTag()
            ).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful && it.body() != null) {
                    val address = StringBuilder()
                    val details = StringBuilder()

                    if (it.body()!!.name != null) {
                        address.append(it.body()!!.name)
                        if (it.body()!!.address.road != null)
                            details.append("${it.body()!!.address.road}")
                        if (it.body()!!.address.house_number != null)
                            details.append(", ${it.body()!!.address.house_number}")
                        else if (it.body()!!.address.neighbourhood!=null)
                            details.append(", ${it.body()!!.address.neighbourhood}")


                    } else {
                        address.append(it.body()!!.display_name)
                    }
                    view.onAddressFound(address.toString(), details.toString())
                }
            }, {

            })
    }

    override fun getUserAddresses(): Disposable {

        var getAdressesDisposable: Disposable? = null


        getAdressesDisposable = RetrofitHelper.apiService(Constants.BASE_URL)
            .getClientAddresses(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE,
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    view.HIVE_USER_ID,
                    view.HIVE_TOKEN,
                    "GET",
                    "/api/client/mobile/3.0/address/client"
                )
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({


            }, {

            })

        return getAdressesDisposable
    }

    override fun findCurrentAddress(latitude: Double, longitude: Double): Disposable {
        var findAddressDisposable: Disposable? = null

        findAddressDisposable = RetrofitHelper.apiService(Constants.BASE_URL)
            .getCurrentAddress(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE,
                "${latitude} ${longitude}"
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({

                when (it.code()) {
                    Constants.STATUS_SUCCESSFUL -> {
                        if (it.body() != null) {
                            if (it.body()!!.size > 0) {
                                var address = ""
                                for (component in it.body()!![0].components!!) {
                                    if (component.level == 7)
                                        address = component.name
                                    if (address == "" && component.level == 9)
                                        address = component.name
                                    if (component.level == 8)
                                        address += ", ${component.name}"
                                    if (address != "" && component.level == 9)
                                        address += ", ${component.name}"
                                }
                                view.onAddressFound(address, "")

                            } else
                                view.onAddressFound("", "")
                        }
                    }

                }
            }, {

            })

        return findAddressDisposable
    }

    override fun getBonuses(latitude: Double, longitude: Double): Disposable {
        return RetrofitHelper.apiService(Constants.BASE_URL)
            .getBonuses(
                view.getCurrentLanguage().toLanguageTag(),
                "${latitude} ${longitude}",
                Constants.HIVE_PROFILE,
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    view.HIVE_USER_ID,
                    view.HIVE_TOKEN,
                    "GET",
                    "/api/client/mobile/2.1/bonuses"
                )
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful && it.body() != null) {
                    view.onBonusReady(it.body()!!.balance)
                }
            }, {

            })
    }

    override fun getWeather(latitude: Double, longitude: Double): Disposable {
        return RetrofitHelper.apiService(Constants.BASE_URL_OPENWEATHER)
            .getWeather(
                latitude,
                longitude,
                Constants.WEATHER_LANG_RU,
                Constants.OPENWEATHERMAP_API,
                Constants.WEATHER_UNITS
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful && it.body() != null) {
                    view.onWeatherReady(it.body()!!.weather, it.body()!!.main.temp)
                }
            }, {

            })
    }

    fun getFinishedOrder(orderID: Long) : Disposable{
//        63000403382396

        return RetrofitHelper.apiService(Constants.BASE_URL)
            .getFinishedOrder(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE,
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    view.HIVE_USER_ID,
                    view.HIVE_TOKEN,
                    "GET",
                    "/api/client/mobile/1.0/orders/$orderID/finished"
                ),
                orderID
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful && it.body() != null) {


                }
            }, {

            })
    }

    //    override fun getRoute( listCoordinates: ArrayList<RouteCoordinates>, driverRoute: Boolean): Disposable {
//
//
//        return RetrofitHelper.apiService(Constants.BASE_URL_UZ_TAXI_NAVIGATION)
//            .getRoute(GetRouteRequest(listCoordinates, "auto", "ru-RU", "none"))
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeOn(Schedulers.io())
//            .subscribe({
//                if (it.isSuccessful && it.body() != null) {
//                    val decoded = PolyLineUtils.decode(it.body()!!.trip.legs[0].shape, 6)
//                    if (!driverRoute)
//                        view.drawRoute(decoded)
//                } else {
//                    Log.d("DSADASDSADSADAS","${it}")
//                    view.onErrorGetRoute()
//                }
//            }, {
//                Log.d("DSADASDSADSADAS","${it}")
//                view.onErrorGetRoute()
//            })
//
//    }
    override fun getRoute(
        listCoordinates: ArrayList<RouteCoordinates>,
        driverRoute: Boolean
    ): Disposable {

        val routeMap = ArrayList<String>()
        for (coordinate in listCoordinates){
            routeMap.add("${coordinate.lat},${coordinate.lon}")
        }


        return RetrofitHelper.apiService(Constants.BASE_URL_UZ_TAXI_NAVIGATION)
            .getRoute(routeMap,"car","uz",true)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful && it.body() != null) {
                    val decoded = PolyLineUtils.decode(it.body()!!.paths[0].points, 5)
                    if (!driverRoute)
                        view.drawRoute(decoded)
                } else {
                    Log.d("DSADASDSADSADAS", "${it}")
                    view.onErrorGetRoute()
                }
            }, {
                Log.d("DSADASDSADSADAS", "${it}")
                view.onErrorGetRoute()
            })

    }

    override fun getAvailableCars(latitude: Double, longitude: Double, tariff: Long): Disposable {
        return RetrofitHelper.apiService(Constants.BASE_URL)
            .getAvailableCars(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE,
                "$latitude $longitude",
                GetCarsRequest(CarPaymentMethod("cash"), tariff)
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.isSuccessful) {
                    val carsList = ArrayList<GetCarResponse>()
                    for ((counter, car) in it.body()!!.withIndex()) {
                        if (counter < 5) {
                            carsList.add(car)
                        }
                    }
                    view.onCarsAvailabe(carsList)
//                    view.onCarsAvailabe(it.body()!!)
                }
            }, {

            })
    }

    var SERVICE_ID = ""
    var PAYMENT_TYPE = "cash"
    override fun getAvailableService(latitude: Double, longitude: Double,paymentMethod: PaymentMethod): Disposable {
        if (PAYMENT_TYPE!=paymentMethod.kind){
            SERVICE_ID=""
            PAYMENT_TYPE = paymentMethod.kind
        }
        return RetrofitHelper.apiService(Constants.BASE_URL)
            .getAvailableService(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE, "$latitude $longitude",
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    view.HIVE_USER_ID,
                    view.HIVE_TOKEN,
                    "POST",
                    "/api/client/mobile/1.1/service"
                ),
                PaymentMethodParent(SERVICE_ID,paymentMethod)
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful) {
                    if (it.body() != null) {
                        when (it.body()!!.kind) {
                            "service" -> {
                                SERVICE_ID = it.body()!!.serviceId
                                view.onTariffsReady(it.body()!!.tariffs)
                            }
                            Constants.STILL_SAME_SERVICE -> {
                              view.onTarrifTheSame()
                            }
                            else -> {
                                view.onServiceNotAvailable()
                            }
                        }
                    }
                }
            }, {

            })
    }

    override fun getPaymentMethods(latitude: Double, longitude: Double): Disposable {
        return RetrofitHelper.apiService(Constants.BASE_URL)
            .getPaymentOptions(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE, "$latitude $longitude",
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    view.HIVE_USER_ID,
                    view.HIVE_TOKEN,
                    "GET",
                    "/api/client/mobile/1.0/payment-methods"
                )
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful && it.body() != null) {
                    view.onPaymentMethodsReady(it.body()!!)
                }
            }, {

            })
    }

    override fun createOrder(createOrderRequest: CreateOrderRequest): Disposable {
        return RetrofitHelper.apiService(Constants.BASE_URL)
            .createOrder(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE,
                "${view.START_POINT_LAT} ${view.START_POINT_LON}",
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    view.HIVE_USER_ID,
                    view.HIVE_TOKEN,
                    "POST",
                    "/api/client/mobile/4.0/orders"
                ),
                createOrderRequest
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful && it.body() != null) {
                    view.onOrderCreated(it.body()!!.id)
                } else {
                    view.onErrorCreateOrder()
                }
            }, {
                view.onErrorCreateOrder()
            })
    }

    override fun getOngoingOrder(): Disposable {
        return RetrofitHelper.apiService(Constants.BASE_URL)
            .getClientOrders(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE,
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    view.HIVE_USER_ID,
                    view.HIVE_TOKEN,
                    "GET",
                    "/api/client/mobile/2.0/orders"
                )
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful && it.body() != null) {
                    if (it.body()!!.size > 0) {
                        view.onOnGoingOrderFound(it.body()!![0])
                    } else {
                        view.onNoGoingOrder()
                    }
                }
            }, {

            })
    }

    override fun notifyDriver(orderID: Long): Disposable {
        return RetrofitHelper.apiService(Constants.BASE_URL)
            .notifyDriver(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE,
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    view.HIVE_USER_ID,
                    view.HIVE_TOKEN,
                    "GET",
                    "/api/client/mobile/1.0/orders/$orderID/coming"
                ),
                orderID
            ).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({

            }, {

            })
    }


    override fun notifyDriver(orderID: Long, rateOrderBody: RateOrderBody): Disposable {
        return RetrofitHelper.apiService(Constants.BASE_URL)
            .rateOrder(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE,
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    view.HIVE_USER_ID,
                    view.HIVE_TOKEN,
                    "POST",
                    "/api/client/mobile/1.1/orders/$orderID/feedback"
                ),
                orderID,
                rateOrderBody
            ).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({

            }, {

            })
    }


    override fun cancelOrder(orderID: Long): Disposable {
        return RetrofitHelper.apiService(Constants.BASE_URL)
            .cancelOrder(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE,
                NaiveHmacSigner.DateSignature(),
                NaiveHmacSigner.AuthSignature(
                    view.HIVE_USER_ID,
                    view.HIVE_TOKEN,
                    "DELETE",
                    "/api/client/mobile/1.0/orders/$orderID"
                ),
                orderID
            ).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful) {
                    view.onOrderCancelled()
                }
            }, {

            })

    }

    fun playLottie(
        view: LottieAnimationView,
        fileName: String,
        INFINITE: Boolean,
        REVERSE: Boolean
    ) {
        view.setAnimation(fileName)
        if (INFINITE)
            view.repeatCount = LottieDrawable.INFINITE
        if (REVERSE)
            view.repeatMode = LottieDrawable.REVERSE
        view.playAnimation()
    }

    override fun addCarImages() {
        val mapBoxStyle = view.mapBoxStyle
        val resources = view.resources
        val IMAGE_ID = "image-taxi-car"

        val carImage = Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(resources, R.drawable.car_map),
            64,
            128,
            true
        )


        mapBoxStyle.addImage(IMAGE_ID, carImage)


        mapBoxStyle.addImage(
            "finish-image",
            Constants.getBitmap(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_order_end,
                    null
                )!!
            )!!
        )

        mapBoxStyle.addImage(
            "start-image",
            Constants.getBitmap(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_order_start,
                    null
                )!!
            )!!
        )

        mapBoxStyle.addImage(
            "stop-image",
            Constants.getBitmap(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_order_end,
                    null
                )!!
            )!!
        )

        mapBoxStyle.addImage(
            "fleet-0", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_0),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-5", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_5),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-10",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_10),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-15", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_15),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-20",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_20),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-25", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_25),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-30",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_30),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-35", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_35),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-40",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_40),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-45", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_45),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-50",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_50),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-55", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_55),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-60",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_60),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-65", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_65),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-70",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_70),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-75", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_75),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-80",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_80),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-85", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_85),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-90",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_90),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-95", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_95),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-100", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_100),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-105", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_105),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-110",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_110),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-115", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_115),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-120",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_120),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-125", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_125),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-130",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_130),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-135", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_135),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-140",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_140),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-145", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_145),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-150",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_150),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-155", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_155),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-160",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_160),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-165", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_165),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-170",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_170),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-175", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_175),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-180",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_180),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-185", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_185),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-190", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_190),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-195", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_195),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-200",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_200),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-205", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_205),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-210",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_210),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-215", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_215),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-220",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_220),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-225", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_225),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-230",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_230),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-235", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_235),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-240",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_240),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-245", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_245),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-250",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_250),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-255", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_255),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-260",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_260),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-265", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_265),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-270",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_270),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-275", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_275),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-280",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_280),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-285", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_285),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-290", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_290),
                286, 286,
                true
            )
        )
        mapBoxStyle.addImage(
            "fleet-295", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_295),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-300",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_300),
                286, 286,
                true
            )
        )
        mapBoxStyle.addImage(
            "fleet-305", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_305),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-310",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_310),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-315", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_315),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-320",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_320),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-325", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_325),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-330",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_330),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-335", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_335),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-340",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_340),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-345", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_345),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-350",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_350),
                286, 286,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-355", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_355),
                286, 286,
                true
            )
        )


    }

}