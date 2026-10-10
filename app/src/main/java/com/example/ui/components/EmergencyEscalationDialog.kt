package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun EmergencyEscalationDialog(
    emergencyDetails: String,
    onCallEmergencyConfirmed: (phoneNum: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var step1Confirmed by remember { mutableStateOf(false) }
    var step2Confirmed by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .border(2.dp, Color(0xFFBA1A1A), RoundedCornerShape(28.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning Header Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Peringatan Darurat Medis",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🚨 PERINGATAN DARURAT KRITIS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD32F2F)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sistem AI mendeteksi indikator pemburukan fisik kritis:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Emergency details box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = emergencyDetails.ifBlank { "Terdeteksi pendarahan hebat / pembengkakan jalan napas / penurunan kesadaran visual." },
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Safety Rule Notice
                Text(
                    text = "⚠️ ATURAN KESELAMATAN: Panggilan ambulans / rujukan darurat HANYA akan tereksekusi setelah KONFIRMASI MANUAL 2-LANGKAH dari pasien/keluarga untuk mencegah alarm palsu.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        lineHeight = 14.sp,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // STEP 1 CONFIRMATION
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (step1Confirmed) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant),
                    onClick = { step1Confirmed = !step1Confirmed }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = step1Confirmed,
                            onCheckedChange = { step1Confirmed = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E7D32)),
                            modifier = Modifier.testTag("emergency_step1_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LANGKAH 1: Saya mengonfirmasi bahwa gejala darurat di atas benar-benar terjadi.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = if (step1Confirmed) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // STEP 2 CONFIRMATION & ACTION
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (step2Confirmed) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant),
                    onClick = {
                        if (step1Confirmed) {
                            step2Confirmed = !step2Confirmed
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = step2Confirmed,
                            enabled = step1Confirmed,
                            onCheckedChange = { step2Confirmed = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD32F2F)),
                            modifier = Modifier.testTag("emergency_step2_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LANGKAH 2: Konfirmasi panggil Ambulans / Layanan Darurat 118/119 sekarang.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = if (step1Confirmed) Color(0xFFB71C1C) else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ACTION BUTTONS
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Final Call Emergency Button
                    Button(
                        onClick = { onCallEmergencyConfirmed("118") },
                        enabled = step1Confirmed && step2Confirmed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("execute_emergency_call_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HUBUNGI AMBULANS (118/119)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Cancel / False alarm button
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("cancel_emergency_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Tutup / Alarm Palsu", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
