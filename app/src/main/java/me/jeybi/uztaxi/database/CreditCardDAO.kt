package me.jeybi.uztaxi.database

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single
import me.jeybi.uztaxi.utils.Constants


@Dao
interface CreditCardDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCard(creditCardEntity: CreditCardEntity) : Completable

    @Query("SELECT * FROM ${Constants.TABLE_CARDS} ORDER BY id DESC")
    fun getCreditCards(): Single<List<CreditCardEntity>>

    @Query("DELETE FROM table_cards WHERE id = :cardId")
    fun deleteCard(cardId: Int) : Completable

    @Update
    fun updateCard(cardEntity: CreditCardEntity) : Completable

}