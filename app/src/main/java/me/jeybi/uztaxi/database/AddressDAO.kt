package me.jeybi.uztaxi.database

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single
import me.jeybi.uztaxi.utils.Constants

@Dao
interface AddressDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAddresses(addresses : List<AddressEntity>) : Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAddress(addresses : AddressEntity) : Single<Long>


    @Query("SELECT * FROM ${Constants.TABLE_ADDRESS} ORDER BY position")
    fun getAddresses(): Single<List<AddressEntity>>

    @Query("DELETE FROM table_address WHERE id = :id")
    fun deleteAddress(id: Int) : Completable


    @Query("UPDATE table_address SET position = :position WHERE id = :id")
    fun updateAddress(id : Int , position : Long) : Completable

    @Update
    fun updateAddresses(addresses: List<AddressEntity>)

}