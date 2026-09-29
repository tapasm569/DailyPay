package com.dailypay.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dailypay.app.data.local.dao.BorrowerDao
import com.dailypay.app.data.local.dao.DailyDueDao
import com.dailypay.app.data.local.entity.BorrowerEntity
import com.dailypay.app.data.local.entity.DailyDueEntity

@Database(
    entities = [
        BorrowerEntity::class,
        DailyDueEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DailyPayDatabase : RoomDatabase() {

    abstract fun borrowerDao(): BorrowerDao
    abstract fun dailyDueDao(): DailyDueDao

    companion object {
        @Volatile
        private var INSTANCE: DailyPayDatabase? = null

        fun getDatabase(context: Context): DailyPayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DailyPayDatabase::class.java,
                    "dailypay_local.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
