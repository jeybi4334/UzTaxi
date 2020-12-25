package me.jeybi.uztaxi.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottomsheet_map.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.ui.intro.IntroActivity
import me.jeybi.uztaxi.ui.main.fragments.*
import me.jeybi.uztaxi.utils.Constants


class MainActivity : BaseActivity(), MainController.view,
    LocationListener, PermissionsListener {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    lateinit var presenter: MainPresenter

    var mLocationManager: LocationManager? = null

    lateinit var bottomSheetBehaviour: BottomSheetBehavior<View>

    var mainDisposables = CompositeDisposable()

    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {
        presenter = MainPresenter(this)
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        presenter.checkIfAuthenticated()

    }

    override fun onUserNotAuthenticated() {
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }

    override fun onUserApproved() {
        setUpMap()

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this@MainActivity,
            OnSuccessListener<InstanceIdResult> { instanceIdResult ->
                val mToken = instanceIdResult.token

                mainDisposables.add(presenter.registerFCMToken(mToken))

            })

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
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500)


                setUpMapButtons()


            }


            var translationX = 0f
            var translationY = 0f

            var DIRECTION_CURRENT = 0


            mapboxMap.addOnMoveListener(object : MapboxMap.OnMoveListener {
                override fun onMoveBegin(detector: MoveGestureDetector) {
                    // user started moving the map
                    rotatePointerRectangleAnimation()
                    imageViewCloudTop.animate().translationY(-200f).alpha(0.6f)
                        .setInterpolator(AccelerateInterpolator()).setDuration(800).start()

                }

                override fun onMove(detector: MoveGestureDetector) {
                    // user is moving the map

                    translationX = detector.lastDistanceX.toInt().toFloat()
                    translationY = detector.lastDistanceY.toInt().toFloat()

                    if (kotlin.math.abs(translationX) > Constants.convertDpToPixel(
                            7f,
                            this@MainActivity
                        )
                    ) {

                        if (detector.lastDistanceX > 0 && DIRECTION_CURRENT == DIRECTION_LEFT) {
                            RECTANGLE_ANIMATING = false
                        } else if (detector.lastDistanceX < 0 && DIRECTION_CURRENT == DIRECTION_RIGHT) {
                            RECTANGLE_ANIMATING = false
                        }

                        if (detector.lastDistanceX < 0) {
                            animatePointerRectangle(DIRECTION_LEFT)
                            RECTANGLE_ANIMATING = true
                            DIRECTION_CURRENT = DIRECTION_LEFT
                        }
                        if (detector.lastDistanceX > 0) {
                            animatePointerRectangle(DIRECTION_RIGHT)
                            RECTANGLE_ANIMATING = true
                            DIRECTION_CURRENT = DIRECTION_RIGHT
                        }
                    }
//                    pointerRectangle.translationX = translationX

                    if (kotlin.math.abs(translationY) > Constants.convertDpToPixel(
                            7f,
                            this@MainActivity
                        )
                    ) {
                        if (detector.lastDistanceY > 0 && DIRECTION_CURRENT == DIRECTION_BOTTOM) {
                            RECTANGLE_ANIMATING = false
                        } else if (detector.lastDistanceY < 0 && DIRECTION_CURRENT == DIRECTION_TOP) {
                            RECTANGLE_ANIMATING = false
                        }

                        if (detector.lastDistanceY < 0) {
                            animatePointerRectangle(DIRECTION_BOTTOM)
                            RECTANGLE_ANIMATING = true
                            DIRECTION_CURRENT = DIRECTION_BOTTOM
                        }
                        if (detector.lastDistanceY > 0) {
                            animatePointerRectangle(DIRECTION_TOP)
                            RECTANGLE_ANIMATING = true
                            DIRECTION_CURRENT = DIRECTION_TOP
                        }

                    }
//                        pointerRectangle.translationY = detector.lastDistanceY.toInt().toFloat()

                    Log.d(
                        "DSADSADA",
                        "${
                            detector.lastDistanceX.toInt().toFloat()
                        }  ,    ${detector.lastDistanceY.toInt().toFloat()}"
                    )


                }


                override fun onMoveEnd(detector: MoveGestureDetector) {
                    imageViewCloudTop.animate().translationY(0f).alpha(1f)
                        .setInterpolator(DecelerateInterpolator()).setDuration(600).start()
                    // user stopped moving the map
                    RECTANGLE_ANIMATING = false
                    cancelPointerAnimation()
                    cancelPointerRectangleAnimation()

                    val mapLatLng = mapboxMap.cameraPosition.target
                    val mapLat = mapLatLng.latitude
                    val mapLong = mapLatLng.longitude
//                    if (finderDisposable != null&&!finderDisposable!!.isDisposed)
//                        finderDisposable?.dispose()

                    mainDisposables.add(presenter.findCurrentAddress(mapLat, mapLong))

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

        pointerLayout.visibility = View.GONE
        imageViewPointerShadow.visibility = View.GONE

        val latLngBounds = LatLngBounds.Builder()
            .include(LatLng(41.38244668021558, 69.30357947935912))
            .include(LatLng(41.36736430714892, 69.29200483623976))
            .build()


        mapboxMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                latLngBounds,
                200,
                500,
                200,
                900
            ), 1000
        )

        initRouteCoordinates()

        mapBoxStyle.addSource(
            GeoJsonSource(
                "line-source",
                FeatureCollection.fromFeatures(
                    arrayOf(
                        Feature.fromGeometry(
                            LineString.fromLngLats(routeCoordinates)
                        )
                    )
                ), GeoJsonOptions().withLineMetrics(true)
            )
        )

        mapBoxStyle.addLayer(
            LineLayer("linelayer", "line-source").withProperties(
//                lineDasharray(arrayOf(0.01f, 2f)),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(6f),
                lineGradient(
                    interpolate(
                        linear(), lineProgress(),
                        stop(0f, rgb(112, 223, 125)), // green
                        stop(0.35f, rgb(112, 223, 125)), // cyan
                        stop(0.36f, rgb(255, 252, 0)), // yellow
                        stop(0.39f, rgb(255, 30, 0)), // red
                        stop(0.4f, rgb(112, 223, 125)), // green
                        stop(0.42f, rgb(255, 252, 0)), // yellow
                        stop(0.44f, rgb(112, 223, 125)),// green
                        stop(1f, rgb(112, 223, 125)), // green
                    )
                )
            )
        )


        mapBoxStyle.addImage(
            "start-image",
            Constants.getBitmap(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.icon_start,
                    null
                )!!
            )!!
        )

        mapBoxStyle.addImage(
            "finish-image",
            Constants.getBitmap(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.icon_finish,
                    null
                )!!
            )!!
        )

        mapBoxStyle.addImage(
            "car-1-image",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.car_map),
                48,
                96,
                true
            )
        )


//        69.29200483623976,41.36736430714892

//        41.37458804781986, 69.30572136297432


        mapBoxStyle.addSource(
            GeoJsonSource(
                "start-source", Feature.fromGeometry(
                    Point.fromLngLat(
                        69.30357947935912,
                        41.38244668021558
                    )
                )
            )
        )

        mapBoxStyle.addSource(
            GeoJsonSource(
                "finish-source", Feature.fromGeometry(
                    Point.fromLngLat(
                        69.29200483623976,
                        41.36736430714892
                    )
                )
            )
        )

        mapBoxStyle.addSource(
            GeoJsonSource(
                "car-1-source", Feature.fromGeometry(
                    Point.fromLngLat(
                        69.30572136297432,
                        41.37458804781986
                    )
                )
            )
        )






        mapBoxStyle.addLayer(
            SymbolLayer(
                "start-layer",
                "start-source"
            ).withProperties(
                iconImage("start-image"),
                iconOffset(arrayOf(0f, -8f))
            )
        )
        mapBoxStyle.addLayer(
            SymbolLayer(
                "finish-layer",
                "finish-source"
            ).withProperties(
                iconImage("finish-image"),
                iconOffset(arrayOf(0f, -8f))
            )
        )

        mapBoxStyle.addLayer(
            SymbolLayer(
                "car-1-layer",
                "car-1-source"
            ).withProperties(
                iconImage("car-1-image"),
                iconOffset(arrayOf(0f, -8f))
            )
        )


    }

    override fun startFindingCar() {
        cardNext.visibility = View.GONE

        val latLngBounds = LatLngBounds.Builder()
            .include(LatLng(41.38244668021558, 69.30357947935912))
            .include(LatLng(41.36736430714892, 69.29200483623976))
            .build()


        mapboxMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                latLngBounds,
                200,
                500,
                200,
                400
            ), 400
        )


        changeBottomSheetFragment(CarSearchFragment(), true)
        pointerLayout.visibility = View.VISIBLE
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        imageViewPointerShadow.visibility = View.INVISIBLE
        imageViewPointerFoot.visibility = View.GONE
        lottieAnimation.visibility = View.VISIBLE
        lottieAnimation.playAnimation()
    }

    override fun onCancelSearchClicked() {
        pointerLayout.visibility = View.GONE
        lottieAnimation.cancelAnimation()
        lottieAnimation.visibility = View.GONE
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        changeBottomSheetFragment(CarFoundFragment(), true)
        mapBoxStyle.removeLayer("finish-layer")
        mapBoxStyle.removeLayer("start-layer")
        mapBoxStyle.removeLayer("linelayer")
        mapBoxStyle.removeLayer("car-1-layer")


        val latLngBounds = LatLngBounds.Builder()
            .include(LatLng(41.37458804781986, 69.30572136297432))
            .include(LatLng(41.38244668021558, 69.30357947935912))
            .build()

        mapboxMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                latLngBounds,
                250,
                400,
                150,
                950
            ), 1000
        )


//        41.37814966044984, 69.30567456784124

        val carRoute = ArrayList<Point>()
        carRoute.add(Point.fromLngLat(69.30572136297432, 41.37458804781986))
        carRoute.add(Point.fromLngLat(69.30567456784124, 41.37814966044984))
        carRoute.add(Point.fromLngLat(69.30538975679481, 41.37853598436575))
        carRoute.add(Point.fromLngLat(69.3005808318186, 41.378946964735896))
        carRoute.add(Point.fromLngLat(69.30087659713605, 41.381577177607134))
        carRoute.add(Point.fromLngLat(69.30375217715482, 41.381560761149025))
        carRoute.add(Point.fromLngLat(69.30382873199397, 41.38191976593085))
        carRoute.add(Point.fromLngLat(69.30383975980732, 41.3824104148219))
        carRoute.add(Point.fromLngLat(69.30357947935912, 41.38244668021558))



        mapBoxStyle.addSource(
            GeoJsonSource(
                "car-line-source",
                FeatureCollection.fromFeatures(
                    arrayOf(
                        Feature.fromGeometry(
                            LineString.fromLngLats(carRoute)
                        )
                    )
                ), GeoJsonOptions().withLineMetrics(true)
            )
        )

        mapBoxStyle.addLayer(
            LineLayer("car-line-layer", "car-line-source").withProperties(
//                lineDasharray(arrayOf(0.01f, 2f)),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(4f),
                lineColor(Color.parseColor("#39034E"))
            )
        )

        mapBoxStyle.addImage(
            "car-2-image",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.car_black),
                64,
                128,
                true
            )
        )

        mapBoxStyle.addSource(
            GeoJsonSource(
                "car-2-source", Feature.fromGeometry(
                    Point.fromLngLat(
                        69.30572136297432,
                        41.37458804781986
                    )
                )
            )
        )

        mapBoxStyle.addLayer(
            SymbolLayer(
                "car-2-layer",
                "car-2-source"
            ).withProperties(
                iconImage("car-2-image"),
                iconOffset(arrayOf(0f, -8f))
            )
        )

        mapBoxStyle.addImage(
            "car-dest-image",
            Constants.getBitmap(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.icon_start,
                    null
                )!!
            )!!
        )

        mapBoxStyle.addSource(
            GeoJsonSource(
                "car-dest-source", Feature.fromGeometry(
                    Point.fromLngLat(
                        69.30357947935912, 41.38244668021558
                    )
                )
            )
        )

        mapBoxStyle.addLayer(
            SymbolLayer(
                "car-dest-layer",
                "car-dest-source"
            ).withProperties(
                iconImage("car-dest-image"),
                iconOffset(arrayOf(0f, -8f))
            )
        )

    }

    override fun onRideStarted() {
        bottomSheetBehaviour.halfExpandedRatio = 0.4f
        changeBottomSheetFragment(RideStartedFragment(), true)
    }

    override fun editRideClicked() {
        bottomSheetBehaviour.halfExpandedRatio = 0.6f
        changeBottomSheetFragment(EditRideFragment(), true)
    }

    override fun onCancelRideClicked() {
        changeBottomSheetFragment(RideFinishedFragment(), true)
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onAddressFound(name: String) {
        textViewCurrentAddress.text = name
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
                    pointerRectangle.animate().rotation(0f).setStartDelay(100).setDuration(100)
                        .start()
                }
                2 -> {
                    pointerRectangle.animate().translationY(
                        -1 * Constants.convertDpToPixel(
                            8f,
                            this
                        )
                    ).setDuration(200).start()
                    pointerRectangle.animate().rotation(0f).setStartDelay(100).setDuration(100)
                        .start()
                }
                3 -> {
                    pointerRectangle.animate().translationX(
                        -1 * Constants.convertDpToPixel(
                            8f,
                            this
                        )
                    ).setDuration(200).start()
                    pointerRectangle.animate().rotation(0f).setStartDelay(100).setDuration(100)
                        .start()
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
        mainDisposables.dispose()

    }

    override fun onLocationChanged(location: Location) {
        val position = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(16.0)
            .tilt(20.0)
            .build()

        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)

        Log.d("DASDASDS", "${location.latitude}  ${location.longitude}")


    }

    val routeCoordinates = ArrayList<Point>()

    private fun initRouteCoordinates() {
        routeCoordinates.add(Point.fromLngLat(69.30357947935912, 41.38244668021558))
        routeCoordinates.add(Point.fromLngLat(69.30383104695989, 41.38240699522194))
        routeCoordinates.add(Point.fromLngLat(69.30383862123891, 41.38201940743597))
        routeCoordinates.add(Point.fromLngLat(69.303761363593, 41.38155338973744))
        routeCoordinates.add(Point.fromLngLat(69.29754440164538, 41.38156920879397))
        routeCoordinates.add(Point.fromLngLat(69.29805760720247, 41.38335576338465))
        routeCoordinates.add(Point.fromLngLat(69.2973205578097, 41.3833286706471))
        routeCoordinates.add(Point.fromLngLat(69.29683505959122, 41.38162512717319))
        routeCoordinates.add(Point.fromLngLat(69.29693067313367, 41.38129716724795))
        routeCoordinates.add(Point.fromLngLat(69.29342001377383, 41.36918497862202))
        routeCoordinates.add(Point.fromLngLat(69.292742155587, 41.36756958147784))
        routeCoordinates.add(Point.fromLngLat(69.29200483623976, 41.36736430714892))

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