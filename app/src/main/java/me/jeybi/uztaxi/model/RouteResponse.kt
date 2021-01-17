package me.jeybi.uztaxi.model

data class RouteResponse(
    val routes: ArrayList<ModelRoute>
)

data class ModelRoute(
    val distance: Double,
    val geometry: ModelGeometry
)

data class ModelGeometry(
    val coordinates : ArrayList<ArrayList<Double>>
)