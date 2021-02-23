package me.jeybi.uztaxi.ui.main

import com.mapbox.geojson.Point
import io.reactivex.disposables.Disposable
import me.jeybi.uztaxi.model.*

interface MainController {

    interface view {

        fun onUserNotAuthenticated()
        fun onUserApproved()
        fun buildAlertMessageNoGps()

        fun onBonusReady(bonus : Double)

        fun onAddressFound(name : String,details : String)

        fun onSearchClicked(searchCancelListener: SearchCancelListener)

        fun onWeatherReady(weather : ArrayList<WeatherType>, temperature : Int)


        fun onDestinationPickClicked(action : Int)


        fun drawRoute(route: ArrayList<Point>)

        fun onBottomSheetSearchItemClicked(latitude: Double, longitude: Double,title : String)

        fun onTariffsReady(tariffs : ArrayList<ServiceTariff>)
        fun onServiceNotAvailable()

        fun onPaymentMethodsReady(paymentMethods : ArrayList<PaymentMethod>)

        fun onCarsAvailabe(data : ArrayList<GetCarResponse>)

        fun onAddAddressClicked()

        fun onOrderCreated(orderID : Long)

        fun onOrderCancelled()

        fun onNoGoingOrder()

        fun onOnGoingOrderFound(shortOrderInfo: ShortOrderInfo)

        fun drawDriverRoute(route: ArrayList<Point>,origin : Point)

        fun onHasGPS()


        fun onErrorGetRoute()
        fun onErrorCreateOrder()

    }
    interface presenter{

        fun checkIfAuthenticated()
        fun requestPermissions()
        fun checkGPS()
        fun registerFCMToken(token : String) : Disposable
        fun getUserAddresses() : Disposable

        fun findCurrentAddress(latitude : Double, longitude : Double) : Disposable

        fun reverseGeocode(latitude : Double, longitude : Double) : Disposable

        fun getBonuses(latitude: Double, longitude: Double) : Disposable

        fun getWeather(latitude : Double, longitude : Double) : Disposable

        fun getRoute(listCoordinates: ArrayList<RouteCoordinates>,driverRoute : Boolean) : Disposable

        fun getAvailableCars(latitude: Double,longitude: Double,tariff : Long) : Disposable

        fun getAvailableService(latitude: Double,longitude: Double) : Disposable

        fun getPaymentMethods(latitude: Double,longitude: Double) : Disposable

        fun createOrder(createOrderRequest: CreateOrderRequest) : Disposable

        fun getOngoingOrder() : Disposable

        fun notifyDriver(orderID: Long) : Disposable

        fun cancelOrder(orderID: Long) : Disposable

        fun notifyDriver(orderID : Long,rateOrderBody: RateOrderBody): Disposable

        fun addCarImages()
    }

    interface SearchCancelListener{
        fun onSearchCancel()
        fun onSearchStart()
    }
}