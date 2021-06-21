package me.jeybi.uztaxi.model

data class EstimateRideRequest(
    val tariff : Long,
    val paymentMethod : PaymentMethod,
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
    val modifier : EstimateCostModifier?,
    val calculation : String

)


data class EstimateCostModifier(
    val type : String?,
    val value : Double?
)