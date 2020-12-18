package me.jeybi.uztaxi.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapzen.tangram.*
import com.mapzen.tangram.TouchInput.TapResponder
import com.mapzen.tangram.networking.DefaultHttpHandler
import com.mapzen.tangram.networking.HttpHandler
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottomsheet_map.*
import me.jeybi.uztaxi.BuildConfig.NEXTZEN_API_KEY
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.ui.intro.IntroActivity
import me.jeybi.uztaxi.ui.main.fragments.SearchFragment
import me.jeybi.uztaxi.utils.Constants
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.Request
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.abs


class MainActivity : BaseActivity(), MainController.view, MapView.MapReadyCallback,
    LocationListener {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    lateinit var presenter: MainPresenter

    var mapController: MapController? = null
    var mLocationManager: LocationManager? = null

    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {
        presenter = MainPresenter(this)
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
//        presenter.checkIfAuthenticated()

        setUpMap()
    }

    override fun onUserNotAuthenticated() {
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }

    override fun onUserApproved() {

    }

    override fun buildAlertMessageNoGps() {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("GPS aniqlanmadi")
            .setCancelable(false)
            .setPositiveButton(
                "Xa"
            ) { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton(
                "Yo'q"
            ) { dialog, id -> dialog.cancel() }
        val alert: AlertDialog = builder.create()
        alert.show()

    }

    private fun setUpMap() {
        setUpBottomSheet()
        presenter.checkGPS()
        presenter.requestPermissions()

        // This starts a background process to set up the map.
        mapView.getMapAsync(this, getHttpHandler())
    }

    fun setUpBottomSheet() {
        val bottomSheetBehaviour = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehaviour.isFitToContents = false
        bottomSheetBehaviour.halfExpandedRatio = 0.4f


        changeBottomSheetFragment(SearchFragment())

        bottomSheetBehaviour.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("DASDADS", "STATE $newState")
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                if (slideOffset > 0.3) {
                    cardGPS.alpha = 0.6f - slideOffset
                    cardNext.alpha = 0.6f - slideOffset

                    //                    if (cardGPS.alpha == 1f) {
//                        cardGPS.animate().alpha(0f).setDuration(200)
//                            .setInterpolator(AccelerateInterpolator()).start()
//                    }

                } else {
                    cardGPS.alpha = 1f
                    cardNext.alpha = 1f
//                    if (cardGPS.alpha == 0f) {
//                        cardGPS.animate().alpha(1f).setDuration(200)
//                            .setInterpolator(AccelerateInterpolator()).start()
//                    }
                }
            }
        })

    }

    fun changeBottomSheetFragment(newFragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()

// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack if needed

// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack if needed
        transaction.replace(R.id.containerBottomSheet, newFragment)

        transaction.commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        val permissionsToRequest: ArrayList<String?> = ArrayList()
        for (i in grantResults.indices) {
            permissionsToRequest.add(permissions[i])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onMapReady(mapController: MapController?) {

        setUpMapButtons()

        this.mapController = mapController
        // Set our API key as a scene update.
        val updates: MutableList<SceneUpdate> = java.util.ArrayList()
        updates.add(SceneUpdate("global.sdk_api_key", NEXTZEN_API_KEY))

        mapController?.loadSceneFileAsync("walkabout/walkabout-style.yaml", updates)


        val cameraPosition = CameraPosition()
        cameraPosition.latitude = 41.312475324732056
        cameraPosition.longitude = 69.28003451322134
        cameraPosition.zoom = 16f
        cameraPosition.tilt = Math.toRadians(45.0).toFloat()

        mapController?.flyToCameraPosition(cameraPosition, 0, null)

        val touchInput: TouchInput = mapController!!.touchInput

        touchInput.setTapResponder(object : TapResponder {
            override fun onSingleTapUp(x: Float, y: Float): Boolean {
                val pointLat = mapController.screenPositionToLngLat(PointF(x, y))
                mapController.updateCameraPosition(
                    CameraUpdateFactory.setPosition(pointLat!!),
                    1000
                )
                return false
            }

            override fun onSingleTapConfirmed(x: Float, y: Float): Boolean {
                return true
            }
        })

        touchInput.setAllGesturesEnabled()



        touchInput.setPanResponder(object : TouchInput.PanResponder {
            override fun onPanBegin(): Boolean {
                imageViewCloudTop.animate().translationY(-200f).alpha(0.6f).setInterpolator(AccelerateInterpolator()).setDuration(800).start()
                mapController.updateCameraPosition(
                    CameraUpdateFactory.setZoom(mapController.cameraPosition.zoom - 0.05f),
                    0,
                    MapController.EaseType.CUBIC
                )
                rotatePointerRectangleAnimation()

                mapController.panResponder.onPanBegin()

                return false
            }

            override fun onPan(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
                mapController.panResponder.onPan(startX, startY, endX, endY)

                val diffY: Float = startY - endY
                val diffX: Float = startX - endX


                if (abs(diffX) > abs(diffY)) {
                    if (startX - endX < 0) {
                        animatePointerRectangle(DIRECTION_LEFT)
                        RECTANGLE_ANIMATING = true
                    }
                    if (startX - endX > 0) {
                        animatePointerRectangle(DIRECTION_RIGHT)
                        RECTANGLE_ANIMATING = true
                    }
                } else {
                    if (startY - endY < 0) {
                        animatePointerRectangle(DIRECTION_BOTTOM)
                        RECTANGLE_ANIMATING = true
                    }
                    if (startY - endY > 0) {
                        animatePointerRectangle(DIRECTION_TOP)
                        RECTANGLE_ANIMATING = true
                    }


                }


                return false
            }

            override fun onPanEnd(): Boolean {
                imageViewCloudTop.animate().translationY(0f).alpha(1f).setInterpolator(DecelerateInterpolator()).setDuration(600).start()

                mapController.updateCameraPosition(
                    CameraUpdateFactory.setZoom(mapController.cameraPosition.zoom + 0.05f),
                    500,
                    MapController.EaseType.QUINT
                )


                RECTANGLE_ANIMATING = false
                cancelPointerAnimation()
                cancelPointerRectangleAnimation()
                mapController.panResponder.onPanEnd()

                return false

            }

            override fun onFling(
                posX: Float,
                posY: Float,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                mapController.panResponder.onFling(posX, posY, velocityX, velocityY)

                mapController.updateCameraPosition(
                    CameraUpdateFactory.setZoom(mapController.cameraPosition.zoom + 0.05f),
                    500,
                    MapController.EaseType.QUINT
                )


                return false
            }

            override fun onCancelFling(): Boolean {

                mapController.panResponder.onCancelFling()
                return false
            }

        })


    }

    private fun setUpMapButtons() {
        cardGPS.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val criteria = Criteria()
                criteria.accuracy = Criteria.ACCURACY_COARSE
                criteria.powerRequirement = Criteria.POWER_LOW
                criteria.isAltitudeRequired = false
                criteria.isBearingRequired = false
                criteria.isSpeedRequired = false
                criteria.isCostAllowed = true
                criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH
                criteria.verticalAccuracy = Criteria.ACCURACY_HIGH

                val looper = null

                mLocationManager?.requestSingleUpdate(criteria, this, looper)
            }
        }
    }


    var RECTANGLE_ANIMATING = false
    val DIRECTION_LEFT = 1
    val DIRECTION_TOP = 2
    val DIRECTION_RIGHT = 3
    val DIRECTION_BOTTOM = 4

    private fun animatePointerRectangle(direction: Int) {
        if (!RECTANGLE_ANIMATING) {
            when (direction) {
                1 -> {
                    pointerRectangle.animate().translationX(
                        1 * Constants.convertDpToPixel(
                            8f,
                            this
                        )
                    ).setDuration(200).start()
                }
                2 -> {
                    pointerRectangle.animate().translationY(
                        -1 * Constants.convertDpToPixel(
                            8f,
                            this
                        )
                    ).setDuration(200).start()
                }
                3 -> {
                    pointerRectangle.animate().translationX(
                        -1 * Constants.convertDpToPixel(
                            8f,
                            this
                        )
                    ).setDuration(200).start()

                }
                4 -> {
                    pointerRectangle.animate().translationY(
                        1 * Constants.convertDpToPixel(
                            9f,
                            this
                        )
                    ).setDuration(200).start()
                    pointerRectangle.animate().rotation(-20f).setStartDelay(100).setDuration(100)
                        .start()
//                    pointerRectangle.animate().translationX(-1 * Constants.convertDpToPixel(1.5f,this)).setStartDelay(100).setDuration(100).start()

                }
            }
        }

    }

    private fun cancelPointerRectangleAnimation() {
        pointerRectangle.animate().translationY(0f).setDuration(200).start()
        pointerRectangle.animate().translationX(0f).setDuration(200).start()
    }

    private fun rotatePointerRectangleAnimation() {


        pointerRectangle.rotation = 0f
        pointerLayout.animate().translationY(-50f).setInterpolator(AccelerateInterpolator())
            .setDuration(
                200
            ).start()
        imageViewPointerShadow.animate().scaleX(1.8f).setInterpolator(AccelerateInterpolator())
            .setDuration(
                200
            ).start()
        imageViewPointerShadow.animate().alpha(0.7f).setInterpolator(AccelerateInterpolator())
            .setDuration(
                200
            ).start()
//        pointerRectangle.animate().rotation(pointerRectangle.rotation-180f).setDuration(600).setInterpolator(LinearInterpolator()).start()

    }

    private fun cancelPointerAnimation() {


        pointerRectangle.rotation = 0f
        pointerLayout.animate().translationY(0f).setInterpolator(OvershootInterpolator())
            .setDuration(
                1200
            ).start()
        imageViewPointerShadow.animate().scaleX(1f).setInterpolator(AccelerateInterpolator())
            .setDuration(
                200
            ).start()
        imageViewPointerShadow.animate().alpha(1f).setInterpolator(AccelerateInterpolator())
            .setDuration(
                200
            ).start()

        pointerRectangle.animate().rotation(pointerRectangle.rotation + 90f).setDuration(600)
            .setInterpolator(
                LinearInterpolator()
            ).start()
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    fun getHttpHandler(): HttpHandler? {
        val builder = DefaultHttpHandler.getClientBuilder()
        val cacheDir: File? = externalCacheDir
        if (cacheDir != null && cacheDir.exists()) {
            builder.cache(Cache(cacheDir, 16 * 1024 * 1024))
        }
        return object : DefaultHttpHandler(builder) {
            var tileCacheControl = CacheControl.Builder().maxStale(7, TimeUnit.DAYS).build()
            override fun configureRequest(url: HttpUrl, builder: Request.Builder) {
                if ("tile.nextzen.com" == url.host()) {
                    builder.cacheControl(tileCacheControl)
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        val cameraPosition = CameraPosition()

        cameraPosition.latitude = location.latitude
        cameraPosition.longitude = location.longitude
        cameraPosition.zoom = 16f
        cameraPosition.tilt = Math.toRadians(45.0).toFloat()

        mapController?.flyToCameraPosition(cameraPosition, 1000, null)
//        mLocationManager?.removeUpdates(this)
    }

    // Required functions

    override fun onProviderEnabled(provider: String) {
        super.onProviderEnabled(provider)
    }

    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
    }

    override fun onStatusChanged(arg0: String?, arg1: Int, arg2: Bundle?) {}

}