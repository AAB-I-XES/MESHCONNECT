package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.MeshLinkViewModel
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DeveloperDashboardScreen
import com.example.ui.screens.MeshVisualizerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoiceCallScreen
import com.example.ui.theme.MeshLinkTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MeshLinkViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.startHardwareNetworking()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestMeshPermissions()

        setContent {
            MeshLinkTheme {
                MeshLinkApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startHardwareNetworking()
    }

    private fun checkAndRequestMeshPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        val missing = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            requestPermissionLauncher.launch(missing.toTypedArray())
        } else {
            viewModel.startHardwareNetworking()
        }
    }
}

@Composable
fun MeshLinkApp(viewModel: MeshLinkViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "chat_list",
        modifier = Modifier.fillMaxSize()
    ) {
            composable("chat_list") {
                ChatListScreen(
                    viewModel = viewModel,
                    onConversationClick = { convId ->
                        viewModel.selectConversation(convId)
                        navController.navigate("chat_detail/$convId")
                    },
                    onNavigateToVisualizer = { navController.navigate("visualizer") },
                    onNavigateToContacts = { navController.navigate("contacts") },
                    onNavigateToDevDashboard = { navController.navigate("dev_dashboard") }
                )
            }

            composable(
                route = "chat_detail/{conversationId}",
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val convId = backStackEntry.arguments?.getString("conversationId") ?: ""
                ChatDetailScreen(
                    conversationId = convId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onStartCall = { peerMeshId, peerName, isWalkieTalkie ->
                        viewModel.startVoiceCall(peerMeshId, peerName, isWalkieTalkie)
                        navController.navigate("voice_call")
                    }
                )
            }

            composable("visualizer") {
                MeshVisualizerScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("contacts") {
                ContactsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onStartChat = { convId ->
                        viewModel.selectConversation(convId)
                        navController.navigate("chat_detail/$convId")
                    }
                )
            }

            composable("voice_call") {
                VoiceCallScreen(
                    viewModel = viewModel,
                    onEndCallClick = { navController.popBackStack() }
                )
            }

            composable("dev_dashboard") {
                DeveloperDashboardScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
}
