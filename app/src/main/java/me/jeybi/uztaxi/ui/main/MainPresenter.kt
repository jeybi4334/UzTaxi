package me.jeybi.uztaxi.ui.main

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.util.Base64Utils
import com.mapbox.geojson.Point
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
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
                NaiveHmacSigner.AuthSignature(view.HIVE_USER_ID, view.HIVE_TOKEN, "POST", "/api/client/mobile/1.0/registration/fcm"),


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
                        if (it.body()!!.address.road!=null)
                        details.append("${it.body()!!.address.road}")
                        if (it.body()!!.address.house_number!=null)
                            details.append(", ${it.body()!!.address.house_number}")
                    }else{
                        address.append(it.body()!!.display_name)
                    }
                    view.onAddressFound(address.toString(),details.toString())
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
            .getCurrentAddress(view.getCurrentLanguage().toLanguageTag(),Constants.HIVE_PROFILE, "${latitude} ${longitude}")
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
                                view.onAddressFound(address,"")

                            } else
                                view.onAddressFound("","")
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

    override fun getRoute( listCoordinates: ArrayList<RouteCoordinates>, driverRoute: Boolean): Disposable {


        return RetrofitHelper.apiService(Constants.BASE_URL_MAPZEN)
            .getRoute(GetRouteRequest(listCoordinates, "auto", "ru-RU", "none"))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful && it.body() != null) {
                    val decoded = PolyLineUtils.decode(it.body()!!.trip.legs[0].shape, 6)
                    if (!driverRoute)
                        view.drawRoute(decoded)
                } else {
                    view.onErrorGetRoute()
                }
            }, {
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
                    for ((counter, car) in it.body()!!.withIndex()){
                        if (counter<5){
                            carsList.add(car)
                        }
                    }
                    view.onCarsAvailabe(carsList)
//                    view.onCarsAvailabe(it.body()!!)
                }
            }, {

            })
    }

    override fun getAvailableService(latitude: Double, longitude: Double): Disposable {

        return RetrofitHelper.apiService(Constants.BASE_URL)
            .getAvailableService(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE, "$latitude $longitude")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                if (it.isSuccessful) {
                    view.onTariffsReady(it.body()!!.tariffs)
                }
            }, {

            })
    }

    override fun getPaymentMethods(latitude: Double, longitude: Double): Disposable {
        return RetrofitHelper.apiService(Constants.BASE_URL)
            .getPaymentOptions(
                view.getCurrentLanguage().toLanguageTag(),
                Constants.HIVE_PROFILE, "$latitude $longitude")
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


}