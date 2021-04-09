package me.jeybi.uztaxi.network

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import me.jeybi.uztaxi.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {

    @POST("client/mobile/1.0/registration/submit")
    fun registerClient(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("X-Hive-GPS-Position") hive_gps: String?,
        @Body registerUserRequest: RegisterUserRequest
    ): Single<Response<RegisterUserResponse>>

    @GET("client/mobile/1.0/registration/resubmit")
    fun resendSmsCode(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Query("id") id: Int, @Query("confirmationType") confirmationType: String
    ): Single<Response<EmptyModel>>

    @GET("client/mobile/1.0/registration/confirm")
    fun confirmSmsCode(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Query("id") id: Int, @Query("code") code: String
    ): Single<Response<ConfirmMessageResponse>>


    @POST("client/mobile/1.0/registration/fcm")
    @Headers("Accept-Language: ru", "Content-Type: application/json; charset=utf-8")
    fun registerFCM(
        @Header("Hive-Profile") hive_profile: String,
        @Header("X-Hive-GPS-Position") hive_gps: String?,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String,
        @Body registerFCMRequest: RegisterFCMRequest
    ): Single<Response<EmptyModel>>


    @POST("client/mobile/1.1/service")
    fun getAvailableService(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("X-Hive-GPS-Position") hive_gps: String?,
        @Body paymentMethod: PaymentMethod
    ): Single<Response<ServiceResponse>>

    @GET("client/mobile/1.0/payment-methods")
    fun getPaymentOptions(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("X-Hive-GPS-Position") hive_gps: String?,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String
    ): Single<Response<ArrayList<PaymentMethod>>>

    @GET("client/mobile/1.0/account")
    fun getClientBalance(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("X-Hive-GPS-Position") hive_gps: String?,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String
    ): Single<AccountState>

    @POST("client/mobile/2.0/estimate")
    fun getEstimatedRide(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Body estimateRideRequest: EstimateRideRequest
    ): Single<Response<EstimateResponse>>


    @POST("client/mobile/1.0/promo-code-activations")
    fun usePromocode(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String,
        @Body promocodeRequest: PromocodeRequest
    ): Single<Response<PromocodeResponse>>

    @POST("client/mobile/2.0/drivers")
    fun getAvailableCars(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("X-Hive-GPS-Position") hive_gps: String,
        @Body getCarsRequest: GetCarsRequest
    ): Single<Response<ArrayList<GetCarResponse>>>

    @GET("client/mobile/3.0/address/client")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun getClientAddresses(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String
    ): Single<Response<ArrayList<ClientAddress>>>


    @GET("client/mobile/2.0/address/nearest")
    fun getCurrentAddress(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("X-Hive-GPS-Position") hive_gps: String?
    ): Single<Response<ArrayList<SearchedAddress>>>



    @GET("client/mobile/1.0/address/geocoding")
    fun geocodePoint(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("X-Hive-GPS-Position") hive_gps: String?,
        @Query("query") query: String
    ): Single<Response<ArrayList<SearchedAddress>>>


    @GET("weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("lang") lang: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Single<Response<WeatherResponse>>


//    @POST("route?api_key=f105994c-4760-4888-a39b-bb9b2d07f66c")
//    fun getRoute(
//        @Body getRouteRequest: GetRouteRequest
//    ): Single<Response<GetRouteResponse>>

    @GET("route")
    fun getRoute(
        @Query("point",encoded = true) points : List<String>,
        @Query("vehicle")
        vehicle: String,
        @Query("locale")
        locale: String,
        @Query("calc_points")
        calc_points: Boolean
    ): Single<Response<GraphopperNavResponse>>



    @POST("client/mobile/4.0/orders")
    fun createOrder(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String,
        @Body createOrderRequest: CreateOrderRequest
    ): Single<Response<CreateOrderResponse>>

    @DELETE("client/mobile/1.0/orders/{id}")
    fun cancelOrder(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String,
        @Path("id") id: Long
    ): Single<Response<EmptyModel>>


    @GET("client/mobile/2.0/orders")
    @Headers("Content-Type: application/json; charset=utf-8")
    fun getClientOrders(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String
    ): Single<Response<ArrayList<ShortOrderInfo>>>


    @GET("client/mobile/2.0/history?offset=0&length=16")
    @Headers( "Content-Type: application/json; charset=utf-8")
    fun getAddressHistory(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String,
    ): Single<Response<ArrayList<ShortOrderInfo>>>

    @GET("client/mobile/2.2/orders/{id}")
    @Headers( "Content-Type: application/json; charset=utf-8")
    fun getOrderDetails(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String,
        @Path("id") id: Long
    ): Single<Response<OrderInfo>>


    @GET("client/mobile/1.0/orders/{id}/fix-cost")
    @Headers( "Content-Type: application/json; charset=utf-8")
    fun fixOrderCost(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String,
        @Path("id") id: Long,
        @Query("amount") amount : Double
    ): Single<Response<EmptyModel>>



    @GET("client/mobile/1.0/orders/{id}/coming")
    @Headers( "Content-Type: application/json; charset=utf-8")
    fun notifyDriver(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String,
        @Path("id") id: Long
    ): Single<Response<EmptyModel>>

    @POST("client/mobile/1.1/orders/{id}/feedback")
    @Headers( "Content-Type: application/json; charset=utf-8")
    fun rateOrder(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String,
        @Path("id") id: Long,
        @Body rateOrderBody: RateOrderBody
    ): Single<Response<EmptyModel>>


    @GET("client/mobile/2.1/bonuses")
    @Headers( "Content-Type: application/json; charset=utf-8")
    fun getBonuses(
        @Header("Accept-Language") language : String,
        @Header("X-Hive-GPS-Position") hive_gps: String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String,
    ): Single<Response<BonusResponse>>


    @GET("client/mobile/2.0/history")
    @Headers( "Content-Type: application/json; charset=utf-8")
    fun getOrderHistory(
        @Header("Accept-Language") language : String,
        @Header("Hive-Profile") hive_profile: String,
        @Header("Date") date: String,
        @Header("Authentication") hmac: String,
        @Query("offset") offset : Int,
        @Query("length") length : Int
    ): Single<Response<ArrayList<ShortOrderInfo>>>


    @GET("reverse")
    fun reverseGeocode(
        @Query("lat")
        lat: Double,
        @Query("lon")
        lon: Double,
        @Query("zoom")
        zoom: Int,
        @Query("format")
        format: String,
        @Query("accept-language")
        accept_language: String
    ): Single<Response<ReverseGeocodeResponse>>

    @GET("autocomplete")
    fun geocode(
        @Query("text")
        text : String,
        @Query("boundary.circle.lat")
        lat: Double,
        @Query("boundary.circle.lon")
        lon: Double,
        @Query("boundary.circle.radius")
        radius : Int,
        @Query("focus.point.lat")
        focusLat: Double,
        @Query("focus.point.lon")
        focusLon: Double,
        @Query("boundary.country")
        country: String,
        @Query("api_key")
        api_key: String,
        @Query("size")
        size : Int,
        @Query("lang")
        lang: String,
        @Query("sources")
        sources : String
    ): Single<Response<GeocodingResponse>>





}