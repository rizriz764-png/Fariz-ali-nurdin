package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RingkasanAnamnesis(
    val keluhan_utama: String = "",
    val durasi_gejala: String = "",
    val riwayat_kambuh: String = ""
)

@JsonClass(generateAdapter = true)
data class PencatatanVisualAi(
    val lokasi_gejala: String = "",
    val deskripsi_fisik_visual: String = "",
    val kualitas_pencahayaan_gambar: String = "Baik" // Baik / Cukup / Buruk
)

@JsonClass(generateAdapter = true)
data class StatusEskalasiDarurat(
    val indikasi_kritis: Boolean = false,
    val rekomendasi_tindakan: String = ""
)

@JsonClass(generateAdapter = true)
data class SimrsOutput(
    val ringkasan_anamnesis: RingkasanAnamnesis = RingkasanAnamnesis(),
    val pencatatan_visual_ai: PencatatanVisualAi = PencatatanVisualAi(),
    val status_eskalasi_darurat: StatusEskalasiDarurat = StatusEskalasiDarurat(),
    val disclaimer: String = "Data ini adalah pencatatan awal asistensi AI untuk diverifikasi ulang oleh dokter berlisensi."
)

data class SampleVisualCase(
    val id: String,
    val title: String,
    val category: String,
    val isCritical: Boolean,
    val description: String,
    val lightingQuality: String,
    val framingAdvice: String,
    val suggestedAnamnesis: RingkasanAnamnesis,
    val visualAiRecord: PencatatanVisualAi
)
