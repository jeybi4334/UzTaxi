package me.jeybi.uztaxi.model

data class ShortOrderInfo(
    val id: Long,
    val state : Int,
    val route : ArrayList<SearchedAddress>,
    val assignee : Assignee?,
    val time : String?,
    val needsProlongation : Boolean
)

data class Assignee(
    val car : TaxiCar
)

data class TaxiCar(
    val brand : String,
    val model : String,
    val color: String,
    val regNum: String
)