package me.jeybi.uztaxi.model

data class SearchedAddress(
    val name : String,
    val components : ArrayList<SearchComponent>?,
    val types : AddressTypes?,
    val position : SearchPosition?
)

data class ClientAddress(
    val address : SearchedAddress,
    val entrance : String?,
    val flat : String?,
    val comment : String?,
    val pickupPointId : Long?
)

data class AddressTypes(
    val pointType : Int?,
    val aliasType : Int?
)

data class SearchComponent(
    val level : Int,
    val name : String
)

data class SearchPosition(
    val lat : Double,
    val lon : Double
)