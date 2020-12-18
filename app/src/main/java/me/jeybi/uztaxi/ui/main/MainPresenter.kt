package me.jeybi.uztaxi.ui.main

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import me.jeybi.uztaxi.utils.Constants

class MainPresenter(val view: MainActivity) : MainController.presenter {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    override fun checkIfAuthenticated() {
        val authenticated = view.sharedPreferences.getBoolean(Constants.PREF_AUTHENTICATED, false)
        if (!authenticated)
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

}