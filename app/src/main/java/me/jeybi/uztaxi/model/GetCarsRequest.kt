package me.jeybi.uztaxi.model

data class GetCarsRequest(
    val paymentMethod : CarPaymentMethod,
    val tariff : Long
)

data class CarPaymentMethod(
    val kind : String
)

data class GetCarResponse(
    val id : Long,
    val location : RouteCoordinates
)