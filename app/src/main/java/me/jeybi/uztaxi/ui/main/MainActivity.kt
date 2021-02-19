package me.jeybi.uztaxi.ui.main

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
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
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.messaging.FirebaseMessaging
import com.mapbox.android.gestures.MoveGestureDetector
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
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
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
import me.jeybi.uztaxi.R
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
    var CURRENT_ADDRESS = ""

    var CURRENT_MODE = Constants.MODE_SEARCH_WHERE

    var ORDER_STATE = Constants.ORDER_STATE_NOT_CREATED


    val IMAGE_ID = "image-taxi-car"

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

//    val ROUTE_LIST = ArrayList<RouteCoordinates>()

    val ROUTE_DATA = ArrayList<RouteItem>()


    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    private var mSocket: Socket? = null

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

    lateinit var navigationFragment: NavigationFragment

    private fun setUpNavigationView() {
        val tx = supportFragmentManager.beginTransaction()
        navigationFragment = NavigationFragment()
        tx.replace(R.id.navigationView, navigationFragment)
        tx.commit()
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

                try {

                    localizationPlugin.setMapLanguage(
                        when (getCurrentLanguage().toLanguageTag()) {
                            "ru" -> MapLocale.RUSSIA
                            "kl" -> MapLocale("name")
                            else -> MapLocale.US
                        }
                    )

                } catch (exception: RuntimeException) {
                    Log.d("APP LANG", exception.toString())
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

                mapboxMap.addOnMapClickListener {
                    if (sharedPreferences.getBoolean(Constants.SETTINGS_DEMO_CAR, false)) {
                        addCar(it.latitude, it.longitude)
                    } else {

                        val position = CameraPosition.Builder()
                            .target(it)
                            .zoom(16.0)
                            .tilt(TILT_MAP)
                            .build()

                        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position), 1000)

                    }


                    true
                }



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

                showCarsAround()
            }

            mapView.addOnDidFinishLoadingStyleListener {

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
                            R.drawable.ic_marker_finish,
                            null
                        )!!
                    )!!
                )

                mapBoxStyle.addImage(
                    "start-image",
                    Constants.getBitmap(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_marker_start,
                            null
                        )!!
                    )!!
                )

                mapBoxStyle.addImage(
                    "stop-image",
                    Constants.getBitmap(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_marker_stop,
                            null
                        )!!
                    )!!
                )

                addCarImages()

            }


            var translationX = 0f
            var translationY = 0f

            var DIRECTION_CURRENT = 0



            mapboxMap.addOnMoveListener(object : MapboxMap.OnMoveListener {
                override fun onMoveBegin(detector: MoveGestureDetector) {
                    // user started moving the map
                    rvReady.isClickable = false
                    rvReady.setBackgroundResource(R.drawable.bc_button_purple_disabled)

                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE || CURRENT_MODE == Constants.MODE_DESTINATION_PICK || CURRENT_MODE==Constants.MODE_DESTINATION_PICK_STOP || CURRENT_MODE==Constants.MODE_DESTINATION_PICK_EDIT) {
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

                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE) {
                        PEEK_HEIGHT = bottomSheetBehaviour.peekHeight
                        bottomSheetBehaviour.setPeekHeight(PEEK_HEIGHT / 4, true)
                        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
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

                    }

                }


                override fun onMoveEnd(detector: MoveGestureDetector) {

                    MAP_MOVING = false
                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE) {
                        bottomSheetBehaviour.setPeekHeight(PEEK_HEIGHT, true)
                        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
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

                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE || CURRENT_MODE == Constants.MODE_DESTINATION_PICK || CURRENT_MODE == Constants.MODE_DESTINATION_PICK_STOP || CURRENT_MODE == Constants.MODE_DESTINATION_PICK_ADDRESS|| CURRENT_MODE == Constants.MODE_DESTINATION_PICK_EDIT)
//                        mainDisposables.add(presenter.findCurrentAddress(mapLat, mapLong))
                        mainDisposables.add(presenter.reverseGeocode(mapLat, mapLong))

                    when (CURRENT_MODE) {
                        Constants.MODE_SEARCH_WHERE -> {
//                            ROUTE_LIST.clear()
                            ROUTE_DATA.clear()
                            START_POINT_LAT = mapLat
                            START_POINT_LON = mapLong
//                            ROUTE_LIST.add(
//                                0,
//                                RouteCoordinates(START_POINT_LAT, START_POINT_LON, "through")
//                            )
//                            ROUTE_DATA.add(RouteItem(false,START_POINT_NAME,START_POINT_LAT,START_POINT_LON))
                        }
                        Constants.MODE_DESTINATION_PICK -> {
                            END_POINT_LAT = mapLat
                            END_POINT_LON = mapLong

//                            ROUTE_LIST.clear()
                            ROUTE_DATA.clear()

//                            ROUTE_LIST.add(
//                                0, RouteCoordinates(
//                                    START_POINT_LAT,
//                                    START_POINT_LON,
//                                    "through"
//                                )
//                            )




//                            ROUTE_LIST.add(
//                                RouteCoordinates(
//                                    END_POINT_LAT,
//                                    END_POINT_LON,
//                                    "through"
//                                )
//                            )
                        }
                        Constants.MODE_DESTINATION_PICK_STOP -> {
                            END_POINT_LAT = mapLat
                            END_POINT_LON = mapLong
//                            ROUTE_LIST.add(
//                                RouteCoordinates(
//                                    END_POINT_LAT,
//                                    END_POINT_LON,
//                                    "through"
//                                )
//                            )
                        }

                        Constants.MODE_DESTINATION_PICK_EDIT -> {
                            EDIT_POINT_LAT = mapLat
                            EDIT_POINT_LON = mapLong
//                            ROUTE_LIST.add(
//                                RouteCoordinates(
//                                    END_POINT_LAT,
//                                    END_POINT_LON,
//                                    "through"
//                                )
//                            )
                        }


                    }

                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE || CURRENT_MODE == Constants.MODE_DESTINATION_PICK) {

                        if (TARIFF_ID != 0L) {
//                            mainDisposables.add(
//                                presenter.getAvailableCars(
//                                    mapLat,
//                                    mapLong,
//                                    TARIFF_ID
//                                )
//                            )
                            if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE) {
                                mainDisposables.add(
                                    presenter.getAvailableService(
                                        mapLat,
                                        mapLong
                                    )
                                )
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
            Log.d("DASDASDSADAS", "${e.message}")
        }


//        mSocket?.on("drivers", onNewMessage)
//        mSocket?.connect()
    }

    fun sendLocationToSocket() {


        val jsonObject = JSONObject()

        jsonObject.put("radius", 2000)
        jsonObject.put("lat", "${START_POINT_LAT}")
        jsonObject.put("lon", "${START_POINT_LON}")
        jsonObject.put("limit", 5)
        Log.d("DASDASDSADAS", "$jsonObject")

        mSocket!!.emit("drivers", jsonObject)

    }

    var OLD_SEARCH_POINT_LAT = 0.0
    var OLD_SEARCH_POINT_LON = 0.0

    private val onNewMessage: Emitter.Listener = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.d("DASDASDSADAS", args.toString())

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
                        if (carsList.size>0)
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

    override fun onOnGoingOrderFound(shortOrderInfo: ShortOrderInfo) {

        bottomSheetBehaviour.peekHeight = 0
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED


        when (shortOrderInfo.state) {
            Constants.ORDER_STATE_CREATED -> {
                ORDER_STATE = Constants.ORDER_STATE_CREATED
                if (!ROUTE_DRAWN && shortOrderInfo.route.size > 1) {
//                    ROUTE_LIST.clear()
                    ROUTE_DATA.clear()

//                    ROUTE_LIST.add(
//                        RouteCoordinates(
//                            shortOrderInfo.route[0].position!!.lat,
//                            shortOrderInfo.route[0].position!!.lon,
//                            null
//                        )
//                    )

                    ROUTE_DATA.add(RouteItem(false,shortOrderInfo.route[0].name,shortOrderInfo.route[0].position!!.lat,
                        shortOrderInfo.route[0].position!!.lon))
                    ROUTE_DATA.add(RouteItem(false,shortOrderInfo.route[1].name,shortOrderInfo.route[1].position!!.lat,
                        shortOrderInfo.route[1].position!!.lon))

//                    ROUTE_LIST.add(
//                        RouteCoordinates(
//                            shortOrderInfo.route[1].position!!.lat,
//                            shortOrderInfo.route[1].position!!.lon,
//                            null
//                        )
//                    )

                    var routeToDraw = ArrayList<RouteCoordinates>()

                    for (rot in shortOrderInfo.route){
                        routeToDraw.add(RouteCoordinates(rot.position!!.lat,rot.position.lon,"through"))
                    }

                    mainDisposables.add(
                        presenter.getRoute(
                            routeToDraw,
                            false
                        )
                    )

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


        bottomSheetBehaviour.peekHeight = 0
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED

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

            Constants.ORDER_STATE_ASSIGNED -> {

                val carPointOLD = Location("pointOLD")
                carPointOLD.latitude = START_POINT_LAT
                carPointOLD.longitude = START_POINT_LON

                val carPointNEW = Location("pointNEW")

                if (orderInfo.assignee?.location != null) {
                    carPointNEW.latitude = orderInfo.assignee.location.lat
                    carPointNEW.longitude = orderInfo.assignee.location.lon

                    val distance = carPointOLD.distanceTo(carPointNEW)


                    val time = ((distance.toInt() * 3600) / 10000) / 60 + 1


                    textViewTimeCome.text = "$time ${getString(R.string.minute)}"
                }


                textViewCarName1.text =
                    "${orderInfo.assignee?.car?.brand} ${orderInfo.assignee?.car?.model} - ${orderInfo.assignee?.car?.color}"
                textViewCarNumber1.text = "${orderInfo.assignee?.car?.regNum}"

                textViewCarName1.startAnimation(
                    AnimationUtils.loadAnimation(
                        this,
                        R.anim.text_scroll
                    ) as Animation
                )
//                ROUTE_LIST.clear()
                ROUTE_DATA.clear()

//                ROUTE_LIST.add(
//                    RouteCoordinates(
//                        orderInfo.route[0].address.position!!.lat,
//                        orderInfo.route[0].address.position!!.lon,
//                        null
//                    )
//                )
//                ROUTE_LIST.add(
//                    RouteCoordinates(
//                        orderInfo.route[1].address.position!!.lat,
//                        orderInfo.route[1].address.position!!.lon,
//                        null
//                    )
//                )

                var routeToDraw = ArrayList<RouteCoordinates>()

                for (rot in orderInfo.route){
                    routeToDraw.add(RouteCoordinates(rot.address.position!!.lat,rot.address.position.lon,"through"))
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
                    "${orderInfo.assignee?.car?.brand} ${orderInfo.assignee?.car?.model} - ${orderInfo.assignee?.car?.color}"
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
            }
            Constants.ORDER_STATE_EXECUTING -> {

                textViewCarName.text =
                    "${orderInfo.assignee?.car?.brand} ${orderInfo.assignee?.car?.model} - ${orderInfo.assignee?.car?.color}"
                textViewCarNumber.text = "${orderInfo.assignee?.car?.regNum}"

                val decimalFormat = DecimalFormat("###,###")

                textViewRate.text =
                    "${decimalFormat.format(ORDER_COST)} ${getString(R.string.currency)}"
                if (orderInfo.assignee != null)
                    when (orderInfo.assignee!!.car.alias) {
                        Constants.CAR_ALIAS_NEXIA -> {
                            imageViewAssignedCar.setImageResource(R.drawable.nexia)
                        }
                        Constants.CAR_ALIAS_LACETTI -> {
                            imageViewAssignedCar.setImageResource(R.drawable.lacetti)
                        }
                        Constants.CAR_ALIAS_MATIZ -> {
                            imageViewAssignedCar.setImageResource(R.drawable.matiz)
                        }
                        Constants.CAR_ALIAS_SPARK -> {
                            imageViewAssignedCar.setImageResource(R.drawable.spark_2)
                        }


                    }


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


    private fun showFoundCarInfo(orderID: Long) {
        lottieAnimation.cancelAnimation()
        lottieAnimation.visibility = View.GONE

        if (CURRENT_MODE == Constants.MODE_CAR_FOUND || CURRENT_MODE == Constants.MODE_DRIVER_CAME)
            modeDriverFound.visibility = View.VISIBLE
        else
            modeRideStart.visibility = View.VISIBLE


        modeSearchCar.visibility = View.GONE
        cardGPS.visibility = View.GONE
        cardNext.visibility = View.GONE

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

    override fun onWeatherReady(weather: ArrayList<WeatherType>, temperature: Int) {

        val showSeason = sharedPreferences.getBoolean(Constants.SETTINGS_WEATHER_ANIMATION, true)

        val currentTime: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val hour = currentTime.split(":")[0].toInt()


        if (weather.size > 0) {
            WEATHER_READY = true
            when (weather[0].id) {
                in 500..599 -> { //// WEATHER_RAIN
                    if (hour in 7..18) {
                        playLottie(lottieWeather, "day_rain.json", true, REVERSE = false)
                    } else {
                        playLottie(lottieWeather, "weather_rain.json", true, REVERSE = false)
                    }
                    if (showSeason) {
                        playLottie(lottieSeason, "rain.json", true, REVERSE = false)
                        playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
                    }
                }
                in 600..699 -> {  //// WEATHER SNOW
                    if (hour in 7..18) {
                        playLottie(lottieWeather, "day_snow.json", true, REVERSE = false)
                    } else {
                        playLottie(lottieWeather, "weather_snow.json", true, REVERSE = false)
                    }
                    if (showSeason) {
                        playLottie(lottieSeason, "winter.json", true, REVERSE = false)
                        playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
                    }
                }
                in 700..710 -> { //// MIST AND FOG
                    if (hour in 7..18) {
                        playLottie(lottieWeather, "day_cloudy.json", true, REVERSE = false)
                    } else {
                        playLottie(lottieWeather, "weather_cloudy.json", true, REVERSE = false)
                    }
                    if (showSeason)
                        playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
                }
                800 -> { /// WEATHER_CLEAR
                    if (hour in 7..18) {
                        playLottie(lottieWeather, "day_clear.json", true, REVERSE = false)
                    } else {
                        playLottie(
                            lottieWeather,
                            "weather_clear_sky.json",
                            true,
                            REVERSE = false
                        )
                    }
                }
                in 801..900 -> {  //// CLOUDS
                    if (hour in 7..18) {
                        playLottie(
                            lottieWeather,
                            "day_cloudy.json",
                            true,
                            REVERSE = false
                        )
                    } else {
                        playLottie(lottieWeather, "weather_cloudy.json", true, REVERSE = false)
                    }
                }
            }
        }

        textViewTemperature.text = "$temperature"
    }

    override fun onDestinationPickClicked(action: Int) {
        showDestinationPickPage(action,0.0,0.0)
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


    lateinit var routeLineSource: GeoJsonSource

    var routeLineData = ArrayList<Point>()

    override fun onRouteAddClicked(){
        AddresSearchFragment().show(supportFragmentManager, "searchStopPoint")
    }

    override  fun onChangeRouteLocationClicked(lat : Double,lon : Double){
        val position = CameraPosition.Builder()
            .target(LatLng(lat, lon))
            .zoom(16.0)
            .tilt(TILT_MAP)
            .build()

        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))

        showDestinationPickPage(Constants.DESTINATION_PICK_EDIT,lat,lon)
    }
    override fun onRemoveRouteClicked(lat:Double,lon :Double,position: Int,removedItem : RouteItem){
        ROUTE_DATA.remove(removedItem)

        imageViewCloudTop.animate().scaleY(8f).setDuration(400).setInterpolator(AccelerateInterpolator()).start()
        textViewDrawingAddress.visibility = View.VISIBLE
        shimmer.start(textViewDrawingAddress)

        val routeToDraw = ArrayList<RouteCoordinates>()
        val routePoints =  ArrayList<RouteItem>()
        routePoints.addAll(ROUTE_DATA)
        routePoints.add(0,RouteItem(false,START_POINT_NAME,START_POINT_LAT,START_POINT_LON))
        for (rot in routePoints){
            routeToDraw.add(RouteCoordinates(rot.lat,rot.lon,"through"))
        }

        mainDisposables.add(
            presenter.getRoute(
                routeToDraw,
                false
            )
        )

        recyclerViewRoute.adapter?.notifyItemRemoved(position)
    }

    override fun drawRoute(route: ArrayList<Point>) {

        imageViewCloudTop.animate().scaleY(1f).setDuration(400).setInterpolator(AccelerateInterpolator()).start()
        textViewDrawingAddress.visibility = View.GONE
        shimmer.cancel()

        bottomSheetBehaviour.peekHeight = 0
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED


        routeLineData.clear()
        routeLineData.addAll(route)

        ROUTE_DRAWN = true




        if (ORDER_STATE == Constants.ORDER_STATE_NOT_CREATED) {

            recyclerViewRoute.layoutManager = LinearLayoutManager(this)

            recyclerViewRoute.adapter = RouteAdapter(ROUTE_DATA,this,this)
            itemTouchHelper.attachToRecyclerView(recyclerViewRoute)



            textReady.visibility = View.VISIBLE
            progressReady.visibility = View.GONE

            bottomSheetBehaviour.peekHeight = 0
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED

            CURRENT_MODE = Constants.MODE_CREATE_ORDER
            rvReady.visibility = View.GONE
            rvReady.animate().translationY(100f).setDuration(200)
                .setInterpolator(AccelerateInterpolator()).start()

            modeCreateOrder.visibility = View.VISIBLE


            cardGPS.visibility = View.GONE
            cardNext.visibility = View.GONE
            imageViewPointerShadow.visibility = View.GONE
            pointerLayout.visibility = View.GONE
            textViewCurrentAddress.visibility = View.GONE
            textViewCurrentAddressDetails.visibility = View.GONE



            textViewStartAddress.text = START_POINT_NAME



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
                if (END_POINT_LAT != 0.0) {
                    cardViewShowRoute.visibility = View.VISIBLE
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
                }


            }


        }



        if (route.size > 0) {

            removeRoute()



            routeLineSource = GeoJsonSource(
                "line-source",
                FeatureCollection.fromFeatures(
                    arrayOf(
                        Feature.fromGeometry(
                            LineString.fromLngLats(route)
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
                        (rgb(112, 223, 125))
                    )
                ), "demo-l-id"
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

            val routePoints =  ArrayList<RouteItem>()
            routePoints.addAll(ROUTE_DATA)
            routePoints.add(0,RouteItem(false,START_POINT_NAME,START_POINT_LAT,START_POINT_LON))

            for ((counter, rot) in routePoints.withIndex()){
                if (counter!=0&&counter!=routePoints.size-1){
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

        }
    }

    val paymentMethods = ArrayList<PaymentMethod>()

    lateinit var CHOSEN_PAYMENT_METHOD: PaymentMethod

    override fun onPaymentMethodsReady(paymentMethods: ArrayList<PaymentMethod>) {
        this.paymentMethods.clear()
        this.paymentMethods.addAll(paymentMethods)
        CHOSEN_PAYMENT_METHOD = paymentMethods[0]
        imageViewArrow.setOnClickListener {
            PaymentMethodsSheet(paymentMethods).show(supportFragmentManager, "payment")
        }
    }

    override fun onTariffsReady(tariffs: ArrayList<ServiceTariff>) {

        if (TARIFF_ID != tariffs[0].id) {

            TARIFF_ID = tariffs[0].id
            recyclerViewCars.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            val decimalFormat = DecimalFormat("###,###")

            if (paymentMethods.isEmpty()) {
                presenter.getPaymentMethods(START_POINT_LAT, START_POINT_LON)
            }

            recyclerViewCars.adapter = CarsAdapter(this,
                tariffs,
                object : CarsAdapter.TariffClickListener {
                    override fun onTariffChosen(
                        tariffID: Long,
                        shimmer: Shimmer,
                        textViewPrice: ShimmerTextView,
                        options: ArrayList<TariffOption>
                    ) {

                        COMMENT = ""
                        TARIF_OPTIONS.clear()

                        val routePoints = ArrayList<RouteCoordinates>()
                        routePoints.add(RouteCoordinates(START_POINT_LAT, START_POINT_LON, null))
                        if (END_POINT_LAT != 0.0)
                            routePoints.add(RouteCoordinates(END_POINT_LAT, END_POINT_LON, null))

                        var price = 0.0

                        mainDisposables.add(
                            RetrofitHelper.apiService(Constants.BASE_URL)
                                .getEstimatedRide(
                                    getCurrentLanguage().toLanguageTag(),
                                    Constants.HIVE_PROFILE,
                                    EstimateRideRequest(
                                        PaymentMethod("cash", null, null, null),
                                        tariffID,
                                        null,
                                        routePoints
                                    )
                                )
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe({
                                    if (it.isSuccessful) {
                                        shimmer.cancel()
                                        if (it.body() != null) {
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
                })

        }

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
                CHOSEN_PAYMENT_METHOD,
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

        bottomSheetBehaviour.peekHeight = PEEK_HEIGHT

        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        cardGPS.visibility = View.VISIBLE
        cardNext.visibility = View.VISIBLE

        rvReady.visibility = View.GONE
        rvReady.animate().translationY(100f).setDuration(200)
            .setInterpolator(AccelerateInterpolator()).start()
        textViewDistance.visibility = View.GONE

        ROUTE_DRAWN = false


    }

    fun showDestinationPickPage(action: Int,lat : Double,lon : Double) {
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
            Constants.DESTINATION_PICK_EDIT->{
                CURRENT_MODE = Constants.MODE_DESTINATION_PICK_EDIT
            }

        }


        val mapLatLng = mapboxMap.cameraPosition.target
        val mapLat = mapLatLng.latitude
        val mapLong = mapLatLng.longitude

        val position = CameraPosition.Builder()
            .target(LatLng(mapLat + 0.0005, mapLong))
            .tilt(TILT_MAP)
            .build()

        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position), 500)


        bottomSheetBehaviour.peekHeight = 0
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        cardNext.visibility = View.GONE
        rvReady.visibility = View.VISIBLE
        rvReady.animate().translationY(0f).setDuration(200)
            .setInterpolator(DecelerateInterpolator()).start()
        imageViewPointerFoot.visibility = View.VISIBLE
        textViewDistance.visibility = View.GONE

        rvReady.setOnClickListener {


            when (action) {

                Constants.DESTINATION_PICK_ORDEDR -> {
//                    ROUTE_DATA.add(RouteItem(false,END_POINT_NAME,END_POINT_LAT,END_POINT_LON))
                    ROUTE_DATA.add(RouteItem(false,END_POINT_NAME,END_POINT_LAT,END_POINT_LON))
                    if (progressReady.visibility != View.VISIBLE) {

                        val routeToDraw = ArrayList<RouteCoordinates>()
                       val routePoints =  ArrayList<RouteItem>()
                        routePoints.addAll(ROUTE_DATA)
                        routePoints.add(0,RouteItem(false,START_POINT_NAME,START_POINT_LAT,START_POINT_LON))
                        for (rot in routePoints){
                            routeToDraw.add(RouteCoordinates(rot.lat,rot.lon,"through"))
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
                    ROUTE_DATA.add(RouteItem(false,END_POINT_NAME,END_POINT_LAT,END_POINT_LON))

                    recyclerViewRoute.adapter?.notifyDataSetChanged()

                    if (progressReady.visibility != View.VISIBLE) {
                        textReady.visibility = View.GONE
                        progressReady.visibility = View.VISIBLE

                        val routeToDraw = ArrayList<RouteCoordinates>()

                        val routePoints =  ArrayList<RouteItem>()
                        routePoints.addAll(ROUTE_DATA)
                        routePoints.add(0,RouteItem(false,START_POINT_NAME,START_POINT_LAT,START_POINT_LON))
                        for (rot in routePoints){
                            routeToDraw.add(RouteCoordinates(rot.lat,rot.lon,"through"))
                        }

                        mainDisposables.add(
                            presenter.getRoute(
                                routeToDraw,
                                false
                            )
                        )

                    }

                }
                Constants.DESTINATION_PICK_EDIT->{
                    var REMOVE_INDEX = 0
                    for ((index,rot) in ROUTE_DATA.withIndex()){
                        if(rot.lat==lat&&rot.lon==lon){
                            REMOVE_INDEX= index
                        }
                    }
                    ROUTE_DATA.removeAt(REMOVE_INDEX)
                    ROUTE_DATA.add(REMOVE_INDEX,
                        RouteItem(false,EDIT_POINT_NAME,EDIT_POINT_LAT,EDIT_POINT_LON)
                    )
                    recyclerViewRoute.adapter?.notifyDataSetChanged()

                    if (progressReady.visibility != View.VISIBLE) {
                        textReady.visibility = View.GONE
                        progressReady.visibility = View.VISIBLE

                        val routeToDraw = ArrayList<RouteCoordinates>()

                        val routePoints =  ArrayList<RouteItem>()
                        routePoints.addAll(ROUTE_DATA)
                        routePoints.add(0,RouteItem(false,START_POINT_NAME,START_POINT_LAT,START_POINT_LON))
                        for (rot in routePoints){
                            routeToDraw.add(RouteCoordinates(rot.lat,rot.lon,"through"))
                        }

                        mainDisposables.add(
                            presenter.getRoute(
                                routeToDraw,
                                false
                            )
                        )

                    }
                }


                Constants.DESTINATION_PICK_ADDRESS -> {


                }

            }


        }
        rvReady.isClickable = false


        ROUTE_DRAWN = false


        modeCreateOrder.visibility = View.GONE
        cardGPS.visibility = View.VISIBLE
        cardNext.visibility = View.GONE
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
        cardViewShowRoute.visibility = View.GONE
        for (layer in mapBoxStyle.layers) {
            if (layer.id.startsWith("line-") || layer.id.startsWith("start-") || layer.id.startsWith(
                    "finish-"
                )
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

                            onOnGoingOrderChange(orderID, it.body()!!)
                            ORDER_COST = it.body()!!.cost.amount

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


//    override fun onCarsAvailabe(data: ArrayList<GetCarResponse>) {
//
//        for (layer in mapBoxStyle.layers) {
//            if (layer.id.startsWith("layer-"))
//                mapBoxStyle.removeLayer(layer)
//        }
//        for (source in mapBoxStyle.sources) {
//            if (source.id.startsWith("source-taxi-"))
//                mapBoxStyle.removeSource(source)
//        }
//
//
//        var i = 0
//
//
//        var lastCarLatLng =
//            LatLng(data[data.size - 1].location.lat, data[data.size - 1].location.lon)
//
//        for (car in data) {
//            i++
//
//            val SOURCE_ID = "source-taxi-$i"
//
//            val geoJsonSource = GeoJsonSource(
//                SOURCE_ID, Feature.fromGeometry(
//                    Point.fromLngLat(
//                        car.location.lon,
//                        car.location.lat
//                    )
//                ),
//                GeoJsonOptions()
//                    .withCluster(true)
//                    .withClusterMaxZoom(16)
//                    .withClusterRadius(50)
//            )
//
//            mapBoxStyle.addSource(geoJsonSource)
//
//            val LAYER_ID = "layer-${car.id}"
//
//            val rotation = Constants.getRotation(
//                LatLng(car.location.lat, car.location.lon),
//                lastCarLatLng
//            )
//            var image = "fleet-0"
//            if (data.size>1){
//                image = Constants.getCarIcon(rotation)
//            }
//
//            val carLayer = SymbolLayer(LAYER_ID, SOURCE_ID)
//                .withProperties(
//                    iconImage(image),
//                    iconIgnorePlacement(true),
//                    iconAllowOverlap(true),
//                )
//
//            lastCarLatLng = LatLng(car.location.lat, car.location.lon)
//            mapBoxStyle.addLayer(carLayer)
//        }
//    }

    var movingCarPositions = HashMap<Long, LatLng>()
    var movingCarGeoJsonSources  = HashMap<Long, GeoJsonSource>()
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
                }catch (ex: Exception){
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
            .setDuration(5000)


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


        val time = ((distance.toInt() * 3600) / 10000)/60 + 1

        if (!MAP_MOVING) {
            pointerRectangle.animate().scaleY(3f).scaleX(3f)
                .setListener(object  : Animator.AnimatorListener{
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
        }else{
            textViewDurationDriver.text = ""
            textViewDurationMin.visibility = View.INVISIBLE
        }

        val iterator = movingCarGeoJsonSources.iterator()
        while(iterator.hasNext()){
            val item = iterator.next()
            if(!ids.contains(item.key)){
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


//        for (key in movingCarGeoJsonSources.keys) {
//            if (ids.size > 0 && !ids.contains(key)) {
//                movingCarGeoJsonSources.remove(key,movingCarGeoJsonSources[key])
//                mapBoxStyle.removeSource("moving-source-$key")
//                mapBoxStyle.removeLayer("moving-layer-$key")
//                val anim = movingCarAnimations[key]
//                anim?.removeAllUpdateListeners()
//                anim?.removeAllListeners()
//                anim?.cancel()
//                movingCarAnimations.remove(key,movingCarAnimations[key])
//                movingCarAnimationListeners.remove(key,movingCarAnimationListeners[key])
//            }
//        }

        for (car in data) {
            addMovingCar(car)
        }

    }


    override fun onAddAddressClicked() {
        AddresSearchFragment().show(supportFragmentManager, "add_address")
    }

    override fun onBottomSheetSearchItemClicked(
        latitude: Double,
        longitude: Double,
        title: String
    ) {
        END_POINT_NAME = title
        END_POINT_LAT = latitude
        END_POINT_LON = longitude

        ROUTE_DATA.add(RouteItem(false,title,latitude,longitude))

        val routeToDraw = ArrayList<RouteCoordinates>()

        val routePoints =  ArrayList<RouteItem>()
        routePoints.addAll(ROUTE_DATA)
        routePoints.add(0,RouteItem(false,START_POINT_NAME,START_POINT_LAT,START_POINT_LON))
        for (rot in routePoints){
            routeToDraw.add(RouteCoordinates(rot.lat,rot.lon,"through"))
        }

        mainDisposables.add(
            presenter.getRoute(
                routeToDraw,
                false
            )
        )

    }

    fun setUpBottomSheet() {
        bottomSheetBehaviour = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehaviour.isFitToContents = false
        bottomSheetBehaviour.halfExpandedRatio = 0.45f

        PEEK_HEIGHT = bottomSheetBehaviour.peekHeight

        val searchFragment = SearchFragment()
        searchCancelListener = searchFragment
        changeBottomSheet(searchFragment, false)



        bottomSheetBehaviour.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (searchCancelListener != null) {
                    if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        searchCancelListener!!.onSearchCancel()
                    } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        searchCancelListener!!.onSearchStart()
                    }
                }

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
            Constants.MODE_DESTINATION_PICK_EDIT->{
                EDIT_POINT_NAME = if (details != "") details else name
                rvReady.isClickable = true
                rvReady.setBackgroundResource(R.drawable.bc_button_purple)
            }
            else->{
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

        cardNext.setOnClickListener {
//            textViewDestination.text = getString(R.string.around_city)
            drawRoute(arrayListOf())

        }
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


        mSocket?.disconnect();
        mSocket?.off("drivers", onNewMessage);
    }

    override fun onLocationChanged(location: Location) {

        val position = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(16.0)
            .tilt(TILT_MAP)
            .build()


        progressGPS.visibility = View.GONE
        imageGPS.visibility = View.VISIBLE

        mainDisposables.add(presenter.getAvailableService(location.latitude, location.longitude))

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


        mainDisposables.add(presenter.getPaymentMethods(START_POINT_LAT, START_POINT_LON))


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

//
            if (animator != null) {

                animator!!.removeAllUpdateListeners()
                animator!!.removeAllListeners()
                animator!!.cancel()
                moveCar(latitude, longitude)
            } else if (animator == null) {
                moveCar(latitude, longitude)
            }

        } else {

            if (CURRENT_MODE == Constants.MODE_CAR_FOUND)
                showDriverRoute(latitude, longitude)
            if (END_POINT_LAT != 0.0)
                cardViewShowRoute.visibility = View.GONE

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
                                        rotateFormatter.format(
                                            newIconRotateValue
                                        ).toFloat()
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
                        showDestinationPickPage(Constants.DESTINATION_PICK_ORDEDR,0.0,0.0)
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


    private fun addCarImages() {

        mapBoxStyle.addImage(
            "fleet-0", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_0),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-5", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_5),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-10",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_10),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-15", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_15),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-20",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_20),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-25", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_25),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-30",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_30),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-35", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_35),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-40",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_40),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-45", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_45),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-50",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_50),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-55", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_55),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-60",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_60),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-65", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_65),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-70",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_70),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-75", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_75),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-80",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_80),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-85", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_85),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-90",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_90),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-95", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_95),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-100", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_100),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-105", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_105),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-110",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_110),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-115", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_115),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-120",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_120),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-125", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_125),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-130",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_130),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-135", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_135),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-140",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_140),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-145", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_145),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-150",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_150),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-155", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_155),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-160",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_160),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-165", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_165),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-170",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_170),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-175", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_175),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-180",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_180),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-185", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_185),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-190", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_190),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-195", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_195),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-200",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_200),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-205", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_205),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-210",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_210),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-215", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_215),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-220",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_220),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-225", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_225),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-230",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_230),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-235", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_235),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-240",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_240),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-245", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_245),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-250",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_250),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-255", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_255),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-260",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_260),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-265", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_265),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-270",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_270),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-275", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_275),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-280",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_280),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-285", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_285),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-290", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_290),
                188, 188,
                true
            )
        )
        mapBoxStyle.addImage(
            "fleet-295", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_295),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-300",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_300),
                188, 188,
                true
            )
        )
        mapBoxStyle.addImage(
            "fleet-305", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_305),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-310",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_310),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-315", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_315),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-320",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_320),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-325", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_325),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-330",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_330),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-335", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_335),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-340",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_340),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-345", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_345),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-350",
            Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_350),
                188, 188,
                true
            )
        )

        mapBoxStyle.addImage(
            "fleet-355", Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.fleet_355),
                188, 188,
                true
            )
        )


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
            object : ItemTouchHelper.SimpleCallback(UP or
                    DOWN,
//                    or
//                    START or
//                    END,
                0) {

                override fun onMove(recyclerView: RecyclerView,
                                    viewHolder: RecyclerView.ViewHolder,
                                    target: RecyclerView.ViewHolder): Boolean {

                    val adapter = recyclerView.adapter as RouteAdapter
                    val from = viewHolder.adapterPosition
                    val to = target.adapterPosition
                    // 2. Update the backing model. Custom implementation in
                    //    MainRecyclerViewAdapter. You need to implement
                    //    reordering of the backing model inside the method.

                    adapter.moveItem(viewHolder,target ,from, to)
                    // 3. Tell adapter to render the model update.
                    adapter.notifyItemMoved(from, to)

//                    val fromEmoji = ROUTE_DATA[from]
//                    ROUTE_DATA.removeAt(from)
//                    if (to < from) {
//                        ROUTE_DATA.add(to, fromEmoji)
//                    } else {
//                        ROUTE_DATA.add(to - 1, fromEmoji)
//                    }

                    val routeToDraw = ArrayList<RouteCoordinates>()

                    val routePoints =  ArrayList<RouteItem>()
                    routePoints.addAll(ROUTE_DATA)
                    routePoints.add(0,RouteItem(false,START_POINT_NAME,START_POINT_LAT,START_POINT_LON))
                    for (rot in routePoints){
                        routeToDraw.add(RouteCoordinates(rot.lat,rot.lon,"through"))
                    }

                    imageViewCloudTop.animate().scaleY(8f).setDuration(400).setInterpolator(AccelerateInterpolator()).start()
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
                    },500)

                    return true
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                      direction: Int) {
                    // 4. Code block for horizontal swipe.
                    //    ItemTouchHelper handles horizontal swipe as well, but
                    //    it is not relevant with reordering. Ignoring here.
                }
            }
        ItemTouchHelper(simpleItemTouchCallback)
    }
}