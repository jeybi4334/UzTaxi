package me.jeybi.uztaxi.model

import com.google.gson.annotations.SerializedName

data class ReverseGeocodeRequest(
    val lat : Double,
    val lon : Double,
    val zoom : Int,
    val format : String,
    @SerializedName("accept-language")
    val accept_language : String
)