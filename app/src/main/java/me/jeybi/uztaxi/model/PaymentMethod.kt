package me.jeybi.uztaxi.model

data class PaymentMethod(
    val kind: String,
    val id : Long?,
    val name : String?,
    val enoughMoney: Boolean?
)