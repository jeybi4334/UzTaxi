package me.jeybi.uztaxi.model

data class RegisterUserRequest(
    val confirmationType : String,
    val phone: String,
    val info : ClientInfo?,
    val referralCode : String?
)

data class ClientInfo(
//    val lastName : String?,
    val firstName : String?,
//    val middleName: String?,
//    val gender : Int?,
//    val birthDate : String?
)

data class RegisterUserResponse(
    val id : Int?,
    val code : Int?,
    val message : String?
)