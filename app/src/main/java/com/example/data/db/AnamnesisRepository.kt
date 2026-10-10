package com.example.data.db

import kotlinx.coroutines.flow.Flow

class AnamnesisRepository(private val dao: AnamnesisDao) {
    val allRecords: Flow<List<AnamnesisRecordEntity>> = dao.getAllRecords()

    suspend fun getRecordById(id: Long): AnamnesisRecordEntity? = dao.getRecordById(id)

    suspend fun insertRecord(record: AnamnesisRecordEntity): Long = dao.insertRecord(record)

    suspend fun updateRecord(record: AnamnesisRecordEntity) = dao.updateRecord(record)

    suspend fun deleteRecordById(id: Long) = dao.deleteRecordById(id)

    suspend fun verifyRecordByDoctor(id: Long, verified: Boolean, notes: String) {
        dao.verifyRecordByDoctor(id, verified, notes)
    }
}
