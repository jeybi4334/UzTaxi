package me.jeybi.uztaxi.model

data class EstimateRideRequest(
    val paymentMethod : PaymentMethod,
    val tariff : Long,
    val options : ArrayList<TariffOption>?,
    val route : ArrayList<RouteCoordinates>
)

data class EstimateResponse(
    val cost : EstimateCost,
    val distance : Double
)

data class EstimateCost(
    val type : String,
    val amount: Double,
    val calculation : String

)