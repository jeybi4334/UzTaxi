package me.jeybi.uztaxi.model

data class RouteItem(
    val startPoint: Boolean,
    val address : String,
    val lat : Double,
    val lon : Double
)