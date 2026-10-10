package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FramingGuidanceOverlay(
    bitmap: Bitmap?,
    lightingQuality: String, // "Baik", "Cukup", "Buruk"
    framingInstruction: String, // "Tolong dekatkan kamera sekitar 10 cm", "Arahkan kamera sedikit ke kiri", etc.
    distanceCm: Int,
    onCaptureClick: () -> Unit,
    onSelectSampleClick: () -> Unit,
    onAdjustLightingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .border(1.dp, Color(0xFFC5C8BA), RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF262923))
        ) {
            // Display captured or sample image if available, else standard framing preview background
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto Hasil Tangkapan Visual",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1B2E38)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusWeak,
                        contentDescription = "Fokus Kamera",
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            // 2D framing reticle canvas overlay
            val reticleColor = when (lightingQuality) {
                "Baik" -> Color(0xFFB1F18B)
                "Cukup" -> Color(0xFFFFC107)
                else -> Color(0xFFFF5252)
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 5.dp.toPx()
                val cornerLength = 40.dp.toPx()
                val left = size.width * 0.15f
                val top = size.height * 0.15f
                val right = size.width * 0.85f
                val bottom = size.height * 0.85f

                // Top Left corner
                val path = Path().apply {
                    moveTo(left, top + cornerLength)
                    lineTo(left, top)
                    lineTo(left + cornerLength, top)

                    // Top Right corner
                    moveTo(right - cornerLength, top)
                    lineTo(right, top)
                    lineTo(right, top + cornerLength)

                    // Bottom Left corner
                    moveTo(left, bottom - cornerLength)
                    lineTo(left, bottom)
                    lineTo(left + cornerLength, bottom)

                    // Bottom Right corner
                    moveTo(right - cornerLength, bottom)
                    lineTo(right, bottom)
                    lineTo(right, bottom - cornerLength)
                }

                drawPath(
                    path = path,
                    color = reticleColor,
                    style = Stroke(width = strokeWidth)
                )
            }

            // Top Status Badges (Lighting & Distance)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lighting status tag
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Pencahayaan",
                        tint = reticleColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pencahayaan: $lightingQuality",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Distance estimate
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jarak: ~$distanceCm cm",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bottom Real-Time 2D Framing Guidance Banner
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯 Panduan Arah 2D:",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = framingInstruction,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("framing_instruction_text")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Preset samples button
                    IconButton(
                        onClick = onSelectSampleClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .testTag("sample_cases_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Pilih Contoh Foto",
                            tint = Color.White
                        )
                    }

                    // Primary Capture Photo Button
                    IconButton(
                        onClick = onCaptureClick,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, Color.White, CircleShape)
                            .testTag("capture_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Ambil Foto Fisik",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Lighting mode adjust button
                    IconButton(
                        onClick = onAdjustLightingClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .testTag("adjust_lighting_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Atur Cahaya/Kilat",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
