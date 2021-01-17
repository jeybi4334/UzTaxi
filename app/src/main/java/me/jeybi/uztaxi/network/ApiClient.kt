package me.jeybi.uztaxi.network

import io.reactivex.Single
import me.jeybi.uztaxi.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {

    @POST("client/mobile/1.0/registration/submit")
    @Headers("Accept-Language: ru")
    fun registerClient(@Header("Hive-Profile") hive_profile : String,
                       @Header("X-Hive-GPS-Position") hive_gps : String?,
        @Body registerUserRequest: RegisterUserRequest): Single<Response<RegisterUserResponse>>

    @GET("client/mobile/1.0/registration/resubmit")
    @Headers("Accept-Language: ru")
    fun resendSmsCode(
        @Header("Hive-Profile") hive_profile : String,
        @Query("id" )id : Int,@Query("confirmationType") confirmationType : String
    ) : Single<Response<EmptyModel>>

    @GET("client/mobile/1.0/registration/confirm")
    @Headers("Accept-Language: ru")
    fun confirmSmsCode(
        @Header("Hive-Profile") hive_profile : String,
        @Query("id" )id : Int,@Query("code") code : String
    ) : Single<Response<ConfirmMessageResponse>>


    @POST("client/mobile/1.0/registration/fcm")
    @Headers("Accept-Language: ru","Content-Type: application/json; charset=utf-8")
    fun registerFCM(@Header("Hive-Profile") hive_profile : String,
                       @Header("X-Hive-GPS-Position") hive_gps : String?,
                        @Header("Date") date : String,
                        @Header("Authentication") hmac : String,
                       @Body registerFCMRequest: RegisterFCMRequest): Single<Response<EmptyModel>>


    @POST("client/mobile/1.1/service")
    @Headers("Accept-Language: ru")
    fun getAvailableService(@Header("Hive-Profile") hive_profile : String,
                       @Header("X-Hive-GPS-Position") hive_gps : String?
    ): Single<Response<ServiceResponse>>

    @GET("client/mobile/1.0/payment-methods")
    @Headers("Accept-Language: ru")
    fun getPaymentOptions(@Header("Hive-Profile") hive_profile : String,
                            @Header("X-Hive-GPS-Position") hive_gps : String?
    ): Single<ArrayList<PaymentMethod>>

    @GET("client/mobile/1.0/account")
    @Headers("Accept-Language: ru")
    fun getClientBalance(@Header("Hive-Profile") hive_profile : String,
                          @Header("X-Hive-GPS-Position") hive_gps : String?,
                         @Header("Date") date : String,
                         @Header("Authentication") hmac : String
    ): Single<AccountState>

    @POST("client/mobile/2.0/estimate")
    @Headers("Accept-Language: ru")
    fun getEstimatedRide(@Header("Hive-Profile") hive_profile : String,
                         @Body estimateRideRequest: EstimateRideRequest
    ): Single<Response<EstimateResponse>>


    @POST("client/mobile/1.0/promo-code-activations")
    @Headers("Accept-Language: ru")
    fun usePromocode(@Header("Hive-Profile") hive_profile : String,
                     @Header("Date") date : String,
                     @Header("Authentication") hmac : String,
                     @Body promocodeRequest: PromocodeRequest
    ): Single<Response<PromocodeResponse>>

    @POST("client/mobile/4.2/orders")
    @Headers("Accept-Language: ru")
    fun createOrder(@Header("Hive-Profile") hive_profile : String,
                     @Header("Date") date : String,
                     @Header("Authentication") hmac : String,
                     @Body createOrderRequest: CreateOrderRequest
    ): Single<Response<CreateOrderResponse>>

    @GET("client/mobile/2.2/orders")
    @Headers("Accept-Language: ru","Content-Type: application/json; charset=utf-8")
    fun getClientOrders(@Header("Hive-Profile") hive_profile : String,
                           @Header("Date") date : String,
                           @Header("Authentication") hmac : String
    ): Single<Response<ArrayList<ShortOrderInfo>>>



    @POST("client/mobile/2.0/drivers")
    @Headers("Accept-Language: ru")
    fun getAvailableCars(@Header("Hive-Profile") hive_profile : String,
                            @Header("X-Hive-GPS-Position") hive_gps : String,
                         @Body getCarsRequest: GetCarsRequest
    ): Single<Response<ArrayList<GetCarResponse>>>

    @GET("client/mobile/3.0/address/client")
    @Headers("Accept-Language: ru","Content-Type: application/json; charset=utf-8")
    fun getClientAddresses(@Header("Hive-Profile") hive_profile : String,
                           @Header("Date") date : String,
                           @Header("Authentication") hmac : String
                           ): Single<Response<ArrayList<ClientAddress>>>



    @GET("client/mobile/2.0/address/nearest")
    @Headers("Accept-Language: ru")
    fun getCurrentAddress(@Header("Hive-Profile") hive_profile : String,
                            @Header("X-Hive-GPS-Position") hive_gps : String?): Single<Response<ArrayList<SearchedAddress>>>


    @GET("client/mobile/1.0/address/geocoding")
    @Headers("Accept-Language: ru")
    fun geocodePoint(@Header("Hive-Profile") hive_profile : String,
                     @Header("X-Hive-GPS-Position") hive_gps : String?,
                     @Query("query") query : String
    ): Single<Response<ArrayList<SearchedAddress>>>


    @GET("weather")
    fun getWeather(
        @Query("lat") lat : Double,
        @Query("lon") lon : Double,
        @Query("lang") lang : String,
        @Query("appid") appid : String,
        @Query("units") units : String
    ): Single<Response<WeatherResponse>>


    @POST("route?api_key=f105994c-4760-4888-a39b-bb9b2d07f66c")
    fun getRoute(
        @Body getRouteRequest: GetRouteRequest
    ): Single<Response<GetRouteResponse>>








}