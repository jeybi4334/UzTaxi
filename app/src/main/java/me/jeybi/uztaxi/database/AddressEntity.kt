package me.jeybi.uztaxi.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.jeybi.uztaxi.utils.Constants

@Entity(tableName = Constants.TABLE_ADDRESS)
data class AddressEntity(
    @PrimaryKey(autoGenerate = true)
    val id  : Int,
    val latitude : Double,
    val longitude : Double,
    val title : String,
    val block : Int,
    val address : String
)