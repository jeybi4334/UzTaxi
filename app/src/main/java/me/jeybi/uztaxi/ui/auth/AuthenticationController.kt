package me.jeybi.uztaxi.ui.auth

import io.reactivex.disposables.Disposable

interface AuthenticationController {

    interface view {
        fun onPhoneNumberEntered(phoneNumber: String,phoneNumberListener: OnErrorListener)
        fun onSmsSent(id : Int)
        fun onResendClicked(onErrorListener: OnErrorListener)
        fun onVerifyClicked(code : String,onErrorListener: OnErrorListener)
        fun onUserVerified()
    }

    interface presenter {
        fun sendSmsToPhoneNumber(phoneNumber: String,phoneNumberListener: OnErrorListener) : Disposable
        fun resendSmsCode(onErrorListener: OnErrorListener) : Disposable
        fun confirmSmsCode(code : String,onErrorListener: OnErrorListener) : Disposable
    }

    interface OnErrorListener{
        fun onErrorOccured(message : String)
    }
}