package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnamnesisDao {
    @Query("SELECT * FROM anamnesis_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AnamnesisRecordEntity>>

    @Query("SELECT * FROM anamnesis_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): AnamnesisRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AnamnesisRecordEntity): Long

    @Update
    suspend fun updateRecord(record: AnamnesisRecordEntity)

    @Query("DELETE FROM anamnesis_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("UPDATE anamnesis_records SET isVerifiedByDoctor = :verified, doctorNotes = :notes WHERE id = :id")
    suspend fun verifyRecordByDoctor(id: Long, verified: Boolean, notes: String)
}
