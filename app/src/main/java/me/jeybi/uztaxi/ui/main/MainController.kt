package me.jeybi.uztaxi.ui.main

interface MainController {
    interface view {
        fun onUserNotAuthenticated()
        fun onUserApproved()
        fun buildAlertMessageNoGps()

        fun onBottomSheetSearchItemClicked()

        fun startFindingCar()
    }
    interface presenter{
        fun checkIfAuthenticated()
        fun requestPermissions()
        fun checkGPS()

    }
}