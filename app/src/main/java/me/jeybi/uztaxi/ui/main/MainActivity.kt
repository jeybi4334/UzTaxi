package me.jeybi.uztaxi.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SnapHelper
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.iid.FirebaseInstanceId
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
import com.romainpiel.shimmer.Shimmer
import com.romainpiel.shimmer.ShimmerTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_where.*
import kotlinx.android.synthetic.main.bottomsheet_map.*
import me.jeybi.uztaxi.R
import me.jeybi.uztaxi.model.*
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.ui.BaseActivity
import me.jeybi.uztaxi.ui.adapters.CarsAdapter
import me.jeybi.uztaxi.ui.intro.IntroActivity
import me.jeybi.uztaxi.ui.main.fragments.*
import me.jeybi.uztaxi.utils.Constants
import java.text.DecimalFormat


class MainActivity : BaseActivity(), MainController.view,
    LocationListener, PermissionsListener {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private var permissionsManager: PermissionsManager = PermissionsManager(this)

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


    }


    override fun onUserNotAuthenticated() {
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }

    override fun onUserApproved() {
        setUpMap()
        setUpNavigationView()
        mainDisposables.add(presenter.getUserAddresses())

        if (!sharedPreferences.getBoolean(Constants.PREF_FCM_REGISTERED, false)) {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                val mToken = instanceIdResult.token

                mainDisposables.add(presenter.registerFCMToken(mToken))
            }
        }



        rvMenu.setOnClickListener {
            drawerLayout.openDrawer(navigationView)
        }

    }

    private fun setUpNavigationView() {
        val tx = supportFragmentManager.beginTransaction()
        tx.replace(R.id.navigationView, NavigationFragment())
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

//                val position = CameraPosition.Builder()
//                    .target(LatLng(40.49919850425848, 68.77430472707036))
//                    .zoom(16.0)
//                    .tilt(20.0)
//                    .build()
//                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 500)

                requestCurrentLocation()

//                presenter.getWeather(41.322606968175094, 69.31212808427303)

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


            }


            var translationX = 0f
            var translationY = 0f

            var DIRECTION_CURRENT = 0


            shimmer = Shimmer()

            mapboxMap.addOnMoveListener(object : MapboxMap.OnMoveListener {
                override fun onMoveBegin(detector: MoveGestureDetector) {
                    // user started moving the map
                    rotatePointerRectangleAnimation()
                    imageViewCloudTop.animate().translationY(-200f).alpha(0.6f)
                        .setInterpolator(AccelerateInterpolator()).setDuration(800).start()


                    textViewCurrentAddress.text = "уточняем адрес..."
                    shimmer.start(textViewCurrentAddress)
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
//


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

                    if (TARIFF_ID != 0L)
                        mainDisposables.add(presenter.getAvailableCars(mapLat, mapLong, TARIFF_ID))

                }
            })


        }

    }

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
                in 700..799 -> { //// MIST AND FOG
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

    override fun onDestinationPickClicked() {
        CURRENT_MODE = Constants.MODE_DESTINATION_PICK

        bottomSheetBehaviour.peekHeight = 0
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        cardNext.visibility = View.GONE
        rvReady.visibility = View.VISIBLE

        rvReady.setOnClickListener {
            mainDisposables.add(
                presenter.getRoute(
                    Point.fromLngLat(START_POINT_LON, START_POINT_LAT),
                    Point.fromLngLat(END_POINT_LON, END_POINT_LAT)
                )
            )
        }

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


    override fun drawRoute(route: ArrayList<Point>) {
        CURRENT_MODE = Constants.MODE_CREATE_ORDER
        rvReady.visibility = View.GONE
        modeCreateOrder.visibility = View.VISIBLE
        cardGPS.visibility = View.GONE
        cardNext.visibility = View.GONE
        imageViewPointerShadow.visibility = View.GONE
        pointerLayout.visibility = View.GONE
        textViewCurrentAddress.visibility = View.GONE


        Log.d("SADASD", "${route}")

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
                }

            }
        )


        mapBoxStyle.addSource(
            GeoJsonSource(
                "line-source",
                FeatureCollection.fromFeatures(
                    arrayOf(
                        Feature.fromGeometry(
                            LineString.fromLngLats(route)
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
                lineWidth(7f),
                lineColor(
                    (rgb(112, 223, 125))
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

    override fun onTariffsReady(tariffs: ArrayList<ServiceTariff>) {
        TARIFF_ID = tariffs[0].id

        recyclerViewCars.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val decimalFormat = DecimalFormat("###,###")
        val distanceFormat = DecimalFormat("#.##")



        recyclerViewCars.adapter = CarsAdapter(tariffs, object : CarsAdapter.TariffClickListener {
            override fun onTariffChosen(
                tariffID: Long,
                shimmer: Shimmer,
                textViewPrice: ShimmerTextView
            ) {
                val routePoints = ArrayList<RouteCoordinates>()
                routePoints.add(RouteCoordinates(START_POINT_LAT,START_POINT_LON,null))
                routePoints.add(RouteCoordinates(END_POINT_LAT,END_POINT_LON,null))

                mainDisposables.add(
                    RetrofitHelper.apiService(Constants.BASE_URL)
                        .getEstimatedRide(Constants.HIVE_PROFILE,
                           EstimateRideRequest(PaymentMethod("cash",null,null,null),tariffID,null,routePoints)
                            ).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            if (it.isSuccessful) {
                                shimmer.cancel()
                                if (it.body() != null) {
                                    textViewPrice.text =
                                        "${decimalFormat.format(it.body()!!.cost.amount)} сум"
                                    textViewDistance.visibility = View.VISIBLE
                                    textViewDistance.text =
                                        "${Constants.roundAvoid(it.body()!!.distance/1000,2)} км"
                                }
                            }
                        },{

                        })
                )

            }
        })
//        val helper: SnapHelper = GravitySnapHelper(Gravity.START)
//        helper.attachToRecyclerView(recyclerViewCars)
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
                Point.fromLngLat(longitude, latitude)
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


//    override fun startFindingCar() {
//        cardNext.visibility = View.GONE
//
//        val latLngBounds = LatLngBounds.Builder()
//            .include(LatLng(41.38244668021558, 69.30357947935912))
//            .include(LatLng(41.36736430714892, 69.29200483623976))
//            .build()
//
//
//        mapboxMap.animateCamera(
//            CameraUpdateFactory.newLatLngBounds(
//                latLngBounds,
//                200,
//                500,
//                200,
//                400
//            ), 400
//        )
//
//
//        changeBottomSheetFragment(CarSearchFragment(), true)
//        pointerLayout.visibility = View.VISIBLE
//        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
//        imageViewPointerShadow.visibility = View.INVISIBLE
//        imageViewPointerFoot.visibility = View.GONE
//        lottieAnimation.visibility = View.VISIBLE
//        lottieAnimation.playAnimation()
//    }

//    override fun onCancelSearchClicked() {
//        pointerLayout.visibility = View.GONE
//        lottieAnimation.cancelAnimation()
//        lottieAnimation.visibility = View.GONE
//        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HALF_EXPANDED
//        changeBottomSheetFragment(CarFoundFragment(), true)
//        mapBoxStyle.removeLayer("finish-layer")
//        mapBoxStyle.removeLayer("start-layer")
//        mapBoxStyle.removeLayer("linelayer")
//        mapBoxStyle.removeLayer("car-1-layer")
//
//
//        val latLngBounds = LatLngBounds.Builder()
//            .include(LatLng(41.37458804781986, 69.30572136297432))
//            .include(LatLng(41.38244668021558, 69.30357947935912))
//            .build()
//
//        mapboxMap.animateCamera(
//            CameraUpdateFactory.newLatLngBounds(
//                latLngBounds,
//                250,
//                400,
//                150,
//                950
//            ), 1000
//        )
//
//
////        41.37814966044984, 69.30567456784124
//
//        val carRoute = ArrayList<Point>()
//        carRoute.add(Point.fromLngLat(69.30572136297432, 41.37458804781986))
//        carRoute.add(Point.fromLngLat(69.30567456784124, 41.37814966044984))
//        carRoute.add(Point.fromLngLat(69.30538975679481, 41.37853598436575))
//        carRoute.add(Point.fromLngLat(69.3005808318186, 41.378946964735896))
//        carRoute.add(Point.fromLngLat(69.30087659713605, 41.381577177607134))
//        carRoute.add(Point.fromLngLat(69.30375217715482, 41.381560761149025))
//        carRoute.add(Point.fromLngLat(69.30382873199397, 41.38191976593085))
//        carRoute.add(Point.fromLngLat(69.30383975980732, 41.3824104148219))
//        carRoute.add(Point.fromLngLat(69.30357947935912, 41.38244668021558))
//
//
//
//        mapBoxStyle.addSource(
//            GeoJsonSource(
//                "car-line-source",
//                FeatureCollection.fromFeatures(
//                    arrayOf(
//                        Feature.fromGeometry(
//                            LineString.fromLngLats(carRoute)
//                        )
//                    )
//                ), GeoJsonOptions().withLineMetrics(true)
//            )
//        )
//
//        mapBoxStyle.addLayer(
//            LineLayer("car-line-layer", "car-line-source").withProperties(
////                lineDasharray(arrayOf(0.01f, 2f)),
//                lineCap(Property.LINE_CAP_ROUND),
//                lineJoin(Property.LINE_JOIN_ROUND),
//                lineWidth(4f),
//                lineColor(Color.parseColor("#39034E"))
//            )
//        )
//
//        mapBoxStyle.addImage(
//            "car-2-image",
//            Bitmap.createScaledBitmap(
//                BitmapFactory.decodeResource(resources, R.drawable.car_black),
//                64,
//                128,
//                true
//            )
//        )
//
//        mapBoxStyle.addSource(
//            GeoJsonSource(
//                "car-2-source", Feature.fromGeometry(
//                    Point.fromLngLat(
//                        69.30572136297432,
//                        41.37458804781986
//                    )
//                )
//            )
//        )
//
//        mapBoxStyle.addLayer(
//            SymbolLayer(
//                "car-2-layer",
//                "car-2-source"
//            ).withProperties(
//                iconImage("car-2-image"),
//                iconOffset(arrayOf(0f, -8f))
//            )
//        )
//
//        mapBoxStyle.addImage(
//            "car-dest-image",
//            Constants.getBitmap(
//                ResourcesCompat.getDrawable(
//                    resources,
//                    R.drawable.icon_start,
//                    null
//                )!!
//            )!!
//        )
//
//        mapBoxStyle.addSource(
//            GeoJsonSource(
//                "car-dest-source", Feature.fromGeometry(
//                    Point.fromLngLat(
//                        69.30357947935912, 41.38244668021558
//                    )
//                )
//            )
//        )
//
//        mapBoxStyle.addLayer(
//            SymbolLayer(
//                "car-dest-layer",
//                "car-dest-source"
//            ).withProperties(
//                iconImage("car-dest-image"),
//                iconOffset(arrayOf(0f, -8f))
//            )
//        )
//
//    }

//    override fun onRideStarted() {
//        bottomSheetBehaviour.halfExpandedRatio = 0.4f
//        changeBottomSheetFragment(RideStartedFragment(), true)
//    }

//    override fun editRideClicked() {
//        bottomSheetBehaviour.halfExpandedRatio = 0.6f
//        changeBottomSheetFragment(EditRideFragment(), true)
//    }

//    override fun onCancelRideClicked() {
//        changeBottomSheetFragment(RideFinishedFragment(), true)
//        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
//    }

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
            requestCurrentLocation()
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
//        mapboxNavigation.unregisterRoutesObserver(routeProgressObserver)
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

        mainDisposables.add(presenter.getAvailableService(location.latitude, location.longitude))

        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)


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

    override fun onBackPressed() {

        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView)
        } else {

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