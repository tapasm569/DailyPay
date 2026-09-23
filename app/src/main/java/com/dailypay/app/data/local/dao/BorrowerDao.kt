package com.dailypay.app.data.local.dao

import androidx.room.*
import com.dailypay.app.data.local.entity.BorrowerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BorrowerDao {

    @Query("SELECT * FROM borrowers WHERE lenderId = :lenderId ORDER BY name ASC")
    fun getBorrowersFlow(lenderId: String): Flow<List<BorrowerEntity>>

    @Query("SELECT * FROM borrowers WHERE lenderId = :lenderId ORDER BY name ASC")
    suspend fun getBorrowersDirect(lenderId: String): List<BorrowerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(borrowers: List<BorrowerEntity>)

    @Query("DELETE FROM borrowers WHERE id = :borrowerId")
    suspend fun deleteById(borrowerId: String)

    @Query("DELETE FROM borrowers WHERE lenderId = :lenderId")
    suspend fun deleteForLender(lenderId: String)
}
