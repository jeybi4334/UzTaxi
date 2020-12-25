package me.jeybi.uztaxi.ui.main

import io.reactivex.disposables.Disposable

interface MainController {
    interface view {
        fun onUserNotAuthenticated()
        fun onUserApproved()
        fun buildAlertMessageNoGps()

        fun onBottomSheetSearchItemClicked()

        fun startFindingCar()

        fun onCancelSearchClicked()

        fun onRideStarted()

        fun editRideClicked()

        fun onCancelRideClicked()

        fun onAddressFound(name : String)
    }
    interface presenter{
        fun checkIfAuthenticated()
        fun requestPermissions()
        fun checkGPS()
        fun registerFCMToken(token : String) : Disposable

        fun findCurrentAddress(latitude : Double, longitude : Double) : Disposable
    }
}