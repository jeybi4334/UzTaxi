package me.jeybi.uztaxi.ui.auth

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.jeybi.uztaxi.model.ErrorObj
import me.jeybi.uztaxi.model.RegisterUserRequest
import me.jeybi.uztaxi.network.RetrofitHelper
import me.jeybi.uztaxi.utils.Constants


class AuthenticationPresenter(val view: AuthenticationActivity) :
    AuthenticationController.presenter {

    var PHONE_NUMBER = ""
    var USER_ID = 0

    override fun sendSmsToPhoneNumber(
        phoneNumber: String,
        phoneNumberListener: AuthenticationController.OnErrorListener
    ): Disposable {
        PHONE_NUMBER = phoneNumber
        val registerUserRequest =
            RegisterUserRequest(Constants.CONF_TYPE_SMS, phoneNumber, null, null)

        var sendSmsDisposable : Disposable? = null
        sendSmsDisposable = RetrofitHelper.apiService()
            .registerClient(Constants.HIVE_PROFILE, null, registerUserRequest)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                sendSmsDisposable!!.dispose()
                when (it.code()) {
                    Constants.STATUS_SUCCESSFUL -> {
                        if (it.isSuccessful && it.body() != null) {
                            USER_ID = it.body()!!.id ?: 0
                            view.onSmsSent(USER_ID)
                        }
                    }
                    Constants.STATUS_BAD_REQUEST -> {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorObj>() {}.type
                        val errorResponse: ErrorObj? =
                            gson.fromJson(it.errorBody()!!.charStream(), type)
                        if (errorResponse != null)
                            phoneNumberListener.onErrorOccured(errorResponse.message)
                    }
                    Constants.STATUS_SERVER_ERROR -> {
                        phoneNumberListener.onErrorOccured("Server Error")
                    }
                }
            }, {
                sendSmsDisposable!!.dispose()
            })
        return sendSmsDisposable
    }

    override fun resendSmsCode(onErrorListener: AuthenticationController.OnErrorListener): Disposable {
        var resendDisposable : Disposable? = null
        resendDisposable = RetrofitHelper.apiService()
            .resendSmsCode(Constants.HIVE_PROFILE, USER_ID, Constants.CONF_TYPE_SMS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                resendDisposable!!.dispose()
                when (it.code()) {
                    Constants.STATUS_SUCCESSFUL -> {
                        onErrorListener.onErrorOccured("no")
                    }
                    Constants.STATUS_BAD_REQUEST -> {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorObj>() {}.type
                        val errorResponse: ErrorObj? =
                            gson.fromJson(it.errorBody()!!.charStream(), type)
                        if (errorResponse != null)
                            onErrorListener.onErrorOccured(errorResponse.message)
                    }
                    Constants.STATUS_SERVER_ERROR -> {

                    }
                }
            }, {
                resendDisposable!!.dispose()
            })
        return resendDisposable
    }

    override fun confirmSmsCode(
        code: String,
        onErrorListener: AuthenticationController.OnErrorListener
    ): Disposable {
        var confirmDisposable : Disposable?=null
        confirmDisposable = RetrofitHelper.apiService()
            .confirmSmsCode(Constants.HIVE_PROFILE, USER_ID, code)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                confirmDisposable!!.dispose()
                Log.d("ASDASDA","${it.code()}")
                when (it.code()) {
                    Constants.STATUS_SUCCESSFUL -> {
                        view.sharedPreferences.edit().putLong(Constants.HIVE_USER_ID,it.body()!!.id).apply()
                        view.sharedPreferences.edit().putString(Constants.HIVE_USER_TOKEN,it.body()!!.key).apply()
                        view.onUserVerified()
                    }
                    Constants.STATUS_BAD_REQUEST -> {
                        val gson = Gson()
                        val type = object : TypeToken<ErrorObj>() {}.type
                        val errorResponse: ErrorObj? =
                            gson.fromJson(it.errorBody()!!.charStream(), type)
                        if (errorResponse != null)
                            onErrorListener.onErrorOccured(errorResponse.message)
                    }
                    Constants.STATUS_SERVER_ERROR -> {

                    }
                }
            }, {
                confirmDisposable!!.dispose()
            })

        return confirmDisposable
    }


}