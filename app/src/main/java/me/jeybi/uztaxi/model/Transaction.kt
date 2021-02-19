package me.jeybi.uztaxi.model

data class Transaction(
    val id : Int,
    val name : String,
    val type : Int,
    val amount : Double,
    val date : String,
    val source : String
)