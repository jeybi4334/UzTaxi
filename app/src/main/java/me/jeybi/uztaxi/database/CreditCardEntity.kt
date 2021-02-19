package me.jeybi.uztaxi.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.jeybi.uztaxi.utils.Constants


@Entity(tableName = Constants.TABLE_CARDS)
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id  : Int,
    val cardName : String,
    val cardNumber : String,
    val cardExpiry : String,
    val cardDesign : Int
)