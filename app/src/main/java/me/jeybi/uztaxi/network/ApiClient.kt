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
                       @Header("X-Hive-GPS-Position") hive_gps : String?,
                       @Body registerUserRequest: RegisterUserRequest): Single<Response<RegisterUserResponse>>

    @GET("client/mobile/2.0/address/nearest")
    @Headers("Accept-Language: ru")
    fun getCurrentAddress(@Header("Hive-Profile") hive_profile : String,
                            @Header("X-Hive-GPS-Position") hive_gps : String?): Single<Response<ArrayList<SearchedAddress>>>


}