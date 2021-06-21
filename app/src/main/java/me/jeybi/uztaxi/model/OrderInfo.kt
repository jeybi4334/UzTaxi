package me.jeybi.uztaxi.model

data class OrderInfo(
    val state: Int,
    val route: ArrayList<ClientAddress>,
    val assignee: Asignee?,
    val options: ArrayList<Option>,
    val time: String?,
    val needsProlongation: Boolean?,
    val comment: String?,
    val distance: Double?,
    val cost: Cost,
    val executionTime : String?,
    val usedBonuses : Double?,
    val paymentMethod: PaymentMethod?,
    val costChangeAllowed: Boolean,
    val costChangeStep : Boolean?,
    val isComing : Boolean?,
    val paidWaitingStartsAt : String
    )

data class Cost(
    val type: String,
    val amount: Double,
    val calculation: String,
    val modifier: CostModifier?,
    val fixed : Double?=null,
    val details: ArrayList<CostItem>?
    )

data class CostItem(
    val title : String,
    val cost : Double
)

data class CostModifier(
    val type: String,
    val value: Double
)

data class Option(
    val id: Long,
    val name: String,
    val type: String,
    val value: Double,
    val selected: Boolean
)

data class Asignee(
    val car: Car,
    val location: SearchPosition?,
    val call: AssigneeCall
)

data class Car(
    val alias: String,
    val brand: String,
    val model: String,
    val color: String,
    val regNum: String
)

data class AssigneeCall(
    val allow: String,
    val numbers: ArrayList<String>?
)