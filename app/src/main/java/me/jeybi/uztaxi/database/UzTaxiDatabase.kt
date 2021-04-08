package me.jeybi.uztaxi.database

import android.content.Context
import android.os.Environment
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import me.jeybi.uztaxi.utils.Constants
import java.lang.String


@Database(
    version = Constants.DATABASE_VERSION,
    entities = [AddressEntity::class, CreditCardEntity::class]
)
abstract class UzTaxiDatabase : RoomDatabase(){

    abstract fun getAddressDAO() : AddressDAO
    abstract fun getCardDAO() : CreditCardDAO

    companion object {
        @Volatile
        private var databseInstance: UzTaxiDatabase? = null

        fun getDatabasenIstance(mContext: Context): UzTaxiDatabase =
            databseInstance ?: synchronized(this) {
                databseInstance ?: buildDatabaseInstance(mContext).also {
                    databseInstance = it
                }
            }


        private fun buildDatabaseInstance(mContext: Context) =
            Room.databaseBuilder(mContext, UzTaxiDatabase::class.java, Constants.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()

                .build()

    }

}