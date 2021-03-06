package me.jeybi.uztaxi.ui.main

import android.Manifest
import android.animation.*
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.*
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.utils.LottieValueAnimator
import com.google.android.gms.common.util.Base64Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.messaging.FirebaseMessaging
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.android.gestures.RotateGestureDetector
import com.mapbox.geojson.*
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
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin
import com.mapbox.mapboxsdk.plugins.localization.MapLocale
import com.mapbox.mapboxsdk.snapshotter.MapSnapshotter
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfMeasurement
import com.romainpiel.shimmer.Shimmer
import com.romainpiel.shimmer.ShimmerTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_edit_ride.*
import kotlinx.android.synthetic.main.bottom_edit_ride.textViewRate
import kotlinx.android.synthetic.main.bottom_sheet_car_search.*
import kotlinx.android.synthetic.main.bottom_sheet_found_driver.*
import kotlinx.android.synthetic.main.bottom_sheet_search.*
import kotlinx.android.synthetic.main.bottom_sheet_where.*
import kotlinx.android.synthetic.main.bottomsheet_map.*
import kotlinx.android.synthetic.main.item_search.*
import kotlinx.android.synthetic.main.layout_delivery.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.UzTaxiApplication
import me.jeybi.uztaxi.database.AddressEntity
import me.jeybi.uztaxi.model.*
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.ui.adapters.CarsAdapter
import me.jeybi.uztaxi.ui.adapters.RouteAdapter
import me.jeybi.uztaxi.ui.intro.IntroActivity
import me.jeybi.uztaxi.ui.main.bottomsheet.*
import me.jeybi.uztaxi.ui.main.fragments.*
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.NaiveHmacSigner
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : BaseActivity(), MainController.view,
    LocationListener, RouteAdapter.onRouteAddClickListener {

    lateinit var presenter: MainPresenter

    var mLocationManager: LocationManager? = null

    lateinit var bottomSheetBehaviour: BottomSheetBehavior<View>

    var mainDisposables = CompositeDisposable()


    lateinit var shimmer: Shimmer

    var TARIFF_ID = 0L

    var CURRENT_LATITUDE: Double = 0.0
    var CURRENT_LONGITUDE: Double = 0.0

    var CURRENT_MODE = Constants.MODE_SEARCH_WHERE

    var ORDER_STATE = Constants.ORDER_STATE_NOT_CREATED

    var START_POINT_LAT = 0.0
    var START_POINT_LON = 0.0

    var END_POINT_LAT = 0.0
    var END_POINT_LON = 0.0
    var EDIT_POINT_LAT = 0.0
    var EDIT_POINT_LON = 0.0

    var START_POINT_NAME = ""
    var END_POINT_NAME = ""
    var EDIT_POINT_NAME = ""

    var BONUS = 0.0

    var TILT_MAP = Constants.DEFAULT_TILT_MAP
 
    val ROUTE_DATA = ArrayList<RouteItem>()

    var COST_CHANGE_STEP = 0.0

    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    private var mSocket: Socket? = null

    override fun onViewDidCreate(savedInstanceState: Bundle?) {
        presenter = MainPresenter(this)

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        presenter.checkIfAuthenticated()

        presenter.playLottie(lottieWeather, "day_rain.json", true, REVERSE = false)
        presenter.playLottie(lottieSeason, "rain.json", true, REVERSE = false)
        presenter.playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
    }


    override fun onUserNotAuthenticated() {
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }

    override fun onUserApproved() {
        shimmer = Shimmer()
        setUpMap()
        setUpNavigationView()
        mainDisposables.add(presenter.getUserAddresses())

        if (!sharedPreferences.getBoolean(Constants.PREF_FCM_REGISTERED, false)) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                val mToken = instanceIdResult
                mainDisposables.add(presenter.registerFCMToken(mToken))

            }
        }

        rvMenu.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
            navigationFragment.onOpen()
        }

    }


    override fun onBonusReady(bonus: Double) {
        BONUS = bonus
        if (::navigationFragment.isInitialized) {
            navigationFragment.onBonusReady(bonus)
        }
    }

    var PAYMENT_TYPE = Constants.PAYMENT_TYPE_CASH
    var PAYMENT_TYPE_ID = 0L

    lateinit var navigationFragment: NavigationFragment

    private fun setUpNavigationView() {
        val tx = supportFragmentManager.beginTransaction()
        navigationFragment = NavigationFragment()
        tx.replace(R.id.navigationView, navigationFragment)
        tx.commit()


        PAYMENT_TYPE =
            sharedPreferences.getString(Constants.PAYMENT_TYPE, Constants.PAYMENT_TYPE_CASH)
                ?: Constants.PAYMENT_TYPE_CASH
        PAYMENT_TYPE_ID = sharedPreferences.getLong(Constants.PAYMENT_TYPE_ID, 0L) ?: 0L

    }

    override fun buildAlertMessageNoGps() {


        val dialog = AlertDialog.Builder(this).create()
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_gps, null)

        val textYes = view.findViewById<TextView>(R.id.textYes)
        val textNo = view.findViewById<TextView>(R.id.textNo)

        textYes.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            dialog.cancel()
        }

        textNo.setOnClickListener {
            dialog.cancel()
        }

        dialog.setView(view)
        dialog.show()

    }


    lateinit var mapboxMap: MapboxMap
    lateinit var mapBoxStyle: Style

    var MAP_MOVING = false

    private fun setUpMap() {
        setUpBottomSheet()

        presenter.requestPermissions()


        mapView?.getMapAsync { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.setStyle(
//                Style.Builder().fromUri("mapbox://styles/jeybi24/ckiyh6njc4oxw19o2lh0n9rtl")
                Style.Builder().fromUri("mapbox://styles/jeybi24/ckkqszy7a145h17pmoy1le5tv")
//                Style.MAPBOX_STREETS
            )
            {
                mapBoxStyle = it
                setUpSocketForDrivers()
                val localizationPlugin = LocalizationPlugin(mapView!!, mapboxMap, it)

//                val builder = VmPolicy.Builder()
//                StrictMode.setVmPolicy(builder.build())

                try {

                    localizationPlugin.setMapLanguage(
                        when (getCurrentLanguage().toLanguageTag()) {
                            "ru" -> MapLocale.RUSSIA
                            "kl" -> MapLocale("name")
                            else -> MapLocale.US
                        }
                    )

                } catch (exception: RuntimeException) {
                }

                presenter.checkGPS()
                mapboxMap.uiSettings.isRotateGesturesEnabled = false
                mapboxMap.uiSettings.isLogoEnabled = false
                mapboxMap.uiSettings.isTiltGesturesEnabled = false
                mapboxMap.uiSettings.isAttributionEnabled = false


                if (sharedPreferences.getBoolean(Constants.SETTINGS_MAP_3D, false)) {
                    TILT_MAP = 90.0
//                    val buildingPlugin = BuildingPlugin(mapView, mapboxMap, it)
//                    buildingPlugin.setColor(Color.LTGRAY)
//                    buildingPlugin.setOpacity(0.6f)
//                    buildingPlugin.setMinZoomLevel(15f)
//                    buildingPlugin.setVisibility(true)
                }

//                mapboxMap.addOnRotateListener(object  : MapboxMap.OnRotateListener{
//                    override fun onRotateBegin(detector: RotateGestureDetector) {
//
//                    }
//
//                    override fun onRotate(detector: RotateGestureDetector) {
//
//                    }
//
//                    override fun onRotateEnd(detector: RotateGestureDetector) {
//                        Log.d("DSADASDASDASDASDa","${mapboxMap.cameraPosition.bearing}")
//                    }
//
//                })

                mapboxMap.addOnMapClickListener {
                    if (sharedPreferences.getBoolean(Constants.SETTINGS_DEMO_CAR, false)) {
                        addCar(it.latitude, it.longitude)
                    } else {

                        val position = CameraPosition.Builder()
                            .target(it)
                            .zoom(16.0)
                            .bearing(1.41)
                            .tilt(TILT_MAP)
                            .build()

                        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position), 1000)

                    }


                    true
                }


                mapboxMap.addOnRotateListener(object : MapboxMap.OnRotateListener {
                    override fun onRotateBegin(detector: RotateGestureDetector) {

                    }

                    override fun onRotate(detector: RotateGestureDetector) {

                    }

                    override fun onRotateEnd(detector: RotateGestureDetector) {

                    }
                })


                registerFirebaseReceiver()

                mainDisposables.add(presenter.getOngoingOrder())

                val lat = sharedPreferences.getFloat(Constants.LAST_KNOWN_LATITUDE, 0f)
                val lon = sharedPreferences.getFloat(Constants.LAST_KNOWN_LONGITUDE, 0f)

                if (lat != 0f) {
                    val position = CameraPosition.Builder()
                        .target(LatLng(lat.toDouble(), lon.toDouble()))
                        .zoom(16.0)
                        .tilt(TILT_MAP)
                        .build()

                    mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))
                } else {
                    val position = CameraPosition.Builder()
                        .target(LatLng(41.31122086155292, 69.27967758784646))
                        .zoom(16.0)
                        .tilt(TILT_MAP)
                        .build()

                    mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))
                }

//                mainDisposables.add(
//                    presenter.getAvailableService(
//                        42.461981894118885, 59.61993481176865
//                    )
//                )
//
//                val position = CameraPosition.Builder()
//                    .target(LatLng(42.461981894118885, 59.61993481176865))
//                    .zoom(16.0)
//                    .tilt(TILT_MAP)
//                    .build()
//
//                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))


                setUpMapButtons()

            }

            mapView.addOnDidFinishLoadingStyleListener {
                presenter.addCarImages()
            }


            var translationX = 0f
            var translationY = 0f

            var DIRECTION_CURRENT = 0



            mapboxMap.addOnMoveListener(object : MapboxMap.OnMoveListener {
                override fun onMoveBegin(detector: MoveGestureDetector) {
                    // user started moving the map
                    rvReady.isClickable = false
                    rvReady.setBackgroundResource(R.drawable.bc_button_purple_disabled)

                    cardGPS.animate().translationX(-500f).setDuration(400)
                        .setInterpolator(AccelerateInterpolator()).start()
//                    cardNext.animate().translationX(500f).setDuration(400).setInterpolator(AccelerateInterpolator()).start()


                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE) {
                        textViewHint.text = getString(R.string.start_point)
                        rvHint.animate().scaleY(1f).scaleX(1f).setDuration(200)
                            .setInterpolator(AnticipateOvershootInterpolator()).start()
                    } else if (CURRENT_MODE == Constants.MODE_DESTINATION_PICK) {
                        textViewHint.text = getString(R.string.end_point)
                        rvHint.animate().scaleY(1f).scaleX(1f).setDuration(200)
                            .setInterpolator(AnticipateOvershootInterpolator()).start()
                    }


                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE || CURRENT_MODE == Constants.MODE_DESTINATION_PICK || CURRENT_MODE == Constants.MODE_DESTINATION_PICK_STOP || CURRENT_MODE == Constants.MODE_DESTINATION_PICK_EDIT) {
                        rotatePointerRectangleAnimation()
                        imageViewCloudTop.animate().translationY(-200f).alpha(0.6f)
                            .setInterpolator(AccelerateInterpolator()).setDuration(800).start()

                        textViewDurationDriver.text = ""
                        textViewDurationMin.visibility = View.INVISIBLE

                        pointerRectangle.animate().scaleY(1f).scaleX(1f).setDuration(400)
                            .setInterpolator(OvershootInterpolator()).start()

                        textViewCurrentAddressDetails.text = ""
                        textViewCurrentAddress.text = getString(R.string.clarifying_address)
                        shimmer.start(textViewCurrentAddress)
                    }

                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE && rvNoService.visibility == View.GONE) {
                        if (bottomSheetBehaviour.state != BottomSheetBehavior.STATE_HIDDEN) {
                            PEEK_HEIGHT = bottomSheetBehaviour.peekHeight
                            bottomSheetBehaviour.setPeekHeight((PEEK_HEIGHT / 2.5f).toInt(), true)
                            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                        }

                    }

                }

                override fun onMove(detector: MoveGestureDetector) {
                    textViewDurationDriver.text = ""
                    textViewDurationMin.visibility = View.INVISIBLE
                    MAP_MOVING = true

                    // user is moving the map
                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE || CURRENT_MODE == Constants.MODE_DESTINATION_PICK) {

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

                    }

                }


                override fun onMoveEnd(detector: MoveGestureDetector) {

                    cardGPS.animate().translationX(0f).setDuration(200)
                        .setInterpolator(DecelerateInterpolator()).start()
//                    cardNext.animate().translationX(0f).setDuration(200).setInterpolator(DecelerateInterpolator()).start()


                    rvHint.animate().scaleY(0f).scaleX(0f).setDuration(300)
                        .setInterpolator(DecelerateInterpolator()).start()

                    MAP_MOVING = false
                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE) {
                        if (bottomSheetBehaviour.state != BottomSheetBehavior.STATE_HIDDEN) {
                            bottomSheetBehaviour.setPeekHeight(PEEK_HEIGHT, true)
                            bottomSheetBehaviour.isHideable = false
                            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                        }

                    }

                    imageViewCloudTop.animate().translationY(0f).alpha(1f)
                        .setInterpolator(DecelerateInterpolator()).setDuration(600).start()
                    // user stopped moving the map
                    RECTANGLE_ANIMATING = false
                    cancelPointerAnimation()
                    cancelPointerRectangleAnimation()

                    val mapLatLng = mapboxMap.cameraPosition.target
                    val mapLat = mapLatLng.latitude
                    val mapLong = mapLatLng.longitude

                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE || CURRENT_MODE == Constants.MODE_DESTINATION_PICK || CURRENT_MODE == Constants.MODE_DESTINATION_PICK_STOP || CURRENT_MODE == Constants.MODE_DESTINATION_PICK_ADDRESS || CURRENT_MODE == Constants.MODE_DESTINATION_PICK_EDIT)
                        mainDisposables.add(presenter.reverseGeocode(mapLat, mapLong))

                    when (CURRENT_MODE) {
                        Constants.MODE_SEARCH_WHERE -> {
                            ROUTE_DATA.clear()
                            START_POINT_LAT = mapLat
                            START_POINT_LON = mapLong

                        }
                        Constants.MODE_DESTINATION_PICK -> {
                            END_POINT_LAT = mapLat
                            END_POINT_LON = mapLong

                            ROUTE_DATA.clear()

                        }
                        Constants.MODE_DESTINATION_PICK_STOP -> {
                            END_POINT_LAT = mapLat
                            END_POINT_LON = mapLong

                        }

                        Constants.MODE_DESTINATION_PICK_EDIT -> {
                            EDIT_POINT_LAT = mapLat
                            EDIT_POINT_LON = mapLong

                        }

                        Constants.MODE_DESTINATION_PICK_START -> {
                            START_POINT_LAT = mapLat
                            START_POINT_LON = mapLong
                            mainDisposables.add(presenter.reverseGeocode(mapLat, mapLong))
                        }


                    }

                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE || CURRENT_MODE == Constants.MODE_DESTINATION_PICK) {

                        if (TARIFF_ID != 0L) {

                            if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE) {
                                when (PAYMENT_TYPE) {
                                    Constants.PAYMENT_TYPE_CASH -> {
                                        mainDisposables.add(
                                            presenter.getAvailableService(
                                                mapLat,
                                                mapLong,
                                                PaymentMethod(
                                                    Constants.PAYMENT_TYPE_CASH,
                                                    null,
                                                    null,
                                                    null
                                                )
                                            )
                                        )
                                    }
                                    Constants.PAYMENT_TYPE_CONTRACTOR -> {

                                        mainDisposables.add(
                                            presenter.getAvailableService(
                                                mapLat,
                                                mapLong,
                                                PaymentMethod(
                                                    Constants.PAYMENT_TYPE_CONTRACTOR,
                                                    PAYMENT_TYPE_ID,
                                                    null,
                                                    null
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    sendLocationToSocket()
                }
            })

        }

    }

    private fun setUpSocketForDrivers() {
        try {

            val options = IO.Options()
            options.forceNew = true
            options.transports = arrayOf(WebSocket.NAME)
            options.reconnectionAttempts = 3
            options.reconnection = true
            options.timeout = 2000
            options.path = "/apicl"


            val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

            IO.setDefaultOkHttpWebSocketFactory(okHttpClient)
            IO.setDefaultOkHttpCallFactory(okHttpClient)


            options.apply {
                callFactory = okHttpClient
                webSocketFactory = okHttpClient
            }

            mSocket = IO.socket("https://botmaker.uz/apicl", options)

            mSocket?.on(Socket.EVENT_CONNECT, Emitter.Listener {
                Log.d("DASDASDSADAS", "SOCKET CONNECTED")
                mSocket?.emit("message", "test")
            })?.on("message",
                Emitter.Listener { args -> println("Message : " + args[0]) })?.on(
                Socket.EVENT_DISCONNECT,
                Emitter.Listener { Log.d("DASDASDSADAS", "SOCKET DISCONNECT") })?.on(
                Socket.EVENT_CONNECT_ERROR,
                Emitter.Listener { args ->
                    Log.d(
                        "DASDASDSADAS",
                        "SOCKET ERROR CONNECTION ${args[0]}"
                    )
                })
            mSocket?.on("drivers", onNewMessage)
            mSocket?.connect()

        } catch (e: java.lang.Exception) {

        }

    }

    fun sendLocationToSocket() {


        val jsonObject = JSONObject()

        jsonObject.put("radius", 2000)
        jsonObject.put("lat", "${START_POINT_LAT}")
        jsonObject.put("lon", "${START_POINT_LON}")
        jsonObject.put("limit", 5)

        mSocket!!.emit("drivers", jsonObject)

    }

    private fun clearMovingCars() {
        movingCarPositions.clear()
        movingCarGeoJsonSources.clear()
        movingCarAnimations.clear()
        movingCarAnimationListeners.clear()

        movingCarRotations.clear()



        for (layer in mapBoxStyle.layers) {
            if (layer.id.startsWith("moving-layer"))
                mapBoxStyle.removeLayer(layer)
        }

        for (source in mapBoxStyle.sources) {
            if (source.id.startsWith("moving-source"))
                mapBoxStyle.removeSource(source)
        }

    }

    var OLD_SEARCH_POINT_LAT = 0.0
    var OLD_SEARCH_POINT_LON = 0.0

    private val onNewMessage: Emitter.Listener = object : Emitter.Listener {
        override fun call(vararg args: Any) {

            runOnUiThread(Runnable {

                val data = args[0] as JSONArray
                try {

                    val carsList = ArrayList<GetCarResponse>()

                    for (i in 0 until data.length()) {
                        val jsonObj = (data[i] as JSONObject)
                        val car = GetCarResponse(
                            jsonObj.getLong("workerId"),
                            RouteCoordinates(
                                jsonObj.getDouble("latitude"),
                                jsonObj.getDouble("longitude"),
                                null
                            )
                        )

                        carsList.add(car)
                    }
                    if (carsList.size > 0)
                        onCarsAvailabe(carsList)


                } catch (e: JSONException) {
                    return@Runnable
                }


            })
        }
    }


    lateinit var aroundCarsDisposable: Disposable

    private fun showCarsAround() {
//        aroundCarsDisposable = Observable.interval(
//            0, 2000,
//            TimeUnit.MILLISECONDS
//        )
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeOn(Schedulers.io())
//            .subscribe {
//                mainDisposables.add(
//                    presenter.getAvailableCars(
//                        START_POINT_LAT,
//                        START_POINT_LON,
//                        TARIFF_ID
//                    )
//                )
//            }
    }

    override fun onHasGPS() {
        enableLocationComponent(mapBoxStyle)
    }


    lateinit var receiver: BroadcastReceiver

    var ORDER_COST = 0.0

    private fun registerFirebaseReceiver() {
        val filter = IntentFilter()
        filter.addAction(Constants.ORDER_STATUS_RECIEVER)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.extras != null) {

                    val ORDER_ID = intent.extras!!.getLong(Constants.ORDER_ID)

                    when (intent.extras!!.get(Constants.ORDER_STATUS)) {
                        Constants.ORDER_STATUS_CREATED -> {
//                            showFoundCarInfo(ORDER_ID)
                        }
                        Constants.ORDER_STATUS_CHANGED -> {
                            showFoundCarInfo(ORDER_ID)
                        }
                        Constants.ORDER_STATUS_DRIVER_ASSIGNED -> {
                            CURRENT_MODE = Constants.MODE_CAR_FOUND
                            showFoundCarInfo(ORDER_ID)
                        }
                        Constants.ORDER_STATUS_DRIVER_DELAY -> {

                        }
                        Constants.ORDER_STATUS_DRIVER_ARRIVED -> {
                            CURRENT_MODE = Constants.MODE_DRIVER_CAME
//                            showDriverCameDialog(ORDER_ID)
//                            removeDriverRoute()
                        }
                        Constants.ORDER_STATUS_EXECUTING -> {
                            CURRENT_MODE = Constants.MODE_RIDE_STARTED
                            showFoundCarInfo(ORDER_ID)
                        }
                        Constants.ORDER_STATUS_DRIVER_UNASSIGNED -> {

                        }
                        Constants.ORDER_STATUS_ORDER_COMPLETED -> {

                        }
                        Constants.ORDER_STATUS_BONUS_ADDED -> {

                        }
                        Constants.ORDER_STATUS_BONUS_WITHDRAWN -> {

                        }
                        Constants.ORDER_STATUS_CANCELLED -> {

                        }
                        Constants.ORDER_STATUS_PAID_WAITING_BEGAN -> {

                        }
                        Constants.ORDER_STATUS_CHAT_REQUEST -> {

                        }

                    }

                }
            }
        }
        registerReceiver(receiver, filter)
    }

    fun showDriverCameDialog(orderID: Long) {

        val position = CameraPosition.Builder()
            .target(
                LatLng(
                    START_POINT_LAT,
                    START_POINT_LON
                )
            )
            .zoom(16.0)
            .build()
//
        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position), 1000)


        val dialog = AlertDialog.Builder(this).create()
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_driver_came, null)

        val rvGoing = view.findViewById<RelativeLayout>(R.id.rvGoing)

        rvGoing.setOnClickListener {
            mainDisposables.add(presenter.notifyDriver(orderID))
            dialog.dismiss()
        }

        dialog.setView(view)
        dialog.show()

    }

    override fun onNoGoingOrder() {
        requestCurrentLocation()
    }

    fun drawOnGoingOrderRoute(shortOrderInfo: ShortOrderInfo) {
        ROUTE_DATA.add(
            RouteItem(
                false,
                shortOrderInfo.route[0].name,
                shortOrderInfo.route[0].position!!.lat,
                shortOrderInfo.route[0].position!!.lon
            )
        )

        for ((index, destination) in shortOrderInfo.route.withIndex()) {
            if (index != 0 && index != shortOrderInfo.route.size - 1) {
                val view = LayoutInflater.from(this).inflate(
                    R.layout.item_destination,
                    linearDestinations,
                    false
                )
                view.findViewById<TextView>(R.id.textView1Address).text = destination.name
                linearDestinations.addView(view)
            }
        }

        ROUTE_DATA.add(
            RouteItem(
                false,
                shortOrderInfo.route[shortOrderInfo.route.size - 1].name,
                shortOrderInfo.route[shortOrderInfo.route.size - 1].position!!.lat,
                shortOrderInfo.route[shortOrderInfo.route.size - 1].position!!.lon
            )
        )


        val routeToDraw = ArrayList<RouteCoordinates>()

        for (rot in shortOrderInfo.route) {
            routeToDraw.add(
                RouteCoordinates(
                    rot.position!!.lat,
                    rot.position.lon,
                    "through"
                )
            )
        }

        mainDisposables.add(
            presenter.getRoute(
                routeToDraw,
                false
            )
        )
    }

    override fun onOnGoingOrderFound(shortOrderInfo: ShortOrderInfo) {

        bottomSheetBehaviour.isHideable = true
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN

        createOrderPositionDisposable(shortOrderInfo.id)

        drawOnGoingOrderRoute(shortOrderInfo)

        when (shortOrderInfo.state) {

            Constants.ORDER_STATE_CREATED -> {
                ORDER_STATE = Constants.ORDER_STATE_CREATED
                if (!ROUTE_DRAWN && shortOrderInfo.route.size > 1) {
//                    ROUTE_LIST.clear()
                    ROUTE_DATA.clear()


                }
                showCarSearchPage(shortOrderInfo.id)
            }
            Constants.ORDER_STATE_ASSIGNED -> {

                ORDER_STATE = Constants.ORDER_STATE_ASSIGNED
                CURRENT_MODE = Constants.MODE_CAR_FOUND
                showFoundCarInfo(shortOrderInfo.id)
            }
            Constants.ORDER_STATE_DRIVER_CAME -> {
                ORDER_STATE = Constants.ORDER_STATE_DRIVER_CAME
                CURRENT_MODE = Constants.MODE_DRIVER_CAME

                linearDestinations.removeAllViews()
                for ((index, destination) in shortOrderInfo.route.withIndex()) {
                    if (index != 0 && index != shortOrderInfo.route.size - 1) {
                        val view = LayoutInflater.from(this).inflate(
                            R.layout.item_destination,
                            linearDestinations,
                            false
                        )
                        view.findViewById<TextView>(R.id.textView1Address).text = destination.name
                        linearDestinations.addView(view)
                    }
                }

                showFoundCarInfo(shortOrderInfo.id)
                showDriverCameDialog(shortOrderInfo.id)
            }
            Constants.ORDER_STATE_EXECUTING -> {
                ORDER_STATE = Constants.ORDER_STATE_EXECUTING
                CURRENT_MODE = Constants.MODE_RIDE_STARTED
                showFoundCarInfo(shortOrderInfo.id)
            }
            Constants.ORDER_STATE_COMPLETED -> {
                ORDER_STATE = Constants.ORDER_STATE_COMPLETED
                carPositionDisposable.dispose()
                showFeedbackOrder(shortOrderInfo.id, ORDER_COST, 0.0)

            }
            Constants.ORDER_STATE_CANCELLED -> {
                ORDER_STATE = Constants.ORDER_STATE_CANCELLED
            }
            Constants.ORDER_STATE_BOOKED -> {
                ORDER_STATE = Constants.ORDER_STATE_BOOKED
            }
        }

    }

    var DRIVER_CAME_DIALOG_SHOWED = false
    var FEEDBACK_SHOWED = false

    fun onOnGoingOrderChange(oderID: Long, orderInfo: OrderInfo) {

        Log.d("DSADASDAS", "${orderInfo.state}")

        bottomSheetBehaviour.isHideable = true
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN

        rvCallDriver.setOnClickListener {
            if (orderInfo.assignee?.call?.numbers != null && orderInfo.assignee.call.numbers.size > 0) {
                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:" + "${orderInfo.assignee?.call?.numbers!![0]}")
                startActivity(dialIntent)
            } else {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.cannot_call_driver_now),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        when (orderInfo.state) {
            Constants.ORDER_STATE_CREATED -> {
                modeDriverFound.visibility = View.GONE
                modeRideStart.visibility = View.GONE
                showCarSearchPage(oderID)
                textSearching.text = getString(R.string.looking_for_drivers)
                shimmer = Shimmer()
                shimmer.start(textSearching)

                rvCancel.setOnClickListener {
                    textSearching.text = getString(R.string.to_cancel_order)
                    mainDisposables.add(presenter.cancelOrder(oderID))
                }

            }

            Constants.ORDER_STATE_ASSIGNED -> {
                mSocket?.disconnect()
                clearMovingCars()

                val carPointOLD = Location("pointOLD")
                carPointOLD.latitude = START_POINT_LAT
                carPointOLD.longitude = START_POINT_LON

                val carPointNEW = Location("pointNEW")

                if (orderInfo.assignee?.location != null) {
                    carPointNEW.latitude = orderInfo.assignee.location.lat
                    carPointNEW.longitude = orderInfo.assignee.location.lon

                    val distance = carPointOLD.distanceTo(carPointNEW)


                    val time = ((distance.toInt() * 3600) / 10000) / 60 + 1


                    textViewTimeCome.text = getString(R.string.driver_comes_within) + " $time ${
                        getString(
                            R.string.minute
                        )
                    }"
                }

                setUpCarImage(orderInfo)


                textViewCarName1.text =
                    "${orderInfo.assignee?.car?.model} - ${orderInfo.assignee?.car?.color}"
                textViewCarNumber1.text = "${orderInfo.assignee?.car?.regNum}"

                ROUTE_DATA.clear()


                var routeToDraw = ArrayList<RouteCoordinates>()

                for (rot in orderInfo.route) {
                    routeToDraw.add(
                        RouteCoordinates(
                            rot.address.position!!.lat,
                            rot.address.position.lon,
                            "through"
                        )
                    )
                }


                modeDriverFound.visibility = View.VISIBLE
                ORDER_STATE = Constants.ORDER_STATE_ASSIGNED
                CURRENT_MODE = Constants.MODE_CAR_FOUND
                if (lottieAnimation.visibility == View.VISIBLE)
                    showFoundCarInfo(oderID)
                if (!ROUTE_DRAWN && orderInfo.route.size > 1) {
                    mainDisposables.add(
                        presenter.getRoute(
                            routeToDraw,
                            false
                        )
                    )
                }
            }
            Constants.ORDER_STATE_DRIVER_CAME -> {
                textViewCarName1.text =
                    "${orderInfo.assignee?.car?.model} - ${orderInfo.assignee?.car?.color}"
                textViewCarNumber1.text = "${orderInfo.assignee?.car?.regNum}"

                modeDriverFound.visibility = View.VISIBLE
                val shimmer = Shimmer()
                shimmer.start(textViewTimeCome)
                textViewTimeCome.text = getString(R.string.driver_is_waiting_for_you)

                ORDER_STATE = Constants.ORDER_STATE_DRIVER_CAME
                CURRENT_MODE = Constants.MODE_DRIVER_CAME
                if (!DRIVER_CAME_DIALOG_SHOWED) {
                    showDriverCameDialog(oderID)
                    DRIVER_CAME_DIALOG_SHOWED = true
                }

                setUpCarImage(orderInfo)


            }
            Constants.ORDER_STATE_EXECUTING -> {

                textViewCarName.text =
                    "${orderInfo.assignee?.car?.model} - ${orderInfo.assignee?.car?.color}"
                textViewCarNumber.text = "${orderInfo.assignee?.car?.regNum}"

                val decimalFormat = DecimalFormat("###,###")

                textViewRate.text =
                    "${decimalFormat.format(ORDER_COST)} ${getString(R.string.currency)}"

                setUpCarImage(orderInfo)

                textView0Address.text = "${orderInfo.route[0].address.name}"
                if (orderInfo.route.size > 1)
                    textView1Address.text = "${orderInfo.route[1].address.name}"

                modeRideStart.visibility = View.VISIBLE

                ORDER_STATE = Constants.ORDER_STATE_EXECUTING
                CURRENT_MODE = Constants.MODE_RIDE_STARTED

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mapboxMap.locationComponent.isLocationComponentEnabled = false
                }

            }
            Constants.ORDER_STATE_COMPLETED -> {
                ORDER_STATE = Constants.ORDER_STATE_COMPLETED
                if (!FEEDBACK_SHOWED) {
                    showFeedbackOrder(oderID, orderInfo.cost.amount, orderInfo.usedBonuses ?: 0.0)
                    FEEDBACK_SHOWED = true
                    showSearchWherePage()
                    carPositionDisposable.dispose()
                }

            }
            Constants.ORDER_STATE_CANCELLED -> {
                ORDER_STATE = Constants.ORDER_STATE_CANCELLED
                carPositionDisposable.dispose()
                showSearchWherePage()
            }
            Constants.ORDER_STATE_BOOKED -> {
                ORDER_STATE = Constants.ORDER_STATE_BOOKED
            }
        }

    }

    fun setUpCarImage(orderInfo: OrderInfo) {
        if (orderInfo.assignee != null)
            when (orderInfo.assignee.car.alias) {
                Constants.CAR_ALIAS_NEXIA -> {
                    if (orderInfo.assignee.car.model == Constants.CAR_MODEL_NEXIA2_1 || orderInfo.assignee.car.model == Constants.CAR_MODEL_NEXIA2_2) {
                        imageViewAssignedCar.setImageResource(R.drawable.car_white_nexia2)
                        imageViewNexia.setImageResource(R.drawable.car_white_nexia2)
                    } else if (orderInfo.assignee.car.model == Constants.CAR_MODEL_NEXIA1_1 || orderInfo.assignee.car.model == Constants.CAR_MODEL_NEXIA1_2) {
                        imageViewAssignedCar.setImageResource(R.drawable.car_white_nexia1)
                        imageViewNexia.setImageResource(R.drawable.car_white_nexia1)
                    }
                }
                Constants.CAR_ALIAS_MALIBU -> {
                    if (orderInfo.assignee.car.model == Constants.CAR_MODEL_MALIBU1) {
                        imageViewAssignedCar.setImageResource(R.drawable.car_white_malibu1)
                        imageViewNexia.setImageResource(R.drawable.car_white_malibu1)
                    } else {
                        imageViewAssignedCar.setImageResource(R.drawable.car_white_malibu2)
                        imageViewNexia.setImageResource(R.drawable.car_white_malibu2)
                    }
                }
                Constants.CAR_ALIAS_LACETTI -> {
                    imageViewAssignedCar.setImageResource(R.drawable.lacetti)
                    imageViewNexia.setImageResource(R.drawable.lacetti)
                }
                Constants.CAR_ALIAS_MATIZ -> {
                    imageViewAssignedCar.setImageResource(R.drawable.matiz)
                    imageViewNexia.setImageResource(R.drawable.matiz)
                }
                Constants.CAR_ALIAS_SPARK -> {
                    imageViewAssignedCar.setImageResource(R.drawable.spark_2)
                    imageViewNexia.setImageResource(R.drawable.spark_2)
                }
                Constants.CAR_ALIAS_COBALT -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_cobalt)
                    imageViewNexia.setImageResource(R.drawable.car_white_cobalt)
                }
                Constants.CAR_ALIAS_EPICA -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_epica)
                    imageViewNexia.setImageResource(R.drawable.car_white_epica)
                }
                Constants.CAR_ALIAS_GENTRA -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_gentra)
                    imageViewNexia.setImageResource(R.drawable.car_white_gentra)
                }
                Constants.CAR_ALIAS_GRANTA -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_granta)
                    imageViewNexia.setImageResource(R.drawable.car_white_granta)
                }
                Constants.CAR_ALIAS_PRIORA -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_priora)
                    imageViewNexia.setImageResource(R.drawable.car_white_priora)
                }
                Constants.CAR_ALIAS_VESTA -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_vesta)
                    imageViewNexia.setImageResource(R.drawable.car_white_vesta)
                }
                Constants.CAR_ALIAS_XRAY -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_xray)
                    imageViewNexia.setImageResource(R.drawable.car_white_xray)
                }
                Constants.CAR_ALIAS_CAPTIVA -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_captiva)
                    imageViewNexia.setImageResource(R.drawable.car_white_captiva)
                }
                Constants.CAR_ALIAS_TOYOTA -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_cruiser)
                    imageViewNexia.setImageResource(R.drawable.car_white_cruiser)
                }
                Constants.CAR_ALIAS_ISUZU -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_isuzu)
                    imageViewNexia.setImageResource(R.drawable.car_white_isuzu)
                }
                Constants.CAR_ALIAS_CHRYSLER -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_300c)
                    imageViewNexia.setImageResource(R.drawable.car_white_300c)
                }
                Constants.CAR_ALIAS_ROLLSROYCE -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_rollsroyce)
                    imageViewNexia.setImageResource(R.drawable.car_white_rollsroyce)
                }
                Constants.CAR_ALIAS_LINCOLN -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_towncar)
                    imageViewNexia.setImageResource(R.drawable.car_white_towncar)
                }
                Constants.CAR_ALIAS_LAND_ROVER -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_land_rover)
                    imageViewNexia.setImageResource(R.drawable.car_white_land_rover)
                }
                Constants.CAR_ALIAS_CADILLAC -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_white_escalade)
                    imageViewNexia.setImageResource(R.drawable.car_white_escalade)
                }

                Constants.CAR_ALIAS_MERCEDES -> {
                    when (orderInfo.assignee.car.model) {
                        Constants.CAR_MODEL_W221 -> {
                            imageViewAssignedCar.setImageResource(R.drawable.car_white_merc221)
                            imageViewNexia.setImageResource(R.drawable.car_white_merc221)
                        }
                        Constants.CAR_MODEL_W220 -> {
                            imageViewAssignedCar.setImageResource(R.drawable.car_white_merc220)
                            imageViewNexia.setImageResource(R.drawable.car_white_merc220)
                        }
                        Constants.CAR_MODEL_GELIK -> {
                            imageViewAssignedCar.setImageResource(R.drawable.car_white_gelik)
                            imageViewNexia.setImageResource(R.drawable.car_white_gelik)
                        }
                        Constants.CAR_MODEL_VITO -> {
                            imageViewAssignedCar.setImageResource(R.drawable.car_black_vito)
                            imageViewNexia.setImageResource(R.drawable.car_black_vito)
                        }
                    }
                }


                Constants.CAR_ALIAS_RAVON -> {
                    when (orderInfo.assignee.car.model) {
                        Constants.CAR_MODEL_NEXIA3 -> {
                            imageViewAssignedCar.setImageResource(R.drawable.car_white_naxia3)
                            imageViewNexia.setImageResource(R.drawable.car_white_naxia3)
                        }
                        Constants.CAR_MODEL_COBALT -> {
                            imageViewAssignedCar.setImageResource(R.drawable.car_white_cobalt)
                            imageViewNexia.setImageResource(R.drawable.car_white_cobalt)
                        }
                        Constants.CAR_MODEL_SPARK -> {
                            imageViewAssignedCar.setImageResource(R.drawable.car_white_spark)
                            imageViewNexia.setImageResource(R.drawable.car_white_spark)
                        }
                        Constants.CAR_MODEL_GENTRA -> {
                            imageViewAssignedCar.setImageResource(R.drawable.car_white_gentra)
                            imageViewNexia.setImageResource(R.drawable.car_white_gentra)
                        }
                        Constants.CAR_MODEL_MATIZ -> {
                            imageViewAssignedCar.setImageResource(R.drawable.car_white_matiz)
                            imageViewNexia.setImageResource(R.drawable.car_white_matiz)
                        }


                    }
                }
                else -> {
                    imageViewAssignedCar.setImageResource(R.drawable.car_default)
                    imageViewNexia.setImageResource(R.drawable.car_default)
                }

            }
    }


    private fun showFoundCarInfo(orderID: Long) {
        lottieAnimation.cancelAnimation()
        lottieAnimation.visibility = View.GONE
        mSocket?.disconnect()
        clearMovingCars()

        if (CURRENT_MODE == Constants.MODE_CAR_FOUND || CURRENT_MODE == Constants.MODE_DRIVER_CAME)
            modeDriverFound.visibility = View.VISIBLE
        else if (CURRENT_MODE == Constants.MODE_RIDE_STARTED)
            modeRideStart.visibility = View.VISIBLE


        modeSearchCar.visibility = View.GONE
        cardGPS.visibility = View.GONE
//        cardNext.visibility = View.GONE

        rvCancelRide.setOnClickListener {
            mainDisposables.add(presenter.cancelOrder(orderID))
        }

        rcCancelOrder.setOnClickListener {
            mainDisposables.add(presenter.cancelOrder(orderID))
        }


    }

    var ROUTE_DRAWN = false

    var searchCancelListener: MainController.SearchCancelListener? = null

    override fun onSearchClicked(searchCancelListener: MainController.SearchCancelListener) {
        this.searchCancelListener = searchCancelListener
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
    }

    var WEATHER_READY = false

    override fun onWeatherReady(weather: ArrayList<WeatherType>, temperature: Double) {

        val showSeason = sharedPreferences.getBoolean(Constants.SETTINGS_WEATHER_ANIMATION, true)

        val currentTime: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val hour = currentTime.split(":")[0].toInt()


        if (weather.size > 0) {
            WEATHER_READY = true
            when (weather[0].id) {
                in 500..599 -> { //// WEATHER_RAIN
                    if (hour in 7..18) {
                        presenter.playLottie(lottieWeather, "day_rain.json", true, REVERSE = false)
                    } else {
                        presenter.playLottie(
                            lottieWeather,
                            "weather_rain.json",
                            true,
                            REVERSE = false
                        )
                    }
                    if (showSeason) {
                        presenter.playLottie(lottieSeason, "rain.json", true, REVERSE = false)
                        presenter.playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
                    }
                }
                in 600..699 -> {  //// WEATHER SNOW
                    if (hour in 7..18) {
                        presenter.playLottie(lottieWeather, "day_snow.json", true, REVERSE = false)
                    } else {
                        presenter.playLottie(
                            lottieWeather,
                            "weather_snow.json",
                            true,
                            REVERSE = false
                        )
                    }
                    if (showSeason) {
                        presenter.playLottie(lottieSeason, "winter.json", true, REVERSE = false)
                        presenter.playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
                    }
                }
                in 700..710 -> { //// MIST AND FOG
                    if (hour in 7..18) {
                        presenter.playLottie(
                            lottieWeather,
                            "day_cloudy.json",
                            true,
                            REVERSE = false
                        )
                    } else {
                        presenter.playLottie(
                            lottieWeather,
                            "weather_cloudy.json",
                            true,
                            REVERSE = false
                        )
                    }
                    if (showSeason)
                        presenter.playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
                }
                800 -> { /// WEATHER_CLEAR
                    if (hour in 7..18) {
                        presenter.playLottie(lottieWeather, "day_clear.json", true, REVERSE = false)
                    } else {
                        presenter.playLottie(
                            lottieWeather,
                            "weather_clear_sky.json",
                            true,
                            REVERSE = false
                        )
                    }
                }
                in 801..900 -> {  //// CLOUDS
                    if (hour in 7..18) {
                        presenter.playLottie(
                            lottieWeather,
                            "day_cloudy.json",
                            true,
                            REVERSE = false
                        )
                    } else {
                        presenter.playLottie(
                            lottieWeather,
                            "weather_cloudy.json",
                            true,
                            REVERSE = false
                        )
                    }
                }
            }
        }
//        presenter.playLottie(lottieSeason, "rain.json", true, REVERSE = false)
//        presenter.playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
        textViewTemperature.text = "${temperature.toInt()}"
    }

    var DELIVERY = false

    override fun onDestinationPickClicked(action: Int, delivery: Boolean) {
        DELIVERY = delivery
        showDestinationPickPage(action, 0.0, 0.0)
    }


    lateinit var routeLineSource: GeoJsonSource


    override fun onRouteAddClicked() {
        AddresSearchFragment().show(supportFragmentManager, "searchStopPoint")
    }

    override fun onChangeRouteLocationClicked(lat: Double, lon: Double) {
        val position = CameraPosition.Builder()
            .target(LatLng(lat, lon))
            .zoom(16.0)
            .tilt(TILT_MAP)
            .build()

        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))

        showDestinationPickPage(Constants.DESTINATION_PICK_EDIT, lat, lon)
    }

    override fun onRemoveRouteClicked(
        lat: Double,
        lon: Double,
        position: Int,
        removedItem: RouteItem
    ) {
        ROUTE_DATA.remove(removedItem)

        imageViewCloudTop.animate().scaleY(8f).setDuration(400).setInterpolator(
            AccelerateInterpolator()
        ).start()
        textViewDrawingAddress.visibility = View.VISIBLE
        shimmer.start(textViewDrawingAddress)

        val routeToDraw = ArrayList<RouteCoordinates>()
        val routePoints = ArrayList<RouteItem>()
        routePoints.addAll(ROUTE_DATA)
        routePoints.add(0, RouteItem(false, START_POINT_NAME, START_POINT_LAT, START_POINT_LON))
        for (rot in routePoints) {
            routeToDraw.add(RouteCoordinates(rot.lat, rot.lon, "through"))
        }

        mainDisposables.add(
            presenter.getRoute(
                routeToDraw,
                false
            )
        )

        recyclerViewRoute.adapter?.notifyItemRemoved(position)
    }

    var routeLineData = ArrayList<Point>()
    var routeCoordinateList = ArrayList<Point>()

    lateinit var TARIFFS : ArrayList<ServiceTariff>

    override fun drawRoute(route: ArrayList<Point>) {


        imageViewCloudTop.animate().scaleY(1f).setDuration(400).setInterpolator(
            AccelerateInterpolator()
        ).start()
        textViewDrawingAddress.visibility = View.GONE
        shimmer.cancel()

//        bottomSheetBehaviour.setPeekHeight(-500, true)
        bottomSheetBehaviour.isHideable = true
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN


        routeLineData.clear()
        routeCoordinateList.clear()
        routeCoordinateList.addAll(route)
        routeIndex = 0
        ROUTE_DRAWN = true




        if (ORDER_STATE == Constants.ORDER_STATE_NOT_CREATED) {

            recyclerViewRoute.layoutManager = LinearLayoutManager(this)

            recyclerViewRoute.adapter = RouteAdapter(ROUTE_DATA, this, this, DELIVERY)
            itemTouchHelper.attachToRecyclerView(recyclerViewRoute)



            textReady.visibility = View.VISIBLE
            progressReady.visibility = View.GONE

            bottomSheetBehaviour.isHideable = true
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN

            CURRENT_MODE = Constants.MODE_CREATE_ORDER
            rvReady.visibility = View.GONE
            rvReady.animate().translationY(100f).setDuration(200)
                .setInterpolator(AccelerateInterpolator()).start()

            modeCreateOrder.visibility = View.VISIBLE
            cardBack.visibility = View.VISIBLE
            cardViewShowRoute.visibility = View.VISIBLE

            cardGPS.visibility = View.GONE
//            cardNext.visibility = View.GONE
            imageViewPointerShadow.visibility = View.GONE
            pointerLayout.visibility = View.GONE
            textViewCurrentAddress.visibility = View.GONE
            textViewCurrentAddressDetails.visibility = View.GONE



            textViewStartAddress.text = START_POINT_NAME


            textViewStartAddress.setOnClickListener {
                SearchDialog(null, false).show(supportFragmentManager, "searchdialog")

            }

            if (DELIVERY) {
                setCarsAdapter(arrayListOf(DELIVERY_TARIFF))
            }else{
                if(::TARIFFS.isInitialized)
                setCarsAdapter(TARIFFS)
            }
            recyclerViewCars.postDelayed({
                if (recyclerViewCars.getChildAt(0) != null) {
                    recyclerViewCars.getChildAt(0)
                        .findViewById<RelativeLayout>(R.id.rvCar)
                        .performClick()
                } else {
                }
            }, 500)





            if (route.size > 0) {
                val routeLines = ArrayList<LatLng>()

                for (point in route) {
                    routeLines.add(LatLng(point.latitude(), point.longitude()))
                }

                val latLngBounds = LatLngBounds.Builder()
                    .includes(routeLines)
                    .build()
//                if (END_POINT_LAT != 0.0) {
//                    cardViewShowRoute.visibility = View.VISIBLE
                cardViewShowRoute.setOnClickListener {

                    mapboxMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            latLngBounds, 0
                        ), 600, object : MapboxMap.CancelableCallback {
                            override fun onCancel() {

                            }

                            override fun onFinish() {
                                mapboxMap.animateCamera(
                                    CameraUpdateFactory.zoomTo(mapboxMap.cameraPosition.zoom - 0.7),
                                    800
                                )

                            }

                        }
                    )
                }
                cardViewShowRoute.performClick()
//                }


            }


        }



        if (route.size > 0) {

            removeRoute()

            routeLineData.add(route[0])

            routeLineSource = GeoJsonSource(
                "line-source",
                FeatureCollection.fromFeatures(
                    arrayOf(
                        Feature.fromGeometry(
                            LineString.fromLngLats(routeLineData)
                        )
                    )
                ), GeoJsonOptions().withLineMetrics(true)
            )

            mapBoxStyle.addSource(routeLineSource)


            mapBoxStyle.addLayerBelow(
                LineLayer("line-layer", "line-source").withProperties(
//                lineDasharray(arrayOf(0.01f, 2f)),
                    lineCap(Property.LINE_CAP_ROUND),
                    lineJoin(Property.LINE_JOIN_ROUND),
                    lineWidth(7f),
                    lineColor(
                        Color.parseColor("#4CAF50")
                    )
                ), "road-label"
            )

            mapBoxStyle.addLayerBelow(
                LineLayer("behind-line-layer", "line-source").withProperties(
//              PropertyFactory.lineDasharray(arrayOf(0.01f, 2f)),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineRoundLimit(0.3f),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
//              PropertyFactory.lineDasharray(  arrayOf(0.4f,2f)),
                    PropertyFactory.lineWidth(15f),
                    PropertyFactory.lineOpacity(1f),
                    PropertyFactory.lineColor(
                        Color.parseColor("#3D8F40")
                    )

                ), "line-layer"
            )


            mapBoxStyle.addSource(
                GeoJsonSource(
                    "start-source", Feature.fromGeometry(
                        Point.fromLngLat(
                            route[0].longitude(),
                            route[0].latitude()
                        )
                    )
                )
            )

            val routePoints = ArrayList<RouteItem>()
            routePoints.addAll(ROUTE_DATA)
            routePoints.add(0, RouteItem(false, START_POINT_NAME, START_POINT_LAT, START_POINT_LON))

            for ((counter, rot) in routePoints.withIndex()) {
                if (counter != 0 && counter != routePoints.size - 1) {
                    mapBoxStyle.addSource(
                        GeoJsonSource(
                            "start-source-${counter}", Feature.fromGeometry(
                                Point.fromLngLat(
                                    rot.lon,
                                    rot.lat
                                )
                            )
                        )
                    )
                    mapBoxStyle.addLayer(
                        SymbolLayer(
                            "start-layer-${counter}",
                            "start-source-${counter}"
                        ).withProperties(
                            iconImage("stop-image"),
                            iconOffset(arrayOf(0f, -8f))
                        )
                    )
                }
            }




            mapBoxStyle.addSource(
                GeoJsonSource(
                    "finish-source-${route[route.size - 1].latitude()},${route[route.size - 1].longitude()}",
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            route[route.size - 1].longitude(),
                            route[route.size - 1].latitude()
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
                    "finish-layer-${route[route.size - 1].latitude()},${route[route.size - 1].longitude()}",
                    "finish-source-${route[route.size - 1].latitude()},${route[route.size - 1].longitude()}"
                ).withProperties(
                    iconImage("finish-image"),
                    iconOffset(arrayOf(0f, -8f))
                )
            )


            animate()
        }
    }

    private class PointEvaluator : TypeEvaluator<Point> {
        override fun evaluate(fraction: Float, startValue: Point, endValue: Point): Point {
            return Point.fromLngLat(
                startValue.longitude() + (endValue.longitude() - startValue.longitude()) * fraction,
                startValue.latitude() + (endValue.latitude() - startValue.latitude()) * fraction
            )
        }
    }

    var routeIndex = 0

    private var currentAnimator: Animator? = null
    private fun animate() {
// Check if we are at the end of the points list
        if (routeCoordinateList.size  > routeIndex) {
            val indexPoint: Point = routeCoordinateList.get(routeIndex)
            val newPoint = Point.fromLngLat(indexPoint.longitude(), indexPoint.latitude())
            currentAnimator = createLatLngAnimator(indexPoint, newPoint)
            currentAnimator?.start()
            routeIndex++
        }
    }

    private fun createLatLngAnimator(currentPosition: Point, targetPosition: Point): Animator? {
        val latLngAnimator =
            ValueAnimator.ofObject(PointEvaluator(), currentPosition, targetPosition)
        latLngAnimator.duration = TurfMeasurement.distance(
            currentPosition,
            targetPosition,
            "meters"
        ).toLong() / 20
        latLngAnimator.interpolator = AccelerateInterpolator()
        latLngAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animate()
            }
        })
        latLngAnimator.addUpdateListener { animation ->
            val point = animation.animatedValue as Point
            routeLineData.add(point)
            routeLineSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(routeLineData)))
        }
        return latLngAnimator
    }


    val paymentMethods = ArrayList<PaymentMethod>()

    lateinit var CHOSEN_PAYMENT_METHOD: PaymentMethod

    override fun onPaymentMethodsReady(paymentMethods: ArrayList<PaymentMethod>) {
        this.paymentMethods.clear()
        this.paymentMethods.addAll(paymentMethods)
        CHOSEN_PAYMENT_METHOD = paymentMethods[0]
        linearPaymentType.setOnClickListener {
            PaymentMethodsSheet(paymentMethods).show(supportFragmentManager, "payment")
        }
    }

    override fun onServiceNotAvailable() {
        if (bottomSheetBehaviour.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehaviour.isHideable = true
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
        }

//        cardNext.visibility = View.GONE
        rvNoService.visibility = View.VISIBLE

    }

    lateinit var DELIVERY_TARIFF: ServiceTariff


    override fun onTarrifTheSame() {
        bottomSheetBehaviour.setPeekHeight(PEEK_HEIGHT, true)
        bottomSheetBehaviour.isHideable = false
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
    }


    override fun onTariffsReady(tariffs: ArrayList<ServiceTariff>) {
        TARIFFS = tariffs
        bottomSheetBehaviour.setPeekHeight(PEEK_HEIGHT, true)
        bottomSheetBehaviour.isHideable = false
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED

        if (rvNoService.visibility == View.VISIBLE) {
            bottomSheetBehaviour.peekHeight = PEEK_HEIGHT
            bottomSheetBehaviour.isHideable = false
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
//            cardNext.visibility = View.VISIBLE
            rvNoService.visibility = View.GONE
        }
        tariffs.forEach {
            it.options.forEach { option ->
                if (option.name.toUpperCase() == "ДОСТАВКА") {
                    if (::searchFragment.isInitialized) {
                        searchFragment.rvDelivery.alpha = 1f
                        DELIVERY_TARIFF = it
                        rvDelivery.isClickable = true
                    }
                }
            }
        }

        if (TARIFF_ID != tariffs[0].id) {

            TARIFF_ID = tariffs[0].id
            recyclerViewCars.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )


            if (paymentMethods.isEmpty()) {
                presenter.getPaymentMethods(START_POINT_LAT, START_POINT_LON)
            }

            setCarsAdapter(tariffs)


        }

    }


    fun setCarsAdapter(tariffs: ArrayList<ServiceTariff>) {
        val decimalFormat = DecimalFormat("###,###")

        recyclerViewCars.adapter = CarsAdapter(this,
            tariffs,
            object : CarsAdapter.TariffClickListener {
                override fun onTariffChosen(
                    tariffID: Long,
                    shimmer: Shimmer,
                    textViewPrice: ShimmerTextView,
                    options: ArrayList<TariffOption>,
                    costChangeStep: Double
                ) {
                    COST_CHANGE_STEP = costChangeStep
                    COMMENT = ""
                    TARIF_OPTIONS.clear()

//                        val routePoints = ArrayList<RouteCoordinates>()
//                        routePoints.add(RouteCoordinates(START_POINT_LAT, START_POINT_LON, null))
//                        if (END_POINT_LAT != 0.0)
//                            routePoints.add(RouteCoordinates(END_POINT_LAT, END_POINT_LON, null))

                    val routeToDraw = ArrayList<RouteCoordinates>()


                    val routePoints = ArrayList<RouteItem>()
                    routePoints.addAll(ROUTE_DATA)
                    routePoints.add(
                        0, RouteItem(
                            false,
                            START_POINT_NAME,
                            START_POINT_LAT,
                            START_POINT_LON
                        )
                    )
                    for (rot in routePoints) {
                        routeToDraw.add(RouteCoordinates(rot.lat, rot.lon, "through"))
                    }

                    var price = 0.0

                    mainDisposables.add(
                        RetrofitHelper.apiService(Constants.BASE_URL)
                            .getEstimatedRide(
                                getCurrentLanguage().toLanguageTag(),
                                Constants.HIVE_PROFILE,
                                NaiveHmacSigner.DateSignature(),
                                NaiveHmacSigner.AuthSignature(
                                    HIVE_USER_ID,
                                    HIVE_TOKEN,
                                    "POST",
                                    "/api/client/mobile/2.0/estimate"
                                ),
                                EstimateRideRequest(
                                    tariffID,
                                    PaymentMethod(PAYMENT_TYPE, PAYMENT_TYPE_ID, null, null),
                                    arrayListOf(),
//                                        routePoints
                                    routeToDraw
                                )
                            )
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe({
                                if (it.isSuccessful) {
                                    shimmer.cancel()
                                    if (it.body() != null) {
                                        if (it.body()!!.cost.modifier != null) {
                                            linearDemand.visibility = View.VISIBLE
                                            shimmer.start(titanicText)
                                            lottieDemand.setAnimation("lottie_demand.json")
                                            lottieDemand.setMinAndMaxProgress(0.14f, 0.84f)
                                            lottieDemand.repeatCount = LottieValueAnimator.INFINITE
                                            lottieDemand.repeatMode = LottieDrawable.REVERSE
                                            lottieDemand.playAnimation()

                                        } else {
                                            shimmer.cancel()
                                            lottieDemand.cancelAnimation()
                                            linearDemand.visibility = View.GONE
                                        }

                                        price = it.body()!!.cost.amount
                                        textViewPrice.text =
                                            "${decimalFormat.format(it.body()!!.cost.amount)} ${
                                                getString(
                                                    R.string.currency
                                                )
                                            }"

                                        if (END_POINT_LAT != 0.0) {
                                            textViewDistance.visibility = View.VISIBLE
                                            textViewDistance.text = "${
                                                Constants.roundAvoid(
                                                    it.body()!!.distance / 1000,
                                                    2
                                                )
                                            } ${getString(R.string.km)}"
                                        }

                                    }
                                }
                            }, {

                            })
                    )

                    val optionChosenListener =
                        object : BottomSheetOrderFilter.OptionsChosenListener {
                            override fun onOptionsChosen(
                                comment: String,
                                options: ArrayList<Long>,
                                optionsValue: Double
                            ) {
                                OPTIONS_VALUE = optionsValue
                                TARIF_OPTIONS = options

                                COMMENT = comment

                                textViewPrice.text =
                                    "${decimalFormat.format(price + optionsValue)} ${getString(R.string.currency)}"

                            }
                        }

                    imageViewFilterCar.setOnClickListener {
                        BottomSheetOrderFilter(
                            options,
                            COMMENT,
                            TARIF_OPTIONS,
                            OPTIONS_VALUE,
                            optionChosenListener
                        ).show(
                            supportFragmentManager,
                            "filter"
                        )
                    }
                    rvOrder.setBackgroundResource(R.drawable.bc_button_purple)

                    rvOrder.setOnClickListener {

                        if (BONUS < 1000) {
                            createOrder(0.0, tariffID)
                        } else {

                            val useBonusSheet = UserBonusSheet(BONUS, tariffID)
                            useBonusSheet.show(supportFragmentManager, "bonus")


                        }


                    }

                }

                override fun onTarifReclicked(tariff: ServiceTariff) {
                        TariffInfoSheet(tariff).show(supportFragmentManager, "tariffInfo")
                }
            })
    }

    fun createOrder(bonus: Double, tariffID: Long) {

        if (progressOrder.visibility == View.GONE) {

            progressOrder.visibility = View.VISIBLE
            textOrder.visibility = View.GONE

            val routeORDER = ArrayList<ClientAddress>()
            routeORDER.add(
                ClientAddress(
                    SearchedAddress(
                        START_POINT_NAME, null, null,
                        SearchPosition(START_POINT_LAT, START_POINT_LON)
                    ), null, null, null, null
                )
            )
            if (END_POINT_LAT != 0.0) {
                routeORDER.add(
                    ClientAddress(
                        SearchedAddress(
                            END_POINT_NAME,
                            null,
                            null,
                            SearchPosition(END_POINT_LAT, END_POINT_LON)
                        ), null, null, null, null
                    )
                )
            }

            val createOrderRequest = CreateOrderRequest(
                PaymentMethod(PAYMENT_TYPE, PAYMENT_TYPE_ID, null, null),
                tariffID,
                TARIF_OPTIONS,
                routeORDER,
                null,
                COMMENT,
                null,
                bonus,
                null,
                null,
                true,
                null
            )

            mainDisposables.add(presenter.createOrder(createOrderRequest))

        }

    }

    var COMMENT = ""
    var TARIF_OPTIONS = ArrayList<Long>()
    var OPTIONS_VALUE = 0.0

    var PEEK_HEIGHT = 0

    fun showSearchWherePage() {
        DELIVERY = false

        cardBack.visibility = View.GONE

        shimmer.cancel()
        lottieDemand.cancelAnimation()
        linearDemand.visibility = View.GONE

        ROUTE_DATA.clear()

        END_POINT_LAT = 0.0
        END_POINT_LON = 0.0

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mapboxMap.locationComponent.isLocationComponentEnabled = true
        }

        removeRoute()
        removeDriverCar()

        imageViewPointerFoot.visibility = View.VISIBLE
        imageViewPointerShadow.visibility = View.VISIBLE
        pointerLayout.visibility = View.VISIBLE

        OPTIONS_VALUE = 0.0
        COMMENT = ""

        modeDriverFound.visibility = View.GONE
        modeRideStart.visibility = View.GONE
        modeSearchCar.visibility = View.GONE
        modeCreateOrder.visibility = View.GONE



        CURRENT_MODE = Constants.MODE_SEARCH_WHERE
        textViewCurrentAddress.visibility = View.VISIBLE
        textViewCurrentAddressDetails.visibility = View.VISIBLE

        bottomSheetBehaviour.isHideable = false
        bottomSheetBehaviour.peekHeight = PEEK_HEIGHT

        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        cardGPS.visibility = View.VISIBLE
//        cardNext.visibility = View.VISIBLE

        rvReady.visibility = View.GONE
        rvReady.animate().translationY(100f).setDuration(200)
            .setInterpolator(AccelerateInterpolator()).start()
        textViewDistance.visibility = View.GONE

        ROUTE_DRAWN = false


    }

    fun showDestinationPickPage(action: Int, lat: Double, lon: Double) {
        cardViewShowRoute.visibility = View.GONE
        END_POINT_NAME = ""
        rvReady.isClickable = false
        rvReady.setBackgroundResource(R.drawable.bc_button_purple_disabled)

        imageViewPointerFoot.visibility = View.VISIBLE

        if (CURRENT_MODE == Constants.MODE_CREATE_ORDER) {

            if (END_POINT_LAT != 0.0) {
                val position = CameraPosition.Builder()
                    .target(LatLng(END_POINT_LAT, END_POINT_LON))
                    .zoom(16.0)
                    .tilt(TILT_MAP)
                    .build()

                mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position), 1000)
            }

            rvOrder.setBackgroundResource(R.drawable.bc_button_purple_disabled)
            rvOrder.setOnClickListener(null)
//            removeRoute()
        }

        when (action) {

            Constants.DESTINATION_PICK_ORDEDR -> {
                CURRENT_MODE = Constants.MODE_DESTINATION_PICK
            }

            Constants.DESTINATION_PICK_STOP -> {
                CURRENT_MODE = Constants.MODE_DESTINATION_PICK_STOP
            }

            Constants.DESTINATION_PICK_ADDRESS -> {
                CURRENT_MODE = Constants.MODE_DESTINATION_PICK_ADDRESS
            }
            Constants.DESTINATION_PICK_EDIT -> {
                CURRENT_MODE = Constants.MODE_DESTINATION_PICK_EDIT
            }
            Constants.DESTINATION_PICK_START -> {
                rvOrder.setBackgroundResource(R.drawable.bc_button_purple_disabled)
                rvOrder.setOnClickListener(null)

                val position = CameraPosition.Builder()
                    .target(LatLng(START_POINT_LAT, START_POINT_LON))
                    .zoom(16.0)
                    .tilt(TILT_MAP)
                    .build()

                mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position), 1000)

                CURRENT_MODE = Constants.MODE_DESTINATION_PICK_START

            }


        }

        if (action != Constants.DESTINATION_PICK_START) {
            val mapLatLng = mapboxMap.cameraPosition.target
            val mapLat = mapLatLng.latitude
            val mapLong = mapLatLng.longitude

            val position = CameraPosition.Builder()
                .target(LatLng(mapLat + 0.0005, mapLong))
                .tilt(TILT_MAP)
                .build()

            mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position), 500)

        }

        bottomSheetBehaviour.isHideable = true
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
//        cardNext.visibility = View.GONE
        rvReady.visibility = View.VISIBLE
        rvReady.animate().translationY(0f).setDuration(200)
            .setInterpolator(DecelerateInterpolator()).start()
        imageViewPointerFoot.visibility = View.VISIBLE
        textViewDistance.visibility = View.GONE

        rvReady.setOnClickListener {


            when (action) {

                Constants.DESTINATION_PICK_ORDEDR -> {
//                    ROUTE_DATA.add(RouteItem(false,END_POINT_NAME,END_POINT_LAT,END_POINT_LON))
                    ROUTE_DATA.add(RouteItem(false, END_POINT_NAME, END_POINT_LAT, END_POINT_LON))
                    if (progressReady.visibility != View.VISIBLE) {

                        val routeToDraw = ArrayList<RouteCoordinates>()
                        val routePoints = ArrayList<RouteItem>()
                        routePoints.addAll(ROUTE_DATA)
                        routePoints.add(
                            0, RouteItem(
                                false,
                                START_POINT_NAME,
                                START_POINT_LAT,
                                START_POINT_LON
                            )
                        )
                        for (rot in routePoints) {
                            routeToDraw.add(RouteCoordinates(rot.lat, rot.lon, "through"))
                        }

                        textReady.visibility = View.GONE
                        progressReady.visibility = View.VISIBLE
                        mainDisposables.add(
                            presenter.getRoute(
                                routeToDraw,
                                false
                            )
                        )
                    }

                }

                Constants.DESTINATION_PICK_STOP -> {
                    ROUTE_DATA.add(RouteItem(false, END_POINT_NAME, END_POINT_LAT, END_POINT_LON))

                    recyclerViewRoute.adapter?.notifyDataSetChanged()

                    if (progressReady.visibility != View.VISIBLE) {
                        textReady.visibility = View.GONE
                        progressReady.visibility = View.VISIBLE

                        val routeToDraw = ArrayList<RouteCoordinates>()

                        val routePoints = ArrayList<RouteItem>()
                        routePoints.addAll(ROUTE_DATA)
                        routePoints.add(
                            0, RouteItem(
                                false,
                                START_POINT_NAME,
                                START_POINT_LAT,
                                START_POINT_LON
                            )
                        )
                        for (rot in routePoints) {
                            routeToDraw.add(RouteCoordinates(rot.lat, rot.lon, "through"))
                        }

                        mainDisposables.add(
                            presenter.getRoute(
                                routeToDraw,
                                false
                            )
                        )

                    }

                }
                Constants.DESTINATION_PICK_EDIT -> {
                    var REMOVE_INDEX = 0
                    for ((index, rot) in ROUTE_DATA.withIndex()) {
                        if (rot.lat == lat && rot.lon == lon) {
                            REMOVE_INDEX = index
                        }
                    }
                    ROUTE_DATA.removeAt(REMOVE_INDEX)
                    ROUTE_DATA.add(
                        REMOVE_INDEX,
                        RouteItem(false, EDIT_POINT_NAME, EDIT_POINT_LAT, EDIT_POINT_LON)
                    )
                    recyclerViewRoute.adapter?.notifyDataSetChanged()

                    if (progressReady.visibility != View.VISIBLE) {
                        textReady.visibility = View.GONE
                        progressReady.visibility = View.VISIBLE

                        val routeToDraw = ArrayList<RouteCoordinates>()

                        val routePoints = ArrayList<RouteItem>()
                        routePoints.addAll(ROUTE_DATA)
                        routePoints.add(
                            0, RouteItem(
                                false,
                                START_POINT_NAME,
                                START_POINT_LAT,
                                START_POINT_LON
                            )
                        )
                        for (rot in routePoints) {
                            routeToDraw.add(RouteCoordinates(rot.lat, rot.lon, "through"))
                        }

                        mainDisposables.add(
                            presenter.getRoute(
                                routeToDraw,
                                false
                            )
                        )

                    }
                }
                Constants.DESTINATION_PICK_START -> {
                    textViewStartAddress.text = START_POINT_NAME

                    val routeToDraw = ArrayList<RouteCoordinates>()

                    val routePoints = ArrayList<RouteItem>()
                    routePoints.addAll(ROUTE_DATA)
                    routePoints.add(
                        0,
                        RouteItem(false, START_POINT_NAME, START_POINT_LAT, START_POINT_LON)
                    )
                    for (rot in routePoints) {
                        routeToDraw.add(RouteCoordinates(rot.lat, rot.lon, "through"))
                    }

                    mainDisposables.add(
                        presenter.getRoute(
                            routeToDraw,
                            false
                        )
                    )
                }

                Constants.DESTINATION_PICK_ADDRESS -> {


                }

            }


        }
        rvReady.isClickable = false


        ROUTE_DRAWN = false


        modeCreateOrder.visibility = View.GONE
//        cardBack.visibility = View.GONE

        cardGPS.visibility = View.VISIBLE
//        cardNext.visibility = View.GONE
        imageViewPointerShadow.visibility = View.VISIBLE
        pointerLayout.visibility = View.VISIBLE
        textViewCurrentAddress.visibility = View.VISIBLE
        textViewCurrentAddressDetails.visibility = View.VISIBLE

    }

    override fun onErrorGetRoute() {
        textReady.visibility = View.VISIBLE
        progressReady.visibility = View.GONE

    }

    private fun removeRoute() {
//        cardViewShowRoute.visibility = View.GONE
        for (layer in mapBoxStyle.layers) {
            if (layer.id.startsWith("line-") || layer.id.startsWith("start-") || layer.id.startsWith(
                    "finish-"
                ) || layer.id.startsWith("behind-")
            )
                mapBoxStyle.removeLayer(layer)
        }

        for (source in mapBoxStyle.sources) {
            if (source.id.startsWith("line-") || source.id.startsWith("start-") || source.id.startsWith(
                    "finish-"
                )
            )
                mapBoxStyle.removeSource(source)
        }

    }

    private fun removeDriverRoute() {
        for (layer in mapBoxStyle.layers) {
            if (layer.id.startsWith("drive-"))
                mapBoxStyle.removeLayer(layer)
        }

        for (source in mapBoxStyle.sources) {
            if (source.id.startsWith("drive-"))
                mapBoxStyle.removeSource(source)
        }

    }

    private fun removeDriverCar() {
        for (layer in mapBoxStyle.layers) {
            if (layer.id.startsWith("demo-"))
                mapBoxStyle.removeLayer(layer)
        }

        for (source in mapBoxStyle.sources) {
            if (source.id.startsWith("demo-"))
                mapBoxStyle.removeSource(source)
        }
    }


    fun showCarSearchPage(orderID: Long) {
        cardViewShowRoute.visibility = View.GONE

        CURRENT_MODE = Constants.MODE_CAR_SEARCH

        modeCreateOrder.visibility = View.GONE
        cardBack.visibility = View.GONE
        cardViewShowRoute.visibility = View.GONE

        modeSearchCar.visibility = View.VISIBLE
        pointerLayout.visibility = View.VISIBLE
        imageViewPointerFoot.visibility = View.GONE
        imageViewPointerShadow.visibility = View.GONE

        lottieAnimation.visibility = View.VISIBLE
        lottieAnimation.playAnimation()
        shimmer = Shimmer()
        shimmer.start(textSearching)

        rvCancel.setOnClickListener {
            textSearching.text = getString(R.string.to_cancel_order)
            mainDisposables.add(presenter.cancelOrder(orderID))
        }

    }

    lateinit var carPositionDisposable: Disposable

    override fun onOrderCreated(orderID: Long) {

        showCarSearchPage(orderID)
        textSearching.text = getString(R.string.looking_for_drivers)
        shimmer = Shimmer()
        shimmer.start(textSearching)

        rvCancel.setOnClickListener {
            textSearching.text = getString(R.string.to_cancel_order)
            mainDisposables.add(presenter.cancelOrder(orderID))
        }

        createOrderPositionDisposable(orderID)

    }

    var COST_CHANGE_DIALOG_SHOWN = false

    private fun createOrderPositionDisposable(orderID: Long) {
        if (!::carPositionDisposable.isInitialized)
            carPositionDisposable = Observable.interval(
                0, 1500,
                TimeUnit.MILLISECONDS
            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    RetrofitHelper.apiService(Constants.BASE_URL)
                        .getOrderDetails(
                            getCurrentLanguage().toLanguageTag(),
                            Constants.HIVE_PROFILE,
                            NaiveHmacSigner.DateSignature(),
                            NaiveHmacSigner.AuthSignature(
                                HIVE_USER_ID,
                                HIVE_TOKEN,
                                "GET",
                                "/api/client/mobile/2.2/orders/$orderID"
                            ),
                            orderID
                        )
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({

                            if (it.isSuccessful && it.body() != null) {

                                if (it.body()!!.needsProlongation != null && it.body()!!.needsProlongation!!) {

                                    if (it.body()!!.cost.fixed == null && !COST_CHANGE_DIALOG_SHOWN) {
                                        COST_CHANGE_DIALOG_SHOWN = true
                                        val dialog = AlertDialog.Builder(this).create()
                                        val view = LayoutInflater.from(this)
                                            .inflate(R.layout.dialog_costchange, null)

                                        val textYes = view.findViewById<TextView>(R.id.textYes)
                                        val textNo = view.findViewById<TextView>(R.id.textNo)
                                        val textViewCostChange =
                                            view.findViewById<TextView>(R.id.textCostChange)

                                        textViewCostChange.text =
                                            "Xaydovchilarni etiborini tortish maqsadida buyurtmangiz narxiga qo'shimcha ${COST_CHANGE_STEP.toInt()} so'm qo'sha olasizmi?"
                                        textYes.setOnClickListener { view ->
                                            dialog.dismiss()
                                            mainDisposables.add(
                                                RetrofitHelper.apiService(Constants.BASE_URL)
                                                    .fixOrderCost(
                                                        getCurrentLanguage().toLanguageTag(),
                                                        Constants.HIVE_PROFILE,
                                                        NaiveHmacSigner.DateSignature(),
                                                        NaiveHmacSigner.AuthSignature(
                                                            HIVE_USER_ID,
                                                            HIVE_TOKEN,
                                                            "GET",
                                                            "/api/client/mobile/1.0/orders/$orderID/fix-cost"
                                                        ),
                                                        orderID,
                                                        it.body()!!.cost.amount + COST_CHANGE_STEP
                                                    )
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribeOn(Schedulers.io())
                                                    .subscribe({ fix ->

                                                    }, {

                                                    })
                                            )
                                        }

                                        textNo.setOnClickListener {
                                            dialog.dismiss()
                                            mainDisposables.add(presenter.cancelOrder(orderID))
                                        }

                                        dialog.setView(view)
                                        dialog.show()
                                    }
                                }

                                onOnGoingOrderChange(orderID, it.body()!!)


                                if (it.body()!!.route[0].address.position != null) {
                                    START_POINT_LAT = it.body()!!.route[0].address.position!!.lat
                                    START_POINT_LON = it.body()!!.route[0].address.position!!.lon

                                    if (it.body()!!.assignee?.location != null) {
                                        addCar(
                                            it.body()!!.assignee?.location!!.lat,
                                            it.body()!!.assignee?.location!!.lon
                                        )
                                    }
                                }


                            }

                            ORDER_COST = if (it.body()!!.cost.fixed == null) {
                                it.body()!!.cost.amount
                            } else {
                                it.body()!!.cost.fixed!!
                            }

                        }, {

                        })

                },
                    {

                    }
                )
    }

    override fun onOrderCancelled() {
        modeSearchCar.visibility = View.GONE
        modeDriverFound.visibility = View.GONE
        modeRideStart.visibility = View.GONE
        modeSearchCar.visibility = View.GONE
        removeDriverRoute()
        removeDriverCar()

        if (::carPositionDisposable.isInitialized)
            carPositionDisposable.dispose()

        if (END_POINT_LAT != 0.0) {
            CURRENT_MODE = Constants.MODE_CREATE_ORDER

            modeCreateOrder.visibility = View.VISIBLE
            cardBack.visibility = View.VISIBLE
            cardViewShowRoute.visibility = View.VISIBLE
            pointerLayout.visibility = View.GONE
            imageViewPointerFoot.visibility = View.GONE
            imageViewPointerShadow.visibility = View.GONE
        } else {
            CURRENT_MODE = Constants.MODE_SEARCH_WHERE
            imageViewPointerFoot.visibility = View.VISIBLE
            imageViewPointerShadow.visibility = View.VISIBLE
            pointerLayout.visibility = View.VISIBLE
            showSearchWherePage()
        }

        lottieAnimation.visibility = View.GONE
        lottieAnimation.cancelAnimation()

        if (::shimmer.isInitialized) {
            shimmer.cancel()
        }
        progressOrder.visibility = View.GONE
        textOrder.visibility = View.VISIBLE

    }


    override fun onErrorCreateOrder() {
        progressOrder.visibility = View.GONE
        textOrder.visibility = View.VISIBLE
    }


    var movingCarPositions = HashMap<Long, LatLng>()
    var movingCarGeoJsonSources = HashMap<Long, GeoJsonSource>()
    var movingCarAnimations = HashMap<Long, ValueAnimator>()
    var movingCarAnimationListeners = HashMap<Long, AnimatorUpdateListener>()

    var movingCarRotations = HashMap<Long, Float>()

    fun addMovingCar(car: GetCarResponse) {

        if (movingCarGeoJsonSources[car.id] == null) {


            movingCarPositions[car.id] = LatLng(car.location.lat, car.location.lon)

            movingCarGeoJsonSources[car.id] = GeoJsonSource(
                "moving-source-${car.id}",
                Feature.fromGeometry(
                    Point.fromLngLat(
                        car.location.lat, car.location.lon
                    )
                )
            )


            if (mapBoxStyle.getSource("moving-source-${car.id}") == null)
                mapBoxStyle.addSource(movingCarGeoJsonSources[car.id]!!)


            val carLayer = SymbolLayer("moving-layer-${car.id}", "moving-source-${car.id}")
                .withProperties(
                    iconImage(Constants.getRandomIcon()),
//                    iconImage("fleet-0"),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true),
                    iconKeepUpright(true)
                )

            mapBoxStyle.addLayer(
                carLayer
            )

        } else {


            if (movingCarAnimations[car.id] != null) {
                val anim = movingCarAnimations[car.id]
                anim!!.removeAllUpdateListeners()
                anim.removeAllListeners()
                anim.cancel()
                moveMovingCar(car)
            } else if (animator == null) {
                moveMovingCar(car)
            }
        }

    }


    fun moveMovingCar(car: GetCarResponse) {

        val OLD_ROTATION = movingCarRotations[car.id] ?: 0f

        val carPointOLD = Location("movingCarPointOLD")
        val oldCarPosition = movingCarPositions[car.id]

        carPointOLD.latitude = oldCarPosition!!.latitude
        carPointOLD.longitude = oldCarPosition.longitude


        val carPointNEW = Location("movingCarPointNEW")
        carPointNEW.latitude = car.location.lat
        carPointNEW.longitude = car.location.lon

        val distance = carPointOLD.distanceTo(carPointNEW)

        if (distance > 5) {

            var rotation = Constants.getRotation(
                movingCarPositions[car.id]!!,
                LatLng(car.location.lat, car.location.lon)
            )

            if (!rotation.isNaN()) {

                if (kotlin.math.abs(rotation - OLD_ROTATION) > 180) {
                    rotation -= 360
                }

                val iconSpinningAnimator = ValueAnimator.ofFloat(OLD_ROTATION, rotation)
                iconSpinningAnimator.duration = 600
                iconSpinningAnimator.interpolator = LinearInterpolator()

                iconSpinningAnimator.addUpdateListener { valueAnimator -> // Retrieve the new animation number to use as the map camera bearing value
                    var newIconRotateValue = valueAnimator.animatedValue as Float
                    movingCarRotations[car.id] = newIconRotateValue

                    if (newIconRotateValue < 0)
                        newIconRotateValue += 360

                    val rotateFormatter = DecimalFormat("#.#")

                    mapboxMap.getStyle { style ->
                        val iconSymbolLayer: Layer? = style.getLayerAs("moving-layer-${car.id}")
                        iconSymbolLayer?.setProperties(
                            iconImage(
                                Constants.getCarIcon(
                                    kotlin.math.abs(
                                        rotateFormatter.format(
                                            newIconRotateValue
                                        ).replace(",", ".").toFloat()
                                    )
                                )
                            )
                        )
                    }
                }

                iconSpinningAnimator!!.start()
            }
        }

        val animatorUpdateListener =
            AnimatorUpdateListener { valueAnimator ->
                val animatedPosition = valueAnimator.animatedValue as LatLng
                movingCarPositions[car.id] = animatedPosition
                try {
                    (mapBoxStyle.getSource("moving-source-${car.id}") as GeoJsonSource).setGeoJson(
                        Point.fromLngLat(
                            animatedPosition.longitude,
                            animatedPosition.latitude
                        )
                    )
                } catch (ex: Exception) {
                    valueAnimator.cancel()
                }

            }
        movingCarAnimationListeners[car.id] = animatorUpdateListener

        val latLngEvaluator: TypeEvaluator<LatLng> = object : TypeEvaluator<LatLng> {
            private val latLng = LatLng()
            override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
                latLng.latitude = (startValue.latitude
                        + (endValue.latitude - startValue.latitude) * fraction)
                latLng.longitude = (startValue.longitude
                        + (endValue.longitude - startValue.longitude) * fraction)
                return latLng
            }
        }

        val anim = ObjectAnimator
            .ofObject(
                latLngEvaluator,
                movingCarPositions[car.id],
                LatLng(car.location.lat, car.location.lon)
            )
            .setDuration(10000)


        anim.interpolator = LinearInterpolator()

        anim.addUpdateListener(movingCarAnimationListeners[car.id])
        movingCarAnimations[car.id] = anim
        movingCarAnimations[car.id]?.start()


        movingCarPositions[car.id] = LatLng(car.location.lat, car.location.lon)


    }

    override fun onCarsAvailabe(data: ArrayList<GetCarResponse>) {
        val ids = ArrayList<Long>()

        for (car in data) {
            ids.add(car.id)
        }

        val carFirst = data[0]

        val carPointOLD = Location("pointOLD")
        carPointOLD.latitude = START_POINT_LAT
        carPointOLD.longitude = START_POINT_LON

        val carPointNEW = Location("pointNEW")
        carPointNEW.latitude = carFirst.location.lat
        carPointNEW.longitude = carFirst.location.lon

        val distance = carPointOLD.distanceTo(carPointNEW)


        val time = ((distance.toInt() * 3600) / 10000) / 60 + 1

        if (!MAP_MOVING) {
            pointerRectangle.animate().scaleY(3f).scaleX(3f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {

                    }

                    override fun onAnimationEnd(animation: Animator?) {

                    }

                    override fun onAnimationCancel(animation: Animator?) {

                    }

                    override fun onAnimationRepeat(animation: Animator?) {

                    }

                })
                .setDuration(400).setInterpolator(AccelerateInterpolator()).start()


            textViewDurationDriver.postDelayed({
                textViewDurationDriver.text = "${time}"
                textViewDurationMin.visibility = View.VISIBLE
            }, 500)
        } else {
            textViewDurationDriver.text = ""
            textViewDurationMin.visibility = View.INVISIBLE
        }

        val iterator = movingCarGeoJsonSources.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (!ids.contains(item.key)) {
                iterator.remove()
                mapBoxStyle.removeLayer("moving-layer-${item.key}")
                mapBoxStyle.removeSource("moving-source-${item.key}")
                val anim = movingCarAnimations[item.key]
                anim?.removeAllUpdateListeners()
                anim?.removeAllListeners()
                anim?.cancel()
                movingCarAnimations.remove(item.key)
                movingCarAnimationListeners.remove(item.key)
            }
        }

        for (car in data) {
            addMovingCar(car)
        }

    }


    override fun onAddAddressClicked() {

        val width = 380
        val height = 380

        val options = MapSnapshotter.Options(width, height)
            .withStyle(mapBoxStyle.uri)
            .withCameraPosition(mapboxMap.cameraPosition)
            .withPixelRatio(2f)
        MapSnapshotter(this, options).start(
            { snapshot ->
                if (snapshot != null)
                    AddAddressSheet(snapshot.bitmap, object : AddAddressSheet.OnAddressAddListener {
                        override fun onAddClicked(
                            addressName: String,
                            alias: Int,
                            instuctions: String,
                            dialog: BottomSheetDialogFragment
                        ) {
                            var entity = AddressEntity(
                                0,
                                START_POINT_LAT,
                                START_POINT_LON,
                                addressName, alias, "",
                                instuctions,
                                Constants.getCurrentTime(),
                                0
                            )
                            mainDisposables.add(
                                (application as UzTaxiApplication).uzTaxiDatabase
                                    .getAddressDAO().insertAddress(entity)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe({
                                        entity.position = it
                                        mainDisposables.add(
                                            (application as UzTaxiApplication).uzTaxiDatabase
                                                .getAddressDAO().updateAddress(it.toInt(), it)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeOn(Schedulers.io())
                                                .subscribe({
                                                    if (searchCancelListener != null && searchCancelListener is SearchFragment)
                                                        (searchCancelListener as SearchFragment).loadSavedAdresses()
                                                    dialog.dismiss()
                                                }, {})
                                        )
                                    }, {
                                    })
                            )
                        }
                    }).show(supportFragmentManager, "addaddress")
            }
        ) { onAddAddressClicked() }


    }

    fun onStartPointChanged(latitude: Double, longitude: Double, title: String) {
        textViewStartAddress.text = title
        START_POINT_NAME = title
        START_POINT_LAT = latitude
        START_POINT_LON = longitude

        val routeToDraw = ArrayList<RouteCoordinates>()

        val routePoints = ArrayList<RouteItem>()
        routePoints.addAll(ROUTE_DATA)
        routePoints.add(0, RouteItem(false, START_POINT_NAME, START_POINT_LAT, START_POINT_LON))
        for (rot in routePoints) {
            routeToDraw.add(RouteCoordinates(rot.lat, rot.lon, "through"))
        }

        mainDisposables.add(
            presenter.getRoute(
                routeToDraw,
                false
            )
        )

    }

    override fun onBottomSheetSearchItemClicked(
        latitude: Double,
        longitude: Double,
        title: String,
        delivery: Boolean
    ) {
        DELIVERY = delivery

        hideKeyboard()

        END_POINT_NAME = title
        END_POINT_LAT = latitude
        END_POINT_LON = longitude

        ROUTE_DATA.add(RouteItem(false, title, latitude, longitude))

        val routeToDraw = ArrayList<RouteCoordinates>()

        val routePoints = ArrayList<RouteItem>()
        routePoints.addAll(ROUTE_DATA)
        routePoints.add(0, RouteItem(false, START_POINT_NAME, START_POINT_LAT, START_POINT_LON))
        for (rot in routePoints) {
            routeToDraw.add(RouteCoordinates(rot.lat, rot.lon, "through"))
        }

        mainDisposables.add(
            presenter.getRoute(
                routeToDraw,
                false
            )
        )

    }

    lateinit var searchFragment: SearchFragment

    fun setUpBottomSheet() {


        bottomSheetBehaviour = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehaviour.isFitToContents = false
        bottomSheetBehaviour.halfExpandedRatio = 0.45f

        PEEK_HEIGHT = bottomSheetBehaviour.peekHeight

        searchFragment = SearchFragment()
        searchCancelListener = searchFragment
        changeBottomSheet(searchFragment, false)

        cardBack.setOnClickListener {
            onBackPressed()
        }



        bottomSheetBehaviour.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (searchCancelListener != null) {
                    if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        hideKeyboard()
                        searchCancelListener!!.onSearchCancel()
                    } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        searchCancelListener!!.onSearchStart()
                    }
                }

            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                if (slideOffset > 0.3) {
                    cardGPS.alpha = 0.6f - slideOffset
//                    cardNext.alpha = 0.6f - slideOffset


                } else {
                    cardGPS.alpha = 1f
//                    cardNext.alpha = 1f

                }
            }
        })

    }

    fun onNextClicked() {

        drawRoute(arrayListOf())

    }

    override fun onAddressFound(name: String, details: String) {

        try {
            textViewCurrentAddress.text = "${name.toInt()}${getString(R.string.house)}"
        } catch (e: java.lang.Exception) {

            textViewCurrentAddress.text = name
            textViewCurrentAddressDetails.text = details
        }
        if (details.isEmpty()) {
            if (name.split(",").size > 1) {

                val newName = name.split(",")[0]
                try {
                    try {
                        val houseNumber = newName.substring(0, 1).toInt()
                        textViewCurrentAddress.text = "${newName}${getString(R.string.house)}"
                    } catch (e: java.lang.Exception) {
                        textViewCurrentAddress.text =
                            "${newName.toInt()}${getString(R.string.house)}"
                    }
                } catch (exception: java.lang.Exception) {
                    textViewCurrentAddress.text = name.split(",")[0]
                }


                textViewCurrentAddressDetails.text = name.split(",")[1]
            }
        } else {
            textViewCurrentAddressDetails.text = details
        }



        shimmer.cancel()

        when (CURRENT_MODE) {
            Constants.MODE_SEARCH_WHERE -> {
                START_POINT_NAME = if (details != "") details else name
            }
            Constants.MODE_DESTINATION_PICK -> {
                END_POINT_NAME = if (details != "") details else name
                rvReady.isClickable = true
                rvReady.setBackgroundResource(R.drawable.bc_button_purple)


//                ROUTE_DATA.add(RouteItem(false,END_POINT_NAME,END_POINT_LAT,END_POINT_LON))
//                textViewDestination.text = END_POINT_NAME
            }
            Constants.MODE_DESTINATION_PICK_START -> {
                START_POINT_NAME = if (details != "") details else name
                rvReady.isClickable = true
                rvReady.setBackgroundResource(R.drawable.bc_button_purple)
            }

            Constants.MODE_DESTINATION_PICK_EDIT -> {
                EDIT_POINT_NAME = if (details != "") details else name
                rvReady.isClickable = true
                rvReady.setBackgroundResource(R.drawable.bc_button_purple)
            }
            else -> {
                END_POINT_NAME = if (details != "") details else name
//                ROUTE_DATA.add(RouteItem(false,END_POINT_NAME,END_POINT_LAT,END_POINT_LON))
                rvReady.isClickable = true
                rvReady.setBackgroundResource(R.drawable.bc_button_purple)

                rvReady.isClickable = true
            }
        }
    }

    fun changeBottomSheet(newFragment: Fragment, backStack: Boolean) {
        bottomSheet.visibility = View.VISIBLE
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.containerBottomSheet, newFragment)
        if (backStack)
            transaction.addToBackStack(null)

        transaction.commit()
    }


    private fun setUpMapButtons() {
        cardGPS.setOnClickListener {
            if (progressGPS.visibility == View.GONE) {
                requestCurrentLocation()
                progressGPS.visibility = View.VISIBLE
                imageGPS.visibility = View.GONE
            }
        }

//        cardNext.setOnClickListener {
////            textViewDestination.text = getString(R.string.around_city)
//            drawRoute(arrayListOf())
//
//        }
    }

    private fun requestCurrentLocation() {
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
        } else {
            presenter.requestPermissions()
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
        hideKeyboard()
        if (searchCancelListener != null && searchCancelListener is SearchFragment)
            (searchCancelListener as SearchFragment).loadSavedAdresses()

        PAYMENT_TYPE =
            sharedPreferences.getString(Constants.PAYMENT_TYPE, Constants.PAYMENT_TYPE_CASH)
                ?: Constants.PAYMENT_TYPE_CASH
        PAYMENT_TYPE_ID = sharedPreferences.getLong(Constants.PAYMENT_TYPE_ID, 0L) ?: 0L

//        mainDisposables.add(presenter.getOngoingOrder())
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
        if (::receiver.isInitialized) {
            unregisterReceiver(receiver)
        }

        if (currentAnimator != null) {
            currentAnimator?.cancel();
        }

        if (::shimmer.isInitialized) {
            shimmer.cancel()
        }

        if (::aroundCarsDisposable.isInitialized) {
            aroundCarsDisposable.dispose()
        }

        if (animator != null) {
            animator!!.cancel()
        }
        if (iconSpinningAnimator != null) {
            iconSpinningAnimator!!.cancel()
        }
        if (::carPositionDisposable.isInitialized)
            carPositionDisposable.dispose()


        mSocket?.disconnect()
        mSocket?.off("drivers", onNewMessage)
    }

    override fun onLocationChanged(location: Location) {

        val position = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(16.0)
            .tilt(TILT_MAP)
            .build()


        progressGPS.visibility = View.GONE
        imageGPS.visibility = View.VISIBLE

        mainDisposables.add(
            presenter.getAvailableService(
                location.latitude,
                location.longitude,
                PaymentMethod(PAYMENT_TYPE, PAYMENT_TYPE_ID, null, null)
            )
        )

        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)

        mainDisposables.add(presenter.getBonuses(location.latitude, location.longitude))

        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        textViewCurrentAddress.text = getString(R.string.clarifying_address)
        textViewCurrentAddressDetails.text = ""
        shimmer.start(textViewCurrentAddress)

//        mainDisposables.add(presenter.findCurrentAddress(location.latitude, location.longitude))
        mainDisposables.add(presenter.reverseGeocode(location.latitude, location.longitude))

        CURRENT_LATITUDE = location.latitude
        CURRENT_LONGITUDE = location.longitude
        if (!WEATHER_READY)
            presenter.getWeather(CURRENT_LATITUDE, CURRENT_LONGITUDE)

        START_POINT_LAT = location.latitude
        START_POINT_LON = location.longitude

        sharedPreferences.edit()
            .putFloat(Constants.LAST_KNOWN_LATITUDE, location.latitude.toFloat()).apply()
        sharedPreferences.edit()
            .putFloat(Constants.LAST_KNOWN_LONGITUDE, location.longitude.toFloat()).apply()


//        mainDisposables.add(presenter.getPaymentMethods(START_POINT_LAT, START_POINT_LON))


    }

    lateinit var carPosition: LatLng
    private var animator: ValueAnimator? = null
    private var iconSpinningAnimator: ValueAnimator? = null
    lateinit var geoJsonSource: GeoJsonSource
    var OLD_ROTATION = 0f

    private fun addCar(latitude: Double, longitude: Double) {


        pointerLayout.visibility = View.GONE
        imageViewPointerShadow.visibility = View.GONE


        if (::geoJsonSource.isInitialized) {

            if (animator != null) {

                animator!!.removeAllUpdateListeners()
                animator!!.removeAllListeners()
                animator!!.cancel()
                moveCar(latitude, longitude)
            } else if (animator == null) {
                moveCar(latitude, longitude)
            }

        } else {
//            val list = ArrayList<RouteCoordinates>()
//            list.add(RouteCoordinates(latitude, longitude, "through"))
//            list.add(RouteCoordinates(41.382449918051115, 69.30347510578719, "through"))
//            mainDisposables.add(presenter.getRoute(list, false))
//
            if (CURRENT_MODE == Constants.MODE_CAR_FOUND)
                showDriverRoute(latitude, longitude)
//            if (END_POINT_LAT != 0.0)
//                cardViewShowRoute.visibility = View.GONE

            carPosition = LatLng(latitude, longitude)

            geoJsonSource = GeoJsonSource(
                "demo-s-id",
                Feature.fromGeometry(
                    Point.fromLngLat(
                        longitude,
                        latitude
                    )
                )
            )

            mapBoxStyle.addSource(geoJsonSource)

            val carLayer = SymbolLayer("demo-l-id", "demo-s-id")
                .withProperties(
                    iconImage("fleet-0"),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true),
                    iconKeepUpright(true)
                )

            mapBoxStyle.addLayer(
                carLayer
            )

        }

    }

    fun moveCar(latitude: Double, longitude: Double) {

        val carPointOLD = Location("carPointOLD")
        carPointOLD.latitude = carPosition.latitude
        carPointOLD.longitude = carPosition.longitude

        val carPointNEW = Location("carPointNEW")
        carPointNEW.latitude = latitude
        carPointNEW.longitude = longitude

        val distance = carPointOLD.distanceTo(carPointNEW)

        if (distance > 5) {

            var rotation = Constants.getRotation(carPosition, LatLng(latitude, longitude))

            if (!rotation.isNaN()) {

                if (kotlin.math.abs(rotation - OLD_ROTATION) > 180) {
                    rotation -= 360
                }




                iconSpinningAnimator = ValueAnimator.ofFloat(OLD_ROTATION, rotation)
                iconSpinningAnimator!!.duration = 600
                iconSpinningAnimator!!.interpolator = LinearInterpolator()

                iconSpinningAnimator!!.addUpdateListener { valueAnimator -> // Retrieve the new animation number to use as the map camera bearing value
                    var newIconRotateValue = valueAnimator.animatedValue as Float
                    if (newIconRotateValue.toString().contains(",")) {
                        newIconRotateValue =
                            newIconRotateValue.toString().replace(",", ".").toFloat()
                    }
                    OLD_ROTATION = newIconRotateValue

                    if (newIconRotateValue < 0)
                        newIconRotateValue += 360

                    val rotateFormatter = DecimalFormat("#.#")



                    mapboxMap.getStyle { style ->
                        val iconSymbolLayer: Layer? = style.getLayerAs("demo-l-id")
                        iconSymbolLayer?.setProperties(
//                            iconRotate(newIconRotateValue),
                            iconImage(
                                Constants.getCarIcon(
                                    kotlin.math.abs(
//                                        rotateFormatter.format(
                                        newIconRotateValue
//                                        ).toFloat()
                                    )
                                )
                            )
                        )
                    }
                }

                iconSpinningAnimator!!.start()
            }
        }

        val animatorUpdateListener =
            AnimatorUpdateListener { valueAnimator ->
                val animatedPosition = valueAnimator.animatedValue as LatLng
                carPosition = animatedPosition

                geoJsonSource.setGeoJson(
                    Point.fromLngLat(
                        animatedPosition.longitude,
                        animatedPosition.latitude
                    )
                )

                val position = CameraPosition.Builder()
                    .target(
                        LatLng(
                            animatedPosition.latitude,
                            animatedPosition.longitude
                        )
                    )
                    .build()
                mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position))

            }

        val latLngEvaluator: TypeEvaluator<LatLng> = object : TypeEvaluator<LatLng> {
            private val latLng = LatLng()
            override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
                latLng.latitude = (startValue.latitude
                        + (endValue.latitude - startValue.latitude) * fraction)
                latLng.longitude = (startValue.longitude
                        + (endValue.longitude - startValue.longitude) * fraction)
                return latLng
            }
        }

        animator = ObjectAnimator
            .ofObject(latLngEvaluator, carPosition, LatLng(latitude, longitude))
            .setDuration(5000)
        animator!!.interpolator = LinearInterpolator()
        animator!!.addUpdateListener(animatorUpdateListener)
        animator!!.start()

        carPosition = LatLng(latitude, longitude)


    }


    private fun showDriverRoute(latitude: Double, longitude: Double) {
        val position = CameraPosition.Builder()
            .target(
                LatLng(
                    latitude,
                    longitude
                )
            )
            .zoom(16.0)
            .build()
//
        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position), 1000)


    }

    fun showFeedbackOrder(orderID: Long, cost: Double, usedBonus: Double) {
        BottomSheetFeedback(orderID, cost, usedBonus).show(supportFragmentManager, "feedback-show")
    }

    lateinit var driwerLineSource: GeoJsonSource


    override fun drawDriverRoute(route: ArrayList<Point>, origin: Point) {

        mapBoxStyle.addSource(
            GeoJsonSource(
                "drive-start-source", Feature.fromGeometry(
                    Point.fromLngLat(
                        route[route.size - 1].longitude(),
                        route[route.size - 1].latitude()
                    )
                )
            )
        )

        mapBoxStyle.addLayerBelow(
            SymbolLayer(
                "drive-start-layer",
                "drive-start-source"
            ).withProperties(
                iconImage("start-image"),
                iconOffset(arrayOf(0f, -8f))
            ), "demo-l-id"
        )


        driwerLineSource = GeoJsonSource(
            "drive-line-source",
            FeatureCollection.fromFeatures(
                arrayOf(
                    Feature.fromGeometry(
                        LineString.fromLngLats(route)
                    )
                )
            ), GeoJsonOptions().withLineMetrics(true)
        )

        mapBoxStyle.addSource(driwerLineSource)

        mapBoxStyle.addLayerBelow(
            LineLayer("drive-line-layer", "drive-line-source").withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineColor(
                    (rgb(57, 3, 78))
                ),
                lineWidth(6f)
            ), "drive-start-layer"
        )


    }


// Required functions

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    override fun onStatusChanged(arg0: String?, arg1: Int, arg2: Bundle?) {}


    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
// Check if permissions are enabled and if not request
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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
                .accuracyColor(ContextCompat.getColor(this, R.color.purple_500))
                .build()
//
//
//            val customLocationComponentOptions = LocationComponentOptions.builder(this)
//                .elevation(5f)
//                .accuracyAlpha(.6f)
//                .accuracyColor(ContextCompat.getColor(this, R.color.purple_500))
//                .foregroundDrawable(R.drawable.find)
//                .build()
//
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

            mapboxMap.locationComponent.addOnIndicatorPositionChangedListener {
                val mapLatLng = mapboxMap.cameraPosition.target
                val mapLat = mapLatLng.latitude
                val mapLong = mapLatLng.longitude

                val distance = Constants.distanceTo(it.latitude(), it.longitude(), mapLat, mapLong)

                if (distance < 60) {
                    val position = CameraPosition.Builder()
                        .target(LatLng(it.latitude(), it.longitude()))
                        .zoom(16.0)
                        .tilt(TILT_MAP)
                        .build()
                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE || CURRENT_MODE == Constants.MODE_DESTINATION_PICK || CURRENT_MODE == Constants.MODE_DESTINATION_PICK_STOP || CURRENT_MODE == Constants.MODE_DESTINATION_PICK_ADDRESS || CURRENT_MODE == Constants.MODE_DESTINATION_PICK_EDIT)
                        mainDisposables.add(presenter.reverseGeocode(mapLat, mapLong))
                    mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position), 1000)

                }


            }

        }


    }

    override fun onBackPressed() {

        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView)
        } else {

            when (CURRENT_MODE) {
                Constants.MODE_SEARCH_WHERE -> {
                    when (bottomSheetBehaviour.state) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                        }
                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                        else -> {
                            super.onBackPressed()
                        }
                    }
                }
                Constants.MODE_DESTINATION_PICK -> {
                    showSearchWherePage()
                }
                Constants.MODE_CREATE_ORDER -> {
                    if (END_POINT_LAT != 0.0)
                        showDestinationPickPage(Constants.DESTINATION_PICK_ORDEDR, 0.0, 0.0)
                    else
                        showSearchWherePage()
                }
                Constants.MODE_CAR_SEARCH -> {

                    val dialog = AlertDialog.Builder(this).create()
                    val view = LayoutInflater.from(this).inflate(R.layout.dialog_cancel, null)

                    val textYes = view.findViewById<TextView>(R.id.textYes)
                    val textNo = view.findViewById<TextView>(R.id.textNo)

                    textYes.setOnClickListener {
                        dialog.dismiss()
                        rvCancel.performClick()
                    }

                    textNo.setOnClickListener {
                        dialog.dismiss()
                    }

                    dialog.setView(view)
                    dialog.show()

                }

            }


        }


    }


    override fun onRestart() {
        super.onRestart()
        if (!sharedPreferences.getBoolean(Constants.SETTINGS_WEATHER_ANIMATION, true)) {
            lottieTerrain.cancelAnimation()
            lottieTerrain.visibility = View.GONE
            lottieSeason.cancelAnimation()
            lottieSeason.visibility = View.GONE
        } else {
            lottieTerrain.playAnimation()
            lottieTerrain.visibility = View.VISIBLE
            lottieSeason.playAnimation()
            lottieSeason.visibility = View.VISIBLE
        }
    }


    fun hideKeyboard() {
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun startDragging(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }


    private val itemTouchHelper by lazy {
        // 1. Note that I am specifying all 4 directions.
        //    Specifying START and END also allows
        //    more organic dragging than just specifying UP and DOWN.
        val simpleItemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(
                UP or
                        DOWN,
//                    or
//                    START or
//                    END,
                0
            ) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {

                    val adapter = recyclerView.adapter as RouteAdapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    // 2. Update the backing model. Custom implementation in
                    //    MainRecyclerViewAdapter. You need to implement
                    //    reordering of the backing model inside the method.

                    adapter.moveItem(viewHolder, target, from, to)
                    // 3. Tell adapter to render the model update.
                    adapter.notifyItemMoved(from, to)

                    val routeToDraw = ArrayList<RouteCoordinates>()

                    val routePoints = ArrayList<RouteItem>()
                    routePoints.addAll(ROUTE_DATA)
                    routePoints.add(
                        0, RouteItem(
                            false,
                            START_POINT_NAME,
                            START_POINT_LAT,
                            START_POINT_LON
                        )
                    )
                    for (rot in routePoints) {
                        routeToDraw.add(RouteCoordinates(rot.lat, rot.lon, "through"))
                    }

                    imageViewCloudTop.animate().scaleY(8f).setDuration(400).setInterpolator(
                        AccelerateInterpolator()
                    ).start()
                    textViewDrawingAddress.visibility = View.VISIBLE
                    shimmer.start(textViewDrawingAddress)

                    mainDisposables.add(
                        presenter.getRoute(
                            routeToDraw,
                            false
                        )
                    )

                    recyclerView.postDelayed({
                        adapter.notifyDataSetChanged()
                    }, 500)

                    return true
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                    // 4. Code block for horizontal swipe.
                    //    ItemTouchHelper handles horizontal swipe as well, but
                    //    it is not relevant with reordering. Ignoring here.
                }
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }
}