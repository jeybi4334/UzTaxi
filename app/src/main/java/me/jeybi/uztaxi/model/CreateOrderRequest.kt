package me.jeybi.uztaxi.model

data class CreateOrderRequest(
    val paymentMethod : PaymentMethod,
    val tariff : Long,
    val options : ArrayList<Long>,
    val route : ArrayList<ClientAddress>,
    val time : OrderTime?,
    val comment: String?,
    val fixCost : Double?,
    val useBonuses : Double?,
    val disableSms: Boolean?,
    val disableVoice: Boolean?,
    val enablePushUpdates :Boolean?,
    val estimationToken: String?

)

data class OrderTime(
    val type : String,
    val value : String
)

data class CreateOrderResponse(
    val id: Long
)
