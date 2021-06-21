package me.jeybi.uztaxi.model

data class WeatherResponse(
    val coord : WeatherCoordinates,
    val weather : ArrayList<WeatherType>,
    val main : WeatherMain,
    val name : String
)

data class WeatherCoordinates(
    val lat : Double,
    val lon : Double
)

data class WeatherType(
    val id : Int,
    val main : String,
    val description: String
)

data class WeatherMain(
    val temp : Double,

)