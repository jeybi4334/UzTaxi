package me.jeybi.uztaxi.ui.main

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.util.Base64Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.jeybi.uztaxi.model.RegisterFCMRequest
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.utils.Constants
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.UTF_8
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
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
        }


    }

    override fun registerFCMToken(token: String): Disposable {
        var registerTokenDisposable: Disposable? = null

        val DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZ"
        val sdf = SimpleDateFormat(DATE_FORMAT)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val dateTimeString: String = sdf.format(Date())
        println(dateTimeString) // current UTC time

        val timeStamp: Long = sdf.parse(dateTimeString).time //current UTC time in milisec

        val cal: Calendar = Calendar.getInstance()
        cal.setTime(Date(timeStamp))
        cal.add(Calendar.HOUR_OF_DAY, 5)
        cal.add(Calendar.MINUTE, 30)

        val DATE = sdf.format(cal.time)

        val HIVE_TOKEN = view.sharedPreferences.getString(Constants.HIVE_USER_TOKEN,"")
        val HIVE_USER_ID = view.sharedPreferences.getLong(Constants.HIVE_USER_ID,0)

        val secret = Base64Utils.decode(HIVE_TOKEN)

        val nonce = System.currentTimeMillis()


        val data = "POST" + "/api/client/mobile/1.0/registration/fcm" + DATE + nonce



       val digest = Base64Utils.encode(hmac("HmacSHA256",secret,data))



        Log.d("DASDASDASD","${String.format("hmac %s:%s:%s",HIVE_USER_ID, nonce, digest)}")


        registerTokenDisposable = RetrofitHelper.apiService()
            .registerFCM(Constants.HIVE_PROFILE, null, DATE,  String.format("hmac %s:%s:%s",HIVE_USER_ID, nonce, digest), RegisterFCMRequest(token))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                registerTokenDisposable?.dispose()
                Log.d("DASDASDASD","${it}")

            }, {
                registerTokenDisposable?.dispose()

            })

        return registerTokenDisposable
    }

    override fun findCurrentAddress(latitude: Double, longitude: Double): Disposable {
        var findAddressDisposable : Disposable? = null

        findAddressDisposable = RetrofitHelper.apiService()
            .getCurrentAddress(Constants.HIVE_PROFILE,"${latitude} ${longitude}")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({

                when(it.code()){
                    Constants.STATUS_SUCCESSFUL->{
                        if (it.body()!=null)
                            view.onAddressFound(it.body()!![0].name)
                    }

                }
            },{

            })

        return findAddressDisposable
    }


    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    fun hmac(algorithm: String?, secret: ByteArray?, data: String): ByteArray? {
        val mac: Mac = Mac.getInstance(algorithm)
        val spec = SecretKeySpec(secret, algorithm)
        mac.init(spec)
        return mac.doFinal( data.toByteArray(UTF_8))
    }

}