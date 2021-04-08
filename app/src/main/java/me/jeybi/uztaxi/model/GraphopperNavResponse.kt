package me.jeybi.uztaxi.model

import com.google.gson.annotations.SerializedName

data class GraphopperNavResponse(
    val hints : NavHints,
    val paths : ArrayList<NavPath>
)

data class NavPath(
    val instructions: ArrayList<NavInstuction>,
    val distance : Double,
    val bbox: ArrayList<Double>,
    val time : Double,
    val points: String,
    val snapped_waypoints : String

    )

data class NavInstuction(
    val distance : Double
)

data class NavHints(
    @SerializedName("visited_nodes.average")
    val visited_nodes_average : Double,
    @SerializedName("visited_nodes.sum")
    val visited_nodes_sum : Double,

)