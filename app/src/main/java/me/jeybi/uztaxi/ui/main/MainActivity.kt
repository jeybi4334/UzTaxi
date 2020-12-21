package me.jeybi.uztaxi.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.layers.TransitionOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottomsheet_map.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.ui.intro.IntroActivity
import me.jeybi.uztaxi.ui.main.fragments.SearchFragment
import me.jeybi.uztaxi.ui.main.fragments.WhereToFragment
import me.jeybi.uztaxi.utils.Constants
import java.lang.Math.abs
import java.net.URI
import java.net.URISyntaxException


class MainActivity : BaseActivity(), MainController.view,
    LocationListener, PermissionsListener {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    lateinit var presenter: MainPresenter

    var mLocationManager: LocationManager? = null

    lateinit var bottomSheetBehaviour: BottomSheetBehavior<View>

    val routeCoordinates = ArrayList<Point>()


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


    lateinit var mapboxMap: MapboxMap
    lateinit var mapBoxStyle: Style


    private fun setUpMap() {
        setUpBottomSheet()
        presenter.checkGPS()
        presenter.requestPermissions()


        mapView?.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(
                Style.Builder().fromUri("mapbox://styles/jeybi24/ckiyh6njc4oxw19o2lh0n9rtl")
            )
            {
                mapBoxStyle = it

                enableLocationComponent(it)

                val position = CameraPosition.Builder()
                    .target(LatLng(41.350537, 69.219483))
                    .zoom(16.0)
                    .tilt(20.0)
                    .build()
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)
                setUpMapButtons()



            }

            mapboxMap.addOnMoveListener(object : MapboxMap.OnMoveListener {
                override fun onMoveBegin(detector: MoveGestureDetector) {
                    // user started moving the map
                    rotatePointerRectangleAnimation()
                    imageViewCloudTop.animate().translationY(-200f).alpha(0.6f)
                        .setInterpolator(AccelerateInterpolator()).setDuration(800).start()

                }

                override fun onMove(detector: MoveGestureDetector) {
                    // user is moving the map

                    if (abs(detector.lastDistanceX) > abs(detector.lastDistanceY)) {
                        if (detector.lastDistanceX < 0) {
                            animatePointerRectangle(DIRECTION_LEFT)
                            RECTANGLE_ANIMATING = true
                        }
                        if (detector.lastDistanceX > 0) {
                            animatePointerRectangle(DIRECTION_RIGHT)
                            RECTANGLE_ANIMATING = true
                        }
                    } else {
                        if (detector.lastDistanceY < 0) {
                            animatePointerRectangle(DIRECTION_BOTTOM)
                            RECTANGLE_ANIMATING = true
                        }
                        if (detector.lastDistanceY > 0) {
                            animatePointerRectangle(DIRECTION_TOP)
                            RECTANGLE_ANIMATING = true
                        }


                    }

                }

                override fun onMoveEnd(detector: MoveGestureDetector) {
                    imageViewCloudTop.animate().translationY(0f).alpha(1f)
                        .setInterpolator(DecelerateInterpolator()).setDuration(600).start()
                    // user stopped moving the map
                    RECTANGLE_ANIMATING = false
                    cancelPointerAnimation()
                    cancelPointerRectangleAnimation()

                }
            })


        }

    }


    fun setUpBottomSheet() {
        bottomSheetBehaviour = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehaviour.isFitToContents = false
        bottomSheetBehaviour.halfExpandedRatio = 0.45f


        changeBottomSheetFragment(SearchFragment(), false)

        bottomSheetBehaviour.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                Log.d("DASDADS", "STATE $newState")
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                if (slideOffset > 0.3) {
                    cardGPS.alpha = 0.6f - slideOffset
                    cardNext.alpha = 0.6f - slideOffset


                } else {
                    cardGPS.alpha = 1f
                    cardNext.alpha = 1f

                }
            }
        })

    }

    override fun onBottomSheetSearchItemClicked() {
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        changeBottomSheetFragment(WhereToFragment(), true)
    }

    override fun startFindingCar() {
        imageViewPointerShadow.visibility = View.INVISIBLE
        imageViewPointerFoot.visibility = View.GONE
        lottieAnimation.visibility = View.VISIBLE
        lottieAnimation.playAnimation()
    }


    fun changeBottomSheetFragment(newFragment: Fragment, backStack: Boolean) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.containerBottomSheet, newFragment)
        if (backStack)
            transaction.addToBackStack(null)

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

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()


    }

    override fun onLocationChanged(location: Location) {
        val position = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(16.0)
            .tilt(20.0)
            .build()
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)

    }

// Required functions

    override fun onProviderEnabled(provider: String) {
        super.onProviderEnabled(provider)
    }

    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
    }

    override fun onStatusChanged(arg0: String?, arg1: Int, arg2: Bundle?) {}


    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .pulseColor(ResourcesCompat.getColor(resources, R.color.purple_200, null))
                .backgroundStaleTintColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.purple_200,
                        null
                    )
                )
                .bearingTintColor(ResourcesCompat.getColor(resources, R.color.purple_200, null))
                .foregroundStaleTintColor(ResourcesCompat.getColor(resources, R.color.white, null))
                .pulseColor(ResourcesCompat.getColor(resources, R.color.white, null))
                .foregroundTintColor(ResourcesCompat.getColor(resources, R.color.white, null))
                .accuracyColor(ContextCompat.getColor(this, R.color.mapboxGreen))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(
                this,
                loadedMapStyle
            )
                .locationComponentOptions(customLocationComponentOptions)
                .build()

// Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {

// Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

// Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

// Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

// Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, "Explanation", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(this, "Location Permission Not Granted", Toast.LENGTH_LONG).show()
            finish()
        }
    }

}