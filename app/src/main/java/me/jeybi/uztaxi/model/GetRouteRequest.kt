package me.jeybi.uztaxi.model

data class GetRouteRequest(
    val locations : ArrayList<RouteCoordinates>,
    val costing : String?,
    val language : String?,
    val directions_type : String?

)

data class RouteCoordinates(
    val lat : Double,
    val lon : Double,
    val type: String?
)

data class GetRouteResponse(
    val trip : RouteTrip
)

data class RouteTrip(
    val language: String?,
    val legs : ArrayList<RouteLeg>,
    val summary : RouteSummary,
    val locations : ArrayList<RouteCoordinates>
)

data class RouteLeg(
    val shape : String,
    val summary : RouteSummary
)

data class RouteSummary(
    val time : Double,
    val length : Float,
    val max_lon : Double,
    val max_lat : Double,
    val min_lon : Double,
    val min_lat : Double
    )
