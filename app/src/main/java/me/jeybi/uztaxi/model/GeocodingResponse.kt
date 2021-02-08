package me.jeybi.uztaxi.model

data class GeocodingResponse(
    val features :  ArrayList<GeocodeFeature>
)

data class GeocodeFeature(
    val type : String,
    val geometry: Geometry,
    val properties : GeocodeProperty
)

data class GeocodeProperty(
    val id : String,
    val layer: String,
    val name : String,
    val housenumber: String?,
    val street : String?,
    val distance: Double?,
    val region: String?,
    val label: String?,
)

data class Geometry(
    val type : String,
    val coordinates : ArrayList<Double>
)
