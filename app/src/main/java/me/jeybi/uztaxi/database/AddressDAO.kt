package me.jeybi.uztaxi.database

import android.location.Address
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single
import me.jeybi.uztaxi.utils.Constants

@Dao
interface AddressDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAddresses(addresses : List<AddressEntity>) : Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAddress(addresses : AddressEntity) : Completable


    @Query("SELECT * FROM ${Constants.TABLE_ADDRESS}")
    fun getAddresses(): Single<List<AddressEntity>>

    @Delete
    fun deleteCar(addressEntity: AddressEntity) : Completable

    @Update
    fun updateCar(addressEntity: AddressEntity)

}