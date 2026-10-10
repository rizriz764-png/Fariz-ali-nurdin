package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MedicalDisclaimerBar
import com.example.ui.components.SimrsJsonCard
import com.example.viewmodel.MainViewModel

@Composable
fun SimrsOutputScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val simrsOutput by viewModel.simrsOutput.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Medical Disclaimer Bar
        MedicalDisclaimerBar()

        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MedicalInformation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Output Rekam Medis SIMRS",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (simrsOutput == null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Belum Ada Data Anamnesis Terproses",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Silakan lakukan proses foto & jawab pertanyaan anamnesis di tab 'Input Anamnesis' terlebih dahulu.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.setTab(0) },
                        modifier = Modifier.testTag("goto_input_tab_button")
                    ) {
                        Text("Mulai Input Anamnesis")
                    }
                }
            }
        } else {
            val output = simrsOutput!!

            // Urgency Status Alert Banner
            if (output.status_eskalasi_darurat.indikasi_kritis) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Indikator Darurat",
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "STATUS ESKALASI: GAWAT DARURAT",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB71C1C),
                                fontSize = 13.sp
                            )
                            Text(
                                text = output.status_eskalasi_darurat.rekomendasi_tindakan,
                                fontSize = 12.sp,
                                color = Color(0xFF7F0000)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Status Normal",
                            tint = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "STATUS: ANAMNESIS AWAL LENGKAP",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Rekomendasi: ${output.status_eskalasi_darurat.rekomendasi_tindakan}",
                                fontSize = 12.sp,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }
                }
            }

            // SIMRS JSON Output Card
            SimrsJsonCard(
                simrsOutput = output,
                onSaveToHistoryClick = {
                    viewModel.saveCurrentRecordToDb()
                    Toast.makeText(context, "Berhasil menyimpan ke riwayat!", Toast.LENGTH_SHORT).show()
                }
            )

            // Doctor Verification Prompt Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFBFCBAD), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F3DC))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verifikasi Dokter",
                        tint = Color(0xFF386B1D)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Portal Verifikasi Dokter Berlisensi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF101F00)
                        )
                        Text(
                            text = "Dokter dapat meninjau hasil pencatatan AI ini dan menambahkan catatan resep/klinis.",
                            fontSize = 11.sp,
                            color = Color(0xFF43493E)
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.setTab(3) }, // Doctor Portal tab
                        modifier = Modifier.testTag("goto_doctor_portal_button")
                    ) {
                        Text("Tinjau Dokter", fontSize = 11.sp, color = Color(0xFF386B1D))
                    }
                }
            }
        }
    }
}
