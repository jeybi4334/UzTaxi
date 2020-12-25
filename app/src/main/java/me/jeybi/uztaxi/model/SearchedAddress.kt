package me.jeybi.uztaxi.model

data class SearchedAddress(
    val name : String,
    val components : ArrayList<SearchComponent>,
    val position : SearchPosition
)

data class SearchComponent(
    val level : Int,
    val name : String
)

data class SearchPosition(
    val lat : Double,
    val lon : Double
)