package me.jeybi.uztaxi.model

data class ReverseGeocodeResponse(
    val place_id : Long,
    val osm_id : String,
    val lat : String,
    val lon : String,
    val category : String,
    val type : String,
    val addresstype : String,
    val name : String?,
    val display_name : String,
    val address : ReverseGeocodeAddress
)

data class ReverseGeocodeAddress(
    val amenity : String?,
    val house_number : String?,
    val neighbourhood : String?,
    val county:  String?,
    val city:  String?,
    val district:  String?,
    val road:  String?,
    val village : String?,
    val region : String?,
    val country : String?,
    val country_code : String?,

)