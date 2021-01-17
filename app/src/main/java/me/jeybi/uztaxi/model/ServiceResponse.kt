package me.jeybi.uztaxi.model

data class ServiceResponse(
    val kind: String,
    val serviceId: String,
    val settings: ServiceSettings,
    val tariffs: ArrayList<ServiceTariff>
)

data class ServiceSettings(
    val cardPaymentAllowed: Boolean,
    val dispatcherCall: DispatcherCall,
    val mainInterface: String,

)

data class ServiceTariff(
    val id: Long,
    val name: String,
    val icon: String,
    val description: String,
    val options: ArrayList<TariffOption>,
    val minCost: Double,
    val costChangeAllowed: Boolean,
    val hint: String,
    val showEstimation: Boolean
)

data class TariffOption(
    val id: Long,
    val name: String,
    val value: Double,
    val mandatory: Boolean
)

data class DispatcherCall(
    val allow: String,
    val number: String
)