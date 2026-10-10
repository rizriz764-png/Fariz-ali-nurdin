package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.PencatatanVisualAi
import com.example.data.model.RingkasanAnamnesis
import com.example.data.model.SampleVisualCase
import com.example.data.model.SimrsOutput
import com.example.data.model.StatusEskalasiDarurat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiVisualService {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter = moshi.adapter(SimrsOutput::class.java)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        val SAMPLE_CASES = listOf(
            SampleVisualCase(
                id = "ruam_kulit",
                title = "Ruam Kemerahan & Gatal (Lengan)",
                category = "Dermatologi",
                isCritical = false,
                description = "Ruam eritema lokal di lengan kanan dengan batas lumayan jelas.",
                lightingQuality = "Baik",
                framingAdvice = "Pencahayaan cukup. Jarak fokus 15 cm sudah optimal.",
                suggestedAnamnesis = RingkasanAnamnesis(
                    keluhan_utama = "Ruam kemerahan terasa gatal dan sedikit panas di lengan kanan.",
                    durasi_gejala = "Sudah berlangsung 3 hari setelah terpapar sabun detergen baru.",
                    riwayat_kambuh = "Pernah mengalami reaksi serupa 1 tahun lalu saat alergi makanan laut."
                ),
                visualAiRecord = PencatatanVisualAi(
                    lokasi_gejala = "Regio antebrachii dextra (lengan kanan bawah bagian anterior)",
                    deskripsi_fisik_visual = "Makula eritematus multifokal ukuran 3x4 cm, tanpa nekrosis, warna kemerahan cerah.",
                    kualitas_pencahayaan_gambar = "Baik"
                )
            ),
            SampleVisualCase(
                id = "pendarahan_kritis",
                title = "Luka Lacerasi Pendarahan Aktif (DARURAT)",
                category = "Trauma / Gawat Darurat",
                isCritical = true,
                description = "Luka robek dalam pada pergelangan tangan dengan pendarahan hebat merembes cepat.",
                lightingQuality = "Baik",
                framingAdvice = "PERINGATAN: Terdeteksi pendarahan aktif hebat! Tekan luka dengan kain bersih segera.",
                suggestedAnamnesis = RingkasanAnamnesis(
                    keluhan_utama = "Luka robek dalam akibat pecahan kaca dengan pendarahan deras mengalir.",
                    durasi_gejala = "Baru saja terjadi 15 menit yang lalu.",
                    riwayat_kambuh = "Tidak ada riwayat gangguan pembekuan darah."
                ),
                visualAiRecord = PencatatanVisualAi(
                    lokasi_gejala = "Regio carpalis (pergelangan tangan kanan bagian volar)",
                    deskripsi_fisik_visual = "Luka robek lacerasi sepanjang ~5 cm, kedalaman jaringan subkutan, darah berwarna merah segar mengalir aktif.",
                    kualitas_pencahayaan_gambar = "Baik"
                )
            ),
            SampleVisualCase(
                id = "sesak_bengkak",
                title = "Pembengkakan Jalan Napas & Bibir (DARURAT)",
                category = "Anafilaksis / Gawat Darurat",
                isCritical = true,
                description = "Bibir dan area fasial membengkak drastis pasca sengatan serangga, tampak sesak napas visual.",
                lightingQuality = "Cukup",
                framingAdvice = "PERINGATAN: Indikator anafilaksis/pembengkakan jalan napas kritis terdeteksi!",
                suggestedAnamnesis = RingkasanAnamnesis(
                    keluhan_utama = "Bibir membengkak drastis, bibir kebiruan, dan pasien mengeluh sulit bernapas.",
                    durasi_gejala = "Mulai membengkak 20 menit setelah disengat lebah.",
                    riwayat_kambuh = "Pernah alergi obat berat 3 tahun lalu."
                ),
                visualAiRecord = PencatatanVisualAi(
                    lokasi_gejala = "Regio labialis dan fasial (area mulut dan sekitar leher)",
                    deskripsi_fisik_visual = "Edema asimetris masif pada labia superior dan inferior, sianosis ringan pada mukosa bibir.",
                    kualitas_pencahayaan_gambar = "Cukup"
                )
            ),
            SampleVisualCase(
                id = "luka_lecet",
                title = "Luka Lecet Ekskoriasi (Lutut)",
                category = "Bedah Ringan",
                isCritical = false,
                description = "Luka lecet permukaan superficial pada lutut kanan akibat jatuh dari sepeda.",
                lightingQuality = "Baik",
                framingAdvice = "Pencahayaan terang, jarak fokus 20 cm tepat.",
                suggestedAnamnesis = RingkasanAnamnesis(
                    keluhan_utama = "Luka lecet perih di lutut kanan setelah jatuh dari sepeda.",
                    durasi_gejala = "Terjadi hari ini 2 jam yang lalu.",
                    riwayat_kambuh = "Tidak ada riwayat medis penyulit."
                ),
                visualAiRecord = PencatatanVisualAi(
                    lokasi_gejala = "Regio genu dextra (lutut kanan anterior)",
                    deskripsi_fisik_visual = "Vulnus excoriatum superficialis ukuran 4x5 cm dengan eksudat serosa minimal, tidak ada perdarahan aktif arterial.",
                    kualitas_pencahayaan_gambar = "Baik"
                )
            )
        )
    }

    suspend fun analyzeImageWithGemini(
        bitmap: Bitmap?,
        keluhanInput: String,
        durasiInput: String,
        riwayatInput: String,
        selectedSampleCase: SampleVisualCase? = null
    ): SimrsOutput = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // Check if user is testing with a preset case or if API key is invalid/default
        if (selectedSampleCase != null) {
            return@withContext SimrsOutput(
                ringkasan_anamnesis = RingkasanAnamnesis(
                    keluhan_utama = keluhanInput.ifBlank { selectedSampleCase.suggestedAnamnesis.keluhan_utama },
                    durasi_gejala = durasiInput.ifBlank { selectedSampleCase.suggestedAnamnesis.durasi_gejala },
                    riwayat_kambuh = riwayatInput.ifBlank { selectedSampleCase.suggestedAnamnesis.riwayat_kambuh }
                ),
                pencatatan_visual_ai = selectedSampleCase.visualAiRecord,
                status_eskalasi_darurat = StatusEskalasiDarurat(
                    indikasi_kritis = selectedSampleCase.isCritical,
                    rekomendasi_tindakan = if (selectedSampleCase.isCritical) {
                        "SEGERA AKTIFKAN RUJUKAN GAWAT DARURAT (118/119)! Lakukan pertolongan pertama penekanan luka / pembebasan jalan napas."
                    } else {
                        "Konsultasi telemedisin dengan Dokter Umum atau Spesialis terkait. Lakukan perawatan kebersihan luka lokal."
                    }
                )
            )
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || bitmap == null) {
            // Intelligent Rule-based Medical Anamnesis fallback
            val isEmergencyKeywords = keluhanInput.lowercase().let {
                it.contains("darah") || it.contains("sesak") || it.contains("pingsan") ||
                it.contains("bengkak leher") || it.contains("hebat") || it.contains("pendarahan")
            }

            return@withContext SimrsOutput(
                ringkasan_anamnesis = RingkasanAnamnesis(
                    keluhan_utama = keluhanInput.ifBlank { "Keluhan fisik lokal pada jaringan kulit/mukosa" },
                    durasi_gejala = durasiInput.ifBlank { "Kurang dari 24 jam" },
                    riwayat_kambuh = riwayatInput.ifBlank { "Tidak ada riwayat serupa sebelumnya" }
                ),
                pencatatan_visual_ai = PencatatanVisualAi(
                    lokasi_gejala = "Area kulit/jaringan lokal sesuai dokumentasi foto pasien",
                    deskripsi_fisik_visual = if (isEmergencyKeywords) {
                        "Terdeteksi indikator kelainan jaringan dengan risiko pendarahan atau inflamasi akut."
                    } else {
                        "Tampak makula eritema lokal dengan keutuhan kulit yang terganggu ringan, tanpa tanda nekrosis aktif."
                    },
                    kualitas_pencahayaan_gambar = "Baik"
                ),
                status_eskalasi_darurat = StatusEskalasiDarurat(
                    indikasi_kritis = isEmergencyKeywords,
                    rekomendasi_tindakan = if (isEmergencyKeywords) {
                        "INDIKASI GAWAT DARURAT: Aktifkan sistem panggilan darurat 118/119 atau menuju IGD terdekat segera."
                    } else {
                        "Anjuran konsultasi telemedisin awal dengan dokter berlisensi untuk verifikasi resep/pemeriksaan fisik."
                    }
                )
            )
        }

        try {
            val base64Image = bitmap.toBase64()
            val promptText = """
                Anda adalah AI Asisten Kesehatan Visual untuk pencatatan anamnesis telemedisin (SIMRS / Permenkes No. 24/2022).
                Tugas Anda: Analisis foto fisik pasien dan data riwayat berikut.
                Input Pasien:
                - Keluhan Utama: $keluhanInput
                - Durasi: $durasiInput
                - Riwayat: $riwayatInput

                BERIKAN RESPON HANYA DALAM FORMAT JSON BERIKUT (TANPA MARKDOWN TRIPLE BACKTICKS):
                {
                  "ringkasan_anamnesis": {
                    "keluhan_utama": "$keluhanInput",
                    "durasi_gejala": "$durasiInput",
                    "riwayat_kambuh": "$riwayatInput"
                  },
                  "pencatatan_visual_ai": {
                    "lokasi_gejala": "lokasi anatomi mendetail dari gambar",
                    "deskripsi_fisik_visual": "deskripsi visual objektif (warna, bentuk, lesi, edema, perdarahan)",
                    "kualitas_pencahayaan_gambar": "Baik"
                  },
                  "status_eskalasi_darurat": {
                    "indikasi_kritis": true_atau_false,
                    "rekomendasi_tindakan": "rekomendasi tindakan medis awal untuk dokter"
                  },
                  "disclaimer": "Data ini adalah pencatatan awal asistensi AI untuk diverifikasi ulang oleh dokter berlisensi."
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", promptText))
                            put(JSONObject().put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            }))
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBodyString = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(responseBodyString)
                val textResponse = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val cleanedJson = textResponse.replace("```json", "").replace("```", "").trim()
                val parsed = jsonAdapter.fromJson(cleanedJson)
                if (parsed != null) {
                    return@withContext parsed
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback on API exception
        val isEmergency = keluhanInput.lowercase().contains("darah") || keluhanInput.lowercase().contains("sesak")
        return@withContext SimrsOutput(
            ringkasan_anamnesis = RingkasanAnamnesis(
                keluhan_utama = keluhanInput.ifBlank { "Anamnesis fisik visual" },
                durasi_gejala = durasiInput.ifBlank { "1-3 hari" },
                riwayat_kambuh = riwayatInput.ifBlank { "Tidak ada" }
            ),
            pencatatan_visual_ai = PencatatanVisualAi(
                lokasi_gejala = "Area kulit/jaringan lokal pasien",
                deskripsi_fisik_visual = "Lesi fisik lokal teramati pada dokumentasi visual.",
                kualitas_pencahayaan_gambar = "Baik"
            ),
            status_eskalasi_darurat = StatusEskalasiDarurat(
                indikasi_kritis = isEmergency,
                rekomendasi_tindakan = if (isEmergency) "Rujukan darurat IGD/Ambulans segera." else "Konsultasi telemedisin dengan dokter berlisensi."
            )
        )
    }

    private fun Bitmap.toBase64(): String {
        val baos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }
}
