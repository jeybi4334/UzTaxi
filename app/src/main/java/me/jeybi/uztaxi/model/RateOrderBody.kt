package me.jeybi.uztaxi.model

data class RateOrderBody(
    val rate : Int,
    val suggestions : ArrayList<String>?,
    val comment : String?
)