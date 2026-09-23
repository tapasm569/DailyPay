package com.dailypay.app.data.local.dao

import androidx.room.*
import com.dailypay.app.data.local.entity.DailyDueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyDueDao {

    @Query("SELECT * FROM daily_dues WHERE lenderId = :lenderId")
    fun getDailyDuesFlow(lenderId: String): Flow<List<DailyDueEntity>>

    @Query("SELECT * FROM daily_dues WHERE lenderId = :lenderId")
    suspend fun getDailyDuesDirect(lenderId: String): List<DailyDueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DailyDueEntity>)

    @Query("DELETE FROM daily_dues WHERE lenderId = :lenderId")
    suspend fun deleteForLender(lenderId: String)
}
