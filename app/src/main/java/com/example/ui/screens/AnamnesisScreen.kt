package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.remote.GeminiVisualService
import com.example.ui.components.FramingGuidanceOverlay
import com.example.ui.components.MedicalDisclaimerBar
import com.example.viewmodel.MainViewModel

@Composable
fun AnamnesisScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap by viewModel.selectedBitmap.collectAsState()
    val lightingQuality by viewModel.lightingQuality.collectAsState()
    val framingInstruction by viewModel.framingInstruction.collectAsState()
    val distanceCm by viewModel.distanceCm.collectAsState()

    val keluhanInput by viewModel.keluhanUtamaInput.collectAsState()
    val durasiInput by viewModel.durasiGejalaInput.collectAsState()
    val riwayatInput by viewModel.riwayatKambuhInput.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val ttsEnabled by viewModel.ttsEnabled.collectAsState()

    var showSampleDialog by remember { mutableStateOf(false) }

    // Gallery Photo Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val selectedBm = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val tempBytes = inputStream.readBytes()
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeByteArray(tempBytes, 0, tempBytes.size, options)
                    
                    var inSampleSize = 1
                    val reqHeight = 1024
                    val reqWidth = 1024
                    if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                        val halfHeight = options.outHeight / 2
                        val halfWidth = options.outWidth / 2
                        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                            inSampleSize *= 2
                        }
                    }
                    
                    val decodeOptions = BitmapFactory.Options().apply {
                        this.inSampleSize = inSampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    BitmapFactory.decodeByteArray(tempBytes, 0, tempBytes.size, decodeOptions)
                }
                viewModel.setSelectedBitmap(selectedBm)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { capturedBm ->
        capturedBm?.let {
            viewModel.setSelectedBitmap(it)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Medical Disclaimer Banner
        MedicalDisclaimerBar()

        // App Logo & Header Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_medical_ai),
                    contentDescription = "Logo Love & Healthy",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Love & Healthy",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    Text(
                        text = "Asisten Kesehatan Visual",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    )
                }
                
                IconButton(
                    onClick = { viewModel.toggleTts() },
                    modifier = Modifier.testTag("toggle_tts_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Panduan Suara",
                        tint = if (ttsEnabled) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // FASE 1: Framing Guidance Overlay
        Text(
            text = "FASE 1: EVALUASI BINGKAI FOTO 2D & PENCAHAYAAN",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        FramingGuidanceOverlay(
            bitmap = bitmap,
            lightingQuality = lightingQuality,
            framingInstruction = framingInstruction,
            distanceCm = distanceCm,
            onCaptureClick = { cameraLauncher.launch(null) },
            onSelectSampleClick = { showSampleDialog = true },
            onAdjustLightingClick = { viewModel.adjustLightingToggle() }
        )

        // Photo Source Picker Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("camera_launch_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ambil Foto", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("gallery_launch_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pilih Galeri", fontSize = 12.sp)
            }
        }

        // Progressive Questionnaire Progress
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "FASE 1: ANAMNESIS INTERAKTIF (LANGKAH ${currentStep + 1}/4)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${((currentStep + 1) * 25)}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (currentStep + 1) / 4f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // STEP 1: Keluhan Utama
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (currentStep == 1) Color(0xFFBFCBAD) else Color(0xFFDEE5D8),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (currentStep == 1) Color(0xFFE7F3DC) else Color(0xFFFFFFFF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "1. Keluhan Utama & Sensasi Nyeri/Gatal",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Deskripsikan gejala fisik yang terlihat, intensitas gatal/perih, atau adanya pendarahan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = keluhanInput,
                    onValueChange = { viewModel.setKeluhanUtama(it) },
                    placeholder = { Text("Contoh: Ruam kemerahan terasa gatal dan sedikit panas di lengan kanan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("keluhan_utama_input"),
                    shape = RoundedCornerShape(16.dp)
                )

                // Quick preset chips for keluhan
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val quickChips = listOf(
                        "Ruam kemerahan & gatal",
                        "Luka lecet perih",
                        "Pendarahan aktif",
                        "Bibir & muka membengkak",
                        "Kulit mengelupas & kering"
                    )
                    items(quickChips) { chip ->
                        FilterChip(
                            selected = keluhanInput.contains(chip),
                            onClick = {
                                val newText = if (keluhanInput.isBlank()) chip else "$keluhanInput, $chip"
                                viewModel.setKeluhanUtama(newText)
                            },
                            label = { Text(chip, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // STEP 2: Durasi Gejala
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (currentStep == 2) Color(0xFFBFCBAD) else Color(0xFFDEE5D8),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (currentStep == 2) Color(0xFFE7F3DC) else Color(0xFFFFFFFF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "2. Durasi & Onset Gejala",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Berapa lama gejala ini sudah dirasakan oleh pasien?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = durasiInput,
                    onValueChange = { viewModel.setDurasiGejala(it) },
                    placeholder = { Text("Contoh: Sudah 3 hari / Baru saja 1 jam yang lalu") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("durasi_gejala_input"),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val durationChips = listOf("Kurang dari 24 jam", "1-3 hari", "1 minggu", "> 1 bulan")
                    items(durationChips) { dur ->
                        FilterChip(
                            selected = durasiInput == dur,
                            onClick = { viewModel.setDurasiGejala(dur) },
                            label = { Text(dur, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // STEP 3: Riwayat Kambuh & Medis
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (currentStep == 3) Color(0xFFBFCBAD) else Color(0xFFDEE5D8),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (currentStep == 3) Color(0xFFE7F3DC) else Color(0xFFFFFFFF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "3. Riwayat Kambuh & Alergi",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Apakah gejala ini pernah kambuh sebelumnya atau ada riwayat alergi?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = riwayatInput,
                    onValueChange = { viewModel.setRiwayatKambuh(it) },
                    placeholder = { Text("Contoh: Pernah kambuh 6 bulan lalu pasca makan udang") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("riwayat_kambuh_input"),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val historyChips = listOf("Tidak ada riwayat", "Pernah kambuh sebelumnya", "Alergi makanan/obat", "Riwayat asma/atopi")
                    items(historyChips) { hist ->
                        FilterChip(
                            selected = riwayatInput == hist,
                            onClick = { viewModel.setRiwayatKambuh(hist) },
                            label = { Text(hist, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // Final Process Button (Phase 3 Trigger)
        Button(
            onClick = { viewModel.processAnamnesisAndGenerateSimrs() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("process_anamnesis_button"),
            enabled = !isAnalyzing,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Menganalisis Visual AI...", fontWeight = FontWeight.Bold)
            } else {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Proses Anamnesis & Buat SIMRS JSON", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Sample Preset Cases Selection Dialog
    if (showSampleDialog) {
        Dialog(onDismissRequest = { showSampleDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pilih Contoh Foto & Kasus Visual",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Gunakan sampel kasus medis teruji untuk evaluasi cepat:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GeminiVisualService.SAMPLE_CASES.forEach { caseItem ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectSampleCase(caseItem)
                                        showSampleDialog = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (caseItem.isCritical) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (caseItem.isCritical) Icons.Default.Warning else Icons.Default.MedicalServices,
                                        contentDescription = null,
                                        tint = if (caseItem.isCritical) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = caseItem.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (caseItem.isCritical) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${caseItem.category} • ${caseItem.description}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showSampleDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batal")
                    }
                }
            }
        }
    }
}
