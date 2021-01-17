package me.jeybi.uztaxi.ui.main

import com.mapbox.geojson.Point
import io.reactivex.disposables.Disposable
import me.jeybi.uztaxi.model.*

interface MainController {

    interface view {

        fun onUserNotAuthenticated()
        fun onUserApproved()
        fun buildAlertMessageNoGps()

        fun onAddressFound(name : String)

        fun onSearchClicked(searchCancelListener: SearchCancelListener)

        fun onWeatherReady(weather : ArrayList<WeatherType>, temperature : Int)


        fun onDestinationPickClicked()


        fun drawRoute(route: ArrayList<Point>)

        fun onBottomSheetSearchItemClicked(latitude: Double, longitude: Double,title : String)

        fun onTariffsReady(tariffs : ArrayList<ServiceTariff>)

        fun onCarsAvailabe(data : ArrayList<GetCarResponse>)
    }
    interface presenter{

        fun checkIfAuthenticated()
        fun requestPermissions()
        fun checkGPS()
        fun registerFCMToken(token : String) : Disposable
        fun getUserAddresses() : Disposable

        fun findCurrentAddress(latitude : Double, longitude : Double) : Disposable

        fun getWeather(latitude : Double, longitude : Double) : Disposable

        fun getRoute(origin: Point, destination: Point) : Disposable

        fun getAvailableCars(latitude: Double,longitude: Double,tariff : Long) : Disposable

        fun getAvailableService(latitude: Double,longitude: Double) : Disposable

//        fun estimateRoute(startPoint : Point,endPoint : Point) : Disposable

    }

    interface SearchCancelListener{
        fun onSearchCancel()
        fun onSearchStart()
    }
}