package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anamnesis_records")
data class AnamnesisRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val patientName: String = "Pasien Telemedisin",
    val keluhanUtama: String,
    val durasiGejala: String,
    val riwayatKambuh: String,
    val lokasiGejala: String,
    val deskripsiFisikVisual: String,
    val kualitasPencahayaan: String,
    val indikasiKritis: Boolean,
    val rekomendasiTindakan: String,
    val rawJsonOutput: String,
    val imagePath: String? = null,
    val isVerifiedByDoctor: Boolean = false,
    val doctorNotes: String = ""
)
