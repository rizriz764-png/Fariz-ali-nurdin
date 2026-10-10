package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmergencyEscalationDialog
import com.example.ui.screens.AnamnesisScreen
import com.example.ui.screens.DoctorPortalScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoveHealthyWebViewScreen
import com.example.ui.screens.SimrsOutputScreen
import com.example.ui.theme.KesehatanVisualTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KesehatanVisualTheme {
                val selectedTab by viewModel.selectedTab.collectAsState()
                val showEmergencyDialog by viewModel.showEmergencyDialog.collectAsState()
                val emergencyDetails by viewModel.emergencyDetails.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { viewModel.setTab(0) },
                                icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Visual & Intake") },
                                label = { Text("Anamnesis", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("nav_item_anamnesis")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { viewModel.setTab(1) },
                                icon = { Icon(Icons.Default.Description, contentDescription = "Output SIMRS") },
                                label = { Text("SIMRS JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("nav_item_simrs")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { viewModel.setTab(2) },
                                icon = { Icon(Icons.Default.History, contentDescription = "Riwayat DB") },
                                label = { Text("Riwayat", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("nav_item_history")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { viewModel.setTab(3) },
                                icon = { Icon(Icons.Default.LocalHospital, contentDescription = "Portal Dokter") },
                                label = { Text("Dokter", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("nav_item_doctor")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 4,
                                onClick = { viewModel.setTab(4) },
                                icon = { Icon(Icons.Default.MedicalServices, contentDescription = "Web Demo Prototype") },
                                label = { Text("Web Demo", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("nav_item_web_demo")
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                viewModel.triggerEmergencyEscalation("Manual trigger panggilan darurat ambulans oleh pengguna.")
                            },
                            containerColor = Color(0xFFBA1A1A),
                            contentColor = Color.White,
                            modifier = Modifier.testTag("emergency_fab")
                        ) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = "Panggilan Darurat 118")
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> AnamnesisScreen(viewModel = viewModel)
                            1 -> SimrsOutputScreen(viewModel = viewModel)
                            2 -> HistoryScreen(viewModel = viewModel)
                            3 -> DoctorPortalScreen(viewModel = viewModel)
                            4 -> LoveHealthyWebViewScreen()
                        }

                        // Phase 2: 2-Step Emergency Escalation Modal Overlay
                        if (showEmergencyDialog) {
                            EmergencyEscalationDialog(
                                emergencyDetails = emergencyDetails,
                                onCallEmergencyConfirmed = { phoneNum ->
                                    viewModel.dismissEmergencyDialog()
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNum"))
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(this@MainActivity, "Memanggil $phoneNum...", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onDismissRequest = { viewModel.dismissEmergencyDialog() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND || 
            level == android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            viewModel.setSelectedBitmap(null)
            System.gc()
        }
    }
}
