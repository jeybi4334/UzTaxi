package me.jeybi.uztaxi.ui.main

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.app.Activity
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
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
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
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_edit_ride.*
import kotlinx.android.synthetic.main.bottom_sheet_car_search.*
import kotlinx.android.synthetic.main.bottom_sheet_search.*
import kotlinx.android.synthetic.main.bottom_sheet_where.*
import kotlinx.android.synthetic.main.bottom_sheet_where.textViewStartAddress
import kotlinx.android.synthetic.main.bottomsheet_map.*
import kotlinx.android.synthetic.main.item_search.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.*
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.ui.adapters.CarsAdapter
import me.jeybi.uztaxi.ui.intro.IntroActivity
import me.jeybi.uztaxi.ui.main.bottomsheet.*
import me.jeybi.uztaxi.ui.main.fragments.*
import me.jeybi.uztaxi.utils.Constants
import me.jeybi.uztaxi.utils.NaiveHmacSigner
import retrofit2.http.OPTIONS
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit


class MainActivity : BaseActivity(), MainController.view,
    LocationListener {

//    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
//
//    private var permissionsManager: PermissionsManager = PermissionsManager(this)

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

    var HIVE_TOKEN: String = ""
    var HIVE_USER_ID: Long = 0

    val IMAGE_ID = "image-taxi-car"

    var START_POINT_LAT = 0.0
    var START_POINT_LON = 0.0

    var END_POINT_LAT = 0.0
    var END_POINT_LON = 0.0

    var START_POINT_NAME = ""
    var END_POINT_NAME = ""


    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun onViewDidCreate(savedInstanceState: Bundle?) {
        presenter = MainPresenter(this)
        HIVE_TOKEN = sharedPreferences.getString(Constants.HIVE_USER_TOKEN, "") ?: ""
        HIVE_USER_ID = sharedPreferences.getLong(Constants.HIVE_USER_ID, 0)

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        presenter.checkIfAuthenticated()
//        onUserApproved()

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
            Log.d("DSADASDsd", "NO TOKEN")
            FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                val mToken = instanceIdResult
                Log.d("DSADASDsd", mToken)
                mainDisposables.add(presenter.registerFCMToken(mToken))

            }
        }

        rvMenu.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
            navigationFragment.onOpen()
        }

    }

    override fun onBonusReady(bonus: Double) {
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
                mapboxMap.uiSettings.isRotateGesturesEnabled = false
                enableLocationComponent(it)
                registerFirebaseReceiver()

                mainDisposables.add(presenter.getOngoingOrder())

                val lat = sharedPreferences.getFloat(Constants.LAST_KNOWN_LATITUDE, 0f)
                val lon = sharedPreferences.getFloat(Constants.LAST_KNOWN_LONGITUDE, 0f)

                if (lat != 0f) {
                    val position = CameraPosition.Builder()
                        .target(LatLng(lat.toDouble(), lon.toDouble()))
                        .zoom(16.0)
                        .tilt(20.0)
                        .build()

                    mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))
                } else {
                    val position = CameraPosition.Builder()
                        .target(LatLng(41.31122086155292, 69.27967758784646))
                        .zoom(16.0)
                        .tilt(20.0)
                        .build()

                    mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position))

                }

                setUpMapButtons()

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
                            R.drawable.icon_finish,
                            null
                        )!!
                    )!!
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

                val carImageBigger = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(resources, R.drawable.car_black),
                    72,
                    144,
                    true
                )


                mapBoxStyle.addImage(
                    "demo-i-id", carImageBigger
                )

            }


            var translationX = 0f
            var translationY = 0f

            var DIRECTION_CURRENT = 0



            mapboxMap.addOnMoveListener(object : MapboxMap.OnMoveListener {
                override fun onMoveBegin(detector: MoveGestureDetector) {
                    // user started moving the map

                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE || CURRENT_MODE == Constants.MODE_DESTINATION_PICK) {
                        rotatePointerRectangleAnimation()
                        imageViewCloudTop.animate().translationY(-200f).alpha(0.6f)
                            .setInterpolator(AccelerateInterpolator()).setDuration(800).start()


                        textViewCurrentAddress.text = "уточняем адрес..."
                        shimmer.start(textViewCurrentAddress)
                    }

                }

                override fun onMove(detector: MoveGestureDetector) {
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
                    imageViewCloudTop.animate().translationY(0f).alpha(1f)
                        .setInterpolator(DecelerateInterpolator()).setDuration(600).start()
                    // user stopped moving the map
                    RECTANGLE_ANIMATING = false
                    cancelPointerAnimation()
                    cancelPointerRectangleAnimation()

                    val mapLatLng = mapboxMap.cameraPosition.target
                    val mapLat = mapLatLng.latitude
                    val mapLong = mapLatLng.longitude

                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE || CURRENT_MODE == Constants.MODE_DESTINATION_PICK)
                        mainDisposables.add(presenter.findCurrentAddress(mapLat, mapLong))

                    when (CURRENT_MODE) {
                        Constants.MODE_SEARCH_WHERE -> {
                            START_POINT_LAT = mapLat
                            START_POINT_LON = mapLong
                        }
                        Constants.MODE_DESTINATION_PICK -> {
                            END_POINT_LAT = mapLat
                            END_POINT_LON = mapLong
                        }
                    }
                    if (CURRENT_MODE == Constants.MODE_SEARCH_WHERE || CURRENT_MODE == Constants.MODE_DESTINATION_PICK) {

                        if (TARIFF_ID != 0L) {
                            mainDisposables.add(
                                presenter.getAvailableCars(
                                    mapLat,
                                    mapLong,
                                    TARIFF_ID
                                )
                            )
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

                }
            })


        }

    }

    lateinit var receiver: BroadcastReceiver

    var ORDER_COST = "0"

    private fun registerFirebaseReceiver() {
        val filter = IntentFilter()
        filter.addAction(Constants.ORDER_STATUS_RECIEVER)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.extras != null) {

                    val ORDER_ID = intent.extras!!.getLong(Constants.ORDER_ID)

                    when (intent.extras!!.get(Constants.ORDER_STATUS)) {
                        Constants.ORDER_STATUS_CREATED -> {
                            showFoundCarInfo(ORDER_ID)
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
                            showDriverCameDialog(ORDER_ID)
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
                if (!ROUTE_DRAWN) {
                    mainDisposables.add(
                        presenter.getRoute(
                            Point.fromLngLat(
                                shortOrderInfo.route[0].position!!.lat,
                                shortOrderInfo.route[0].position!!.lon
                            ),
                            Point.fromLngLat(
                                shortOrderInfo.route[1].position!!.lat,
                                shortOrderInfo.route[1].position!!.lon
                            ), false
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
                showFeedbackOrder(shortOrderInfo.id, ORDER_COST)

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

        when (orderInfo.state) {

            Constants.ORDER_STATE_ASSIGNED -> {
                ORDER_STATE = Constants.ORDER_STATE_ASSIGNED
                CURRENT_MODE = Constants.MODE_CAR_FOUND
                if (lottieAnimation.visibility== View.VISIBLE)
                    showFoundCarInfo(oderID)
                if (!ROUTE_DRAWN){
                    mainDisposables.add(presenter.getRoute(
                        Point.fromLngLat(orderInfo.route[0].address.position!!.lon,
                            orderInfo.route[0].address.position!!.lat),
                        Point.fromLngLat(orderInfo.route[1].address.position!!.lon,
                            orderInfo.route[1].address.position!!.lat),
                        false
                        ))
                }
            }
            Constants.ORDER_STATE_DRIVER_CAME -> {
                ORDER_STATE = Constants.ORDER_STATE_DRIVER_CAME
                CURRENT_MODE = Constants.MODE_DRIVER_CAME
                if (!DRIVER_CAME_DIALOG_SHOWED) {
                    showDriverCameDialog(oderID)
                    DRIVER_CAME_DIALOG_SHOWED = true
                }
            }
            Constants.ORDER_STATE_EXECUTING -> {
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
                if (!FEEDBACK_SHOWED){
                    showFeedbackOrder(oderID, ORDER_COST)
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


    lateinit var carPositionDisposable: Disposable

    private fun showFoundCarInfo(orderID: Long) {
        lottieAnimation.cancelAnimation()
        lottieAnimation.visibility = View.GONE

        modeCarFound.visibility = View.VISIBLE
        modeSearchCar.visibility = View.GONE
        cardGPS.visibility = View.GONE
        cardNext.visibility = View.GONE

        rvCancelRide.setOnClickListener {
            mainDisposables.add(presenter.cancelOrder(orderID))
        }

        val decimalFormat = DecimalFormat("###,###")

        carPositionDisposable = Observable.interval(
            0, 1500,
            TimeUnit.MILLISECONDS
        )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                RetrofitHelper.apiService(Constants.BASE_URL)
                    .getOrderDetails(
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
                            ORDER_COST = decimalFormat.format(it.body()!!.cost.amount)

                            if (textViewRate.text == "" ||textViewCarNumber.text.contains("null")) {
                                textViewCarName.text =
                                    "${it.body()!!.assignee?.car?.brand} ${it.body()!!.assignee?.car?.model} - ${it.body()!!.assignee?.car?.color}"
                                textViewCarNumber.text = "${it.body()!!.assignee?.car?.regNum}"

                                textViewRate.text =
                                    "$ORDER_COST сум"
                                if (it.body()!!.assignee!=null)
                                when(it.body()!!.assignee!!.car.alias){
                                    Constants.CAR_ALIAS_NEXIA->{
                                        imageViewAssignedCar.setImageResource(R.drawable.nexia)
                                    }
                                    Constants.CAR_ALIAS_LACETTI->{
                                        imageViewAssignedCar.setImageResource(R.drawable.lacetti)
                                    }
                                    Constants.CAR_ALIAS_MATIZ->{
                                        imageViewAssignedCar.setImageResource(R.drawable.matiz)
                                    }
                                    Constants.CAR_ALIAS_SPARK->{
                                        imageViewAssignedCar.setImageResource(R.drawable.spark_2)
                                    }


                                }


                                textView0Address.text = "${it.body()!!.route[0].address.name}"
                                textView1Address.text = "${it.body()!!.route[1].address.name}"
                            }

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

    var ROUTE_DRAWN = false

    var searchCancelListener: MainController.SearchCancelListener? = null

    override fun onSearchClicked(searchCancelListener: MainController.SearchCancelListener) {
        this.searchCancelListener = searchCancelListener
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
    }

    var WEATHER_READY = false

    override fun onWeatherReady(weather: ArrayList<WeatherType>, temperature: Int) {

        if (weather.size > 0) {
            WEATHER_READY = true
            when (weather[0].id) {
                in 500..599 -> { //// WEATHER_RAIN
                    playLottie(lottieWeather, "weather_rain.json", true, REVERSE = false)
                    playLottie(lottieSeason, "rain.json", true, REVERSE = false)
                    playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
                }
                in 600..699 -> {  //// WEATHER SNOW
                    playLottie(lottieWeather, "weather_snow.json", true, REVERSE = false)
                    playLottie(lottieSeason, "winter.json", true, REVERSE = false)
                    playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
                }
                in 700..710 -> { //// MIST AND FOG
                    playLottie(lottieWeather, "weather_cloudy.json", true, REVERSE = false)
//                    playLottie(lottieSeason,"winter.json",true, REVERSE = false)
                    playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
                }
                800 -> { /// WEATHER_CLEAR
                    playLottie(lottieWeather, "weather_clear_sky.json", true, REVERSE = false)
//                    playLottie(lottieSeason,"winter.json",true, REVERSE = false)
//                    playLottie(lottieTerrain,"cloud.json",true, REVERSE = false)
                }
                in 801..801 -> {  //// CLOUDS
                    playLottie(lottieWeather, "weather_cloudy.json", true, REVERSE = false)
//                    playLottie(lottieSeason,"winter.json",true, REVERSE = false)
//                    playLottie(lottieTerrain, "cloud.json", true, REVERSE = false)
                }
            }
        }
        textViewTemperature.text = "$temperature"
    }

//    var PEEK_HEIGHT = 0

    override fun onDestinationPickClicked() {
        showDestinationPickPage()
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

    override fun drawRoute(route: ArrayList<Point>) {
        routeLineData.clear()
        routeLineData.addAll(route)

        ROUTE_DRAWN = true

        if (ORDER_STATE==Constants.ORDER_STATE_NOT_CREATED) {

            textReady.visibility = View.VISIBLE
            progressReady.visibility = View.GONE

            CURRENT_MODE = Constants.MODE_CREATE_ORDER
            rvReady.visibility = View.GONE
            modeCreateOrder.visibility = View.VISIBLE
            cardGPS.visibility = View.GONE
            cardNext.visibility = View.GONE
            imageViewPointerShadow.visibility = View.GONE
            pointerLayout.visibility = View.GONE
            textViewCurrentAddress.visibility = View.GONE

            imageViewSelectFromMap.setOnClickListener {
                showDestinationPickPage()
            }

            textViewStartAddress.text = START_POINT_NAME
            textViewDestination.text = END_POINT_NAME

            val routeLines = ArrayList<LatLng>()

            for (point in route) {
                routeLines.add(LatLng(point.latitude(), point.longitude()))
            }

            val latLngBounds = LatLngBounds.Builder()
                .includes(routeLines)
                .build()

            mapboxMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    latLngBounds, 0
                ), 600, object : MapboxMap.CancelableCallback {
                    override fun onCancel() {

                    }

                    override fun onFinish() {
                        mapboxMap.animateCamera(
                            CameraUpdateFactory.zoomTo(mapboxMap.cameraPosition.zoom - 0.5),
                            800
                        )
                        if (recyclerViewCars.getChildAt(0)!=null)
                            recyclerViewCars.getChildAt(0).findViewById<RelativeLayout>(R.id.rvCar)
                                .performClick()
                    }

                }
            )

        }






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
            )
            , "demo-l-id"
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




        mapBoxStyle.addSource(
            GeoJsonSource(
                "finish-source", Feature.fromGeometry(
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
                "finish-layer",
                "finish-source"
            ).withProperties(
                iconImage("finish-image"),
                iconOffset(arrayOf(0f, -8f))
            )
        )

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


        recyclerViewCars.adapter = CarsAdapter(tariffs, object : CarsAdapter.TariffClickListener {
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
                routePoints.add(RouteCoordinates(END_POINT_LAT, END_POINT_LON, null))

                var price = 0.0

                mainDisposables.add(
                    RetrofitHelper.apiService(Constants.BASE_URL)
                        .getEstimatedRide(
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
                                        "${decimalFormat.format(it.body()!!.cost.amount)} сум"
                                    textViewDistance.visibility = View.VISIBLE
                                    textViewDistance.text =
                                        "${Constants.roundAvoid(it.body()!!.distance / 1000, 2)} км"
                                }
                            }
                        }, {

                        })
                )

                val optionChosenListener = object : BottomSheetOrderFilter.OptionsChosenListener {
                    override fun onOptionsChosen(
                        comment: String,
                        options: ArrayList<Long>,
                        optionsValue: Double
                    ) {
                        OPTIONS_VALUE = optionsValue
                        TARIF_OPTIONS = options

                        COMMENT = comment

                        textViewPrice.text =
                            "${decimalFormat.format(price + optionsValue)} сум"

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

                    if (progressOrder.visibility == View.GONE) {

                        progressOrder.visibility = View.VISIBLE
                        textOrder.visibility = View.GONE

                        val createOrderRequest = CreateOrderRequest(
                            CHOSEN_PAYMENT_METHOD,
                            tariffID,
                            TARIF_OPTIONS,
                            arrayListOf(
                                ClientAddress(
                                    SearchedAddress(
                                        START_POINT_NAME, null, null,
                                        SearchPosition(START_POINT_LAT, START_POINT_LON)
                                    ), null, null, null, null
                                ),
                                ClientAddress(
                                    SearchedAddress(
                                        END_POINT_NAME,
                                        null,
                                        null,
                                        SearchPosition(END_POINT_LAT, END_POINT_LON)
                                    ), null, null, null, null
                                )
                            ),
                            null,
                            COMMENT,
                            null,
                            null,
                            null,
                            null,
                            true,
                            null
                        )

                        mainDisposables.add(presenter.createOrder(createOrderRequest))

                    }


                }

            }
        })
//        val helper: SnapHelper = GravitySnapHelper(Gravity.START)
//        helper.attachToRecyclerView(recyclerViewCars)
    }

    var COMMENT = ""
    var TARIF_OPTIONS = ArrayList<Long>()
    var OPTIONS_VALUE = 0.0

    var PEEK_HEIGHT = 0

    fun showSearchWherePage() {

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

        modeCarFound.visibility = View.GONE
        modeSearchCar.visibility = View.GONE
        modeCreateOrder.visibility = View.GONE

        CURRENT_MODE = Constants.MODE_SEARCH_WHERE
        textViewCurrentAddress.visibility = View.VISIBLE

        bottomSheetBehaviour.peekHeight = PEEK_HEIGHT

        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        cardGPS.visibility = View.VISIBLE
        cardNext.visibility = View.VISIBLE

        rvReady.visibility = View.GONE
        textViewDistance.visibility = View.GONE

        ROUTE_DRAWN = false
    }

    fun showDestinationPickPage() {

        imageViewPointerFoot.visibility = View.VISIBLE

        if (CURRENT_MODE == Constants.MODE_CREATE_ORDER) {

            val position = CameraPosition.Builder()
                .target(LatLng(END_POINT_LAT, END_POINT_LON))
                .zoom(16.0)
                .tilt(20.0)
                .build()

            mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position), 1000)
            rvOrder.setBackgroundResource(R.drawable.bc_button_purple_disabled)
            rvOrder.setOnClickListener(null)
            removeRoute()
        }

        CURRENT_MODE = Constants.MODE_DESTINATION_PICK

        bottomSheetBehaviour.peekHeight = 0
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        cardNext.visibility = View.GONE
        rvReady.visibility = View.VISIBLE
        imageViewPointerFoot.visibility = View.VISIBLE
        textViewDistance.visibility = View.GONE

        rvReady.setOnClickListener {

            if (progressReady.visibility != View.VISIBLE) {
                textReady.visibility = View.GONE
                progressReady.visibility = View.VISIBLE
                mainDisposables.add(
                    presenter.getRoute(
                        Point.fromLngLat(START_POINT_LON, START_POINT_LAT),
                        Point.fromLngLat(END_POINT_LON, END_POINT_LAT),
                        false
                    )
                )
            }

        }

        ROUTE_DRAWN = false

        rvReady.visibility = View.VISIBLE
        modeCreateOrder.visibility = View.GONE
        cardGPS.visibility = View.VISIBLE
        cardNext.visibility = View.GONE
        imageViewPointerShadow.visibility = View.VISIBLE
        pointerLayout.visibility = View.VISIBLE
        textViewCurrentAddress.visibility = View.VISIBLE


    }

    override fun onErrorGetRoute() {
        textReady.visibility = View.VISIBLE
        progressReady.visibility = View.GONE

    }

    private fun removeRoute() {
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
            textSearching.text = "Отмена заказа"
            mainDisposables.add(presenter.cancelOrder(orderID))
        }

    }


    override fun onOrderCreated(orderID: Long) {
        showCarSearchPage(orderID)
        textSearching.text = "Ищем водителей вокруг ..."
        shimmer = Shimmer()
        shimmer.start(textSearching)

        rvCancel.setOnClickListener {
            textSearching.text = "Отмена заказа"
            mainDisposables.add(presenter.cancelOrder(orderID))
        }
    }

    override fun onOrderCancelled() {
        modeSearchCar.visibility = View.GONE
        modeCarFound.visibility = View.GONE
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


    override fun onCarsAvailabe(data: ArrayList<GetCarResponse>) {

        for (layer in mapBoxStyle.layers) {
            if (layer.id.startsWith("layer-"))
                mapBoxStyle.removeLayer(layer)
        }
        for (source in mapBoxStyle.sources) {
            if (source.id.startsWith("source-taxi-"))
                mapBoxStyle.removeSource(source)
        }


        var i = 0


        var lastCarLatLng =
            LatLng(data[data.size - 1].location.lat, data[data.size - 1].location.lon)

        for (car in data) {
            i++

            val SOURCE_ID = "source-taxi-$i"

            val geoJsonSource = GeoJsonSource(
                SOURCE_ID, Feature.fromGeometry(
                    Point.fromLngLat(
                        car.location.lon,
                        car.location.lat
                    )
                ),
                GeoJsonOptions()
                    .withCluster(true)
                    .withClusterMaxZoom(16)
                    .withClusterRadius(50)
            )

            mapBoxStyle.addSource(geoJsonSource)

            val LAYER_ID = "layer-${car.id}"

            val carLayer = SymbolLayer(LAYER_ID, SOURCE_ID)
                .withProperties(
                    iconImage(IMAGE_ID),
                    iconIgnorePlacement(true),
                    iconAllowOverlap(true),
                    iconRotate(
                        Constants.getRotation(
                            LatLng(car.location.lat, car.location.lon),
                            lastCarLatLng
                        )
                    )
                )

            lastCarLatLng = LatLng(car.location.lat, car.location.lon)
            mapBoxStyle.addLayer(carLayer)


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
        bottomSheetBehaviour.peekHeight = 0
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED

        mainDisposables.add(
            presenter.getRoute(
                Point.fromLngLat(START_POINT_LON, START_POINT_LAT),
                Point.fromLngLat(longitude, latitude),
                false
            )
        )
        END_POINT_NAME = title
        END_POINT_LAT = latitude
        END_POINT_LON = longitude
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
                Log.d("DASDADS", "STATE $newState")
                if (searchCancelListener != null) {
                    if (newState != BottomSheetBehavior.STATE_EXPANDED) {
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

    override fun onAddressFound(name: String) {
        textViewCurrentAddress.text = name
        shimmer.cancel()
        if (name != "")
            CURRENT_ADDRESS = name
        else
            CURRENT_ADDRESS = "${CURRENT_LATITUDE},${CURRENT_LONGITUDE}"

        when (CURRENT_MODE) {
            Constants.MODE_SEARCH_WHERE -> {
                START_POINT_NAME = CURRENT_ADDRESS
            }
            Constants.MODE_DESTINATION_PICK -> {
                END_POINT_NAME = CURRENT_ADDRESS
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

        if (animator != null) {
            animator!!.cancel()
        }
        if (iconSpinningAnimator != null) {
            iconSpinningAnimator!!.cancel()
        }
        if (::carPositionDisposable.isInitialized)
            carPositionDisposable.dispose()
    }

    override fun onLocationChanged(location: Location) {

        val position = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(16.0)
            .tilt(20.0)
            .build()


        progressGPS.visibility = View.GONE
        imageGPS.visibility = View.VISIBLE

        mainDisposables.add(presenter.getAvailableService(location.latitude, location.longitude))

        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)

        mainDisposables.add(presenter.getBonuses(location.latitude, location.longitude))

        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        textViewCurrentAddress.text = "уточняем адрес..."
        shimmer.start(textViewCurrentAddress)
        mainDisposables.add(presenter.findCurrentAddress(location.latitude, location.longitude))

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


            if (animator != null && !animator!!.isRunning) {
                moveCar(latitude, longitude)

            } else if (animator == null) {
                moveCar(latitude, longitude)
            }

            if (CURRENT_MODE == Constants.MODE_CAR_FOUND) {

//                if (::driwerLineSource.isInitialized) {
//                    val carPoint = Location("carPoint")
//                    carPoint.latitude = latitude
//                    carPoint.longitude = longitude
//                    for (routePoint in driwerRoute) {
//                        val linePoint = Location("linePoint")
//                        linePoint.latitude = routePoint.latitude()
//                        linePoint.longitude = routePoint.longitude()
//
//                        val distance = linePoint.distanceTo(carPoint)
//                        if (distance < 15) {
//                            driwerRoute.remove(routePoint)
//                        }
//                    }
//                    driwerLineSource.setGeoJson(
//                        Feature.fromGeometry(
//                            LineString.fromLngLats(
//                                driwerRoute
//                            )
//                        )
//                    )
//                }

            }

            if (CURRENT_MODE == Constants.MODE_RIDE_STARTED) {

                if (::routeLineSource.isInitialized) {

                    val carPoint = Location("APoint")
                    carPoint.latitude = latitude
                    carPoint.longitude = longitude
                    for (routePoint in routeLineData) {
                        val linePoint = Location("BPoint")
                        linePoint.latitude = routePoint.latitude()
                        linePoint.longitude = routePoint.longitude()

                        val distance = linePoint.distanceTo(carPoint)
                        if (distance < 15) {
                            routeLineData.remove(routePoint)
                        }
                    }
                    routeLineSource.setGeoJson(
                        Feature.fromGeometry(
                            LineString.fromLngLats(
                                routeLineData
                            )
                        )
                    )
                }

            }


        } else {
            if (CURRENT_MODE == Constants.MODE_CAR_FOUND)
                showDriverRoute(latitude, longitude)

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
                    iconImage("demo-i-id")
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
                        if (distance > 10) {

                            var rotation = Constants.getRotation(carPosition, LatLng(latitude, longitude))

                            if (!rotation.isNaN()) {

                                if (kotlin.math.abs(rotation - OLD_ROTATION) > 180) {
                                    rotation -= 360
                                }

                                iconSpinningAnimator = ValueAnimator.ofFloat(OLD_ROTATION, rotation)
                                iconSpinningAnimator!!.duration = 600
                                iconSpinningAnimator!!.interpolator = LinearInterpolator()

                                iconSpinningAnimator!!.addUpdateListener { valueAnimator -> // Retrieve the new animation number to use as the map camera bearing value
                                    val newIconRotateValue = valueAnimator.animatedValue as Float
                                    OLD_ROTATION = newIconRotateValue
                                    mapboxMap.getStyle { style ->
                                        val iconSymbolLayer: Layer? = style.getLayerAs("demo-l-id")
                                        iconSymbolLayer?.setProperties(
                                            iconRotate(newIconRotateValue)
                                        )
                                    }
                                }

                                iconSpinningAnimator!!.start()
                            }


                            animator = ObjectAnimator
                                .ofObject(latLngEvaluator, carPosition, LatLng(latitude, longitude))
                                .setDuration(6000)
                            animator!!.interpolator = LinearInterpolator()
                            animator!!.addUpdateListener(animatorUpdateListener)
                            animator!!.start()

                            carPosition = LatLng(latitude, longitude)

                        }
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

//        mainDisposables.add(
//            presenter.getRoute(
//                Point.fromLngLat(longitude, latitude),
//                Point.fromLngLat(START_POINT_LON, START_POINT_LAT),
//                true
//            )
//        )
    }

    fun showFeedbackOrder(orderID: Long, cost: String) {
        BottomSheetFeedback(orderID, cost, "100").show(supportFragmentManager, "feedback-show")
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


    private val animatorUpdateListener =
        AnimatorUpdateListener { valueAnimator ->
            val animatedPosition = valueAnimator.animatedValue as LatLng
            geoJsonSource.setGeoJson(
                Point.fromLngLat(
                    animatedPosition.longitude,
                    animatedPosition.latitude
                )
            )
        }

    // Class is used to interpolate the marker animation.
    private val latLngEvaluator: TypeEvaluator<LatLng> = object : TypeEvaluator<LatLng> {
        private val latLng = LatLng()
        override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
            latLng.latitude = (startValue.latitude
                    + (endValue.latitude - startValue.latitude) * fraction)
            latLng.longitude = (startValue.longitude
                    + (endValue.longitude - startValue.longitude) * fraction)
            return latLng
        }
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
                    showDestinationPickPage()
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

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


}