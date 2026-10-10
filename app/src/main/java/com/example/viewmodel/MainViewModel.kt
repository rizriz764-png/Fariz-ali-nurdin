package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AnamnesisDao
import com.example.data.db.AnamnesisRecordEntity
import com.example.data.db.AnamnesisRepository
import com.example.data.db.AppDatabase
import com.example.data.model.SampleVisualCase
import com.example.data.model.SimrsOutput
import com.example.data.remote.GeminiVisualService
import com.example.ui.components.TextToSpeechHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AnamnesisRepository
    private val geminiService = GeminiVisualService()
    private var ttsHelper: TextToSpeechHelper? = null

    // Room DB Records Flow
    val allRecords: StateFlow<List<AnamnesisRecordEntity>>
    private val _allRecordsFlow = MutableStateFlow<List<AnamnesisRecordEntity>>(emptyList())

    // UI States
    private val _selectedTab = MutableStateFlow(0) // 0: Intake, 1: Output, 2: History, 3: Doctor
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _lightingQuality = MutableStateFlow("Baik")
    val lightingQuality: StateFlow<String> = _lightingQuality.asStateFlow()

    private val _framingInstruction = MutableStateFlow("Posisikan objek di tengah bingkai retikel 2D. Jarak ~15 cm.")
    val framingInstruction: StateFlow<String> = _framingInstruction.asStateFlow()

    private val _distanceCm = MutableStateFlow(15)
    val distanceCm: StateFlow<Int> = _distanceCm.asStateFlow()

    private val _selectedSampleCase = MutableStateFlow<SampleVisualCase?>(null)
    val selectedSampleCase: StateFlow<SampleVisualCase?> = _selectedSampleCase.asStateFlow()

    // Progressive Questionnaire Inputs
    private val _keluhanUtamaInput = MutableStateFlow("")
    val keluhanUtamaInput: StateFlow<String> = _keluhanUtamaInput.asStateFlow()

    private val _durasiGejalaInput = MutableStateFlow("")
    val durasiGejalaInput: StateFlow<String> = _durasiGejalaInput.asStateFlow()

    private val _riwayatKambuhInput = MutableStateFlow("")
    val riwayatKambuhInput: StateFlow<String> = _riwayatKambuhInput.asStateFlow()

    private val _currentStep = MutableStateFlow(0) // 0: Frame, 1: Keluhan, 2: Durasi, 3: Riwayat, 4: Complete
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // Output & Analysis State
    private val _simrsOutput = MutableStateFlow<SimrsOutput?>(null)
    val simrsOutput: StateFlow<SimrsOutput?> = _simrsOutput.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // 2-Step Emergency Escalation State
    private val _showEmergencyDialog = MutableStateFlow(false)
    val showEmergencyDialog: StateFlow<Boolean> = _showEmergencyDialog.asStateFlow()

    private val _emergencyDetails = MutableStateFlow("")
    val emergencyDetails: StateFlow<String> = _emergencyDetails.asStateFlow()

    // TTS Voice
    private val _ttsEnabled = MutableStateFlow(true)
    val ttsEnabled: StateFlow<Boolean> = _ttsEnabled.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AnamnesisRepository(database.anamnesisDao())
        allRecords = _allRecordsFlow.asStateFlow()

        viewModelScope.launch {
            repository.allRecords.collectLatest { list ->
                _allRecordsFlow.value = list
            }
        }

        try {
            ttsHelper = TextToSpeechHelper(application)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun setSelectedBitmap(bitmap: Bitmap?) {
        _selectedBitmap.value = bitmap
        // Automatically evaluate framing and lighting on image update
        evaluateFramingAndLighting()
    }

    fun selectSampleCase(sampleCase: SampleVisualCase) {
        _selectedSampleCase.value = sampleCase
        _lightingQuality.value = sampleCase.lightingQuality
        _framingInstruction.value = sampleCase.framingAdvice
        _keluhanUtamaInput.value = sampleCase.suggestedAnamnesis.keluhan_utama
        _durasiGejalaInput.value = sampleCase.suggestedAnamnesis.durasi_gejala
        _riwayatKambuhInput.value = sampleCase.suggestedAnamnesis.riwayat_kambuh

        // If sample is emergency, trigger Phase 2 emergency warning immediately
        if (sampleCase.isCritical) {
            triggerEmergencyEscalation(sampleCase.description)
        }

        speakVoiceGuidance("Poin contoh kasus dipilih: ${sampleCase.title}. ${sampleCase.framingAdvice}")
    }

    fun adjustLightingToggle() {
        val nextQuality = when (_lightingQuality.value) {
            "Baik" -> "Cukup"
            "Cukup" -> "Buruk"
            else -> "Baik"
        }
        _lightingQuality.value = nextQuality
        _framingInstruction.value = when (nextQuality) {
            "Baik" -> "Pencahayaan terang dan terfokus. Siap untuk pengambilan foto."
            "Cukup" -> "Pencahayaan cukup. Arahkan kamera sedikit ke kiri untuk menghindari bayangan."
            else -> "Pencahayaan terlalu gelap. Mohon geser ke tempat terang atau nyalakan lampu kilat."
        }
        speakVoiceGuidance(_framingInstruction.value)
    }

    fun evaluateFramingAndLighting() {
        if (_selectedSampleCase.value == null) {
            _lightingQuality.value = "Baik"
            _framingInstruction.value = "Posisikan lokasi gejala di tengah bingkai retikel 2D. Dekatkan kamera sekitar 10 cm."
            speakVoiceGuidance("Tolong dekatkan kamera sekitar 10 cm dan posisikan di tengah retikel 2D.")
        }
    }

    fun setKeluhanUtama(text: String) {
        _keluhanUtamaInput.value = text
        checkEmergencyKeywords(text)
    }

    fun setDurasiGejala(text: String) {
        _durasiGejalaInput.value = text
    }

    fun setRiwayatKambuh(text: String) {
        _riwayatKambuhInput.value = text
    }

    fun setStep(step: Int) {
        _currentStep.value = step
        when (step) {
            1 -> speakVoiceGuidance("Pertanyaan 1 dari 3: Apa keluhan utama dan tingkat rasa perih atau gatal yang dirasakan?")
            2 -> speakVoiceGuidance("Pertanyaan 2 dari 3: Berapa lama gejala ini sudah dirasakan?")
            3 -> speakVoiceGuidance("Pertanyaan 3 dari 3: Apakah gejala serupa pernah kambuh sebelumnya?")
        }
    }

    private fun checkEmergencyKeywords(text: String) {
        val lower = text.lowercase()
        if (lower.contains("pendarahan") || lower.contains("darah deras") ||
            lower.contains("sesak") || lower.contains("bengkak leher") || lower.contains("pingsan")
        ) {
            triggerEmergencyEscalation("Terdeteksi kata kunci indikator pemburukan fisik kritis pada keluhan pasien.")
        }
    }

    fun triggerEmergencyEscalation(details: String) {
        _emergencyDetails.value = details
        _showEmergencyDialog.value = true
        speakVoiceGuidance("PERINGATAN DARURAT: Sistem mendeteksi indikator pemburukan gejala fisik kritis. Konfirmasi dua langkah diperlukan untuk panggilan ambulans.")
    }

    fun dismissEmergencyDialog() {
        _showEmergencyDialog.value = false
    }

    fun toggleTts() {
        _ttsEnabled.value = !_ttsEnabled.value
        if (!_ttsEnabled.value) {
            ttsHelper?.stop()
        }
    }

    fun speakVoiceGuidance(text: String) {
        if (_ttsEnabled.value) {
            ttsHelper?.speak(text)
        }
    }

    // Phase 3: Execute AI Anamnesis & Generate SIMRS JSON Record
    fun processAnamnesisAndGenerateSimrs() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _currentStep.value = 4 // Completed step
            speakVoiceGuidance("Memproses pencatatan anamnesis visual AI dan menghasilkan rekam medis SIMRS...")

            val output = geminiService.analyzeImageWithGemini(
                bitmap = _selectedBitmap.value,
                keluhanInput = _keluhanUtamaInput.value,
                durasiInput = _durasiGejalaInput.value,
                riwayatInput = _riwayatKambuhInput.value,
                selectedSampleCase = _selectedSampleCase.value
            )

            _simrsOutput.value = output
            _isAnalyzing.value = false
            _selectedTab.value = 1 // Switch to SIMRS output tab

            if (output.status_eskalasi_darurat.indikasi_kritis) {
                triggerEmergencyEscalation(output.status_eskalasi_darurat.rekomendasi_tindakan)
            } else {
                speakVoiceGuidance("Proses anamnesis selesai. Rekam medis SIMRS berhasil dibuat.")
            }
        }
    }

    // Save record to local Room DB
    fun saveCurrentRecordToDb(patientName: String = "Pasien Telemedisin") {
        val output = _simrsOutput.value ?: return
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(SimrsOutput::class.java).indent("  ")
        val rawJson = adapter.toJson(output)

        viewModelScope.launch {
            val entity = AnamnesisRecordEntity(
                patientName = patientName,
                keluhanUtama = output.ringkasan_anamnesis.keluhan_utama,
                durasiGejala = output.ringkasan_anamnesis.durasi_gejala,
                riwayatKambuh = output.ringkasan_anamnesis.riwayat_kambuh,
                lokasiGejala = output.pencatatan_visual_ai.lokasi_gejala,
                deskripsiFisikVisual = output.pencatatan_visual_ai.deskripsi_fisik_visual,
                kualitasPencahayaan = output.pencatatan_visual_ai.kualitas_pencahayaan_gambar,
                indikasiKritis = output.status_eskalasi_darurat.indikasi_kritis,
                rekomendasiTindakan = output.status_eskalasi_darurat.rekomendasi_tindakan,
                rawJsonOutput = rawJson
            )
            repository.insertRecord(entity)
            speakVoiceGuidance("Rekam medis berhasil disimpan ke riwayat lokal.")
        }
    }

    fun verifyRecordByDoctor(recordId: Long, doctorNotes: String) {
        viewModelScope.launch {
            repository.verifyRecordByDoctor(recordId, true, doctorNotes)
            speakVoiceGuidance("Rekam medis telah diverifikasi oleh dokter berlisensi.")
        }
    }

    fun deleteRecordById(recordId: Long) {
        viewModelScope.launch {
            repository.deleteRecordById(recordId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper?.shutdown()
    }
}
