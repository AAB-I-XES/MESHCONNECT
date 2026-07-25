package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import com.example.BuildConfig
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.bounceClick
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mesh.NodeInfo
import com.example.mesh.RouteStrategy
import com.example.mesh.TransportType
import com.example.ui.MeshLinkViewModel
import com.example.ui.components.GlassCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MeshPrimary
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// iOS Dark Map Style JSON string
private const val IOS_DARK_MAP_STYLE = """
[
  {"elementType": "geometry", "stylers": [{"color": "#111418"}]},
  {"elementType": "labels.text.fill", "stylers": [{"color": "#8E8E93"}]},
  {"elementType": "labels.text.stroke", "stylers": [{"color": "#111418"}]},
  {"featureType": "administrative", "elementType": "geometry.stroke", "stylers": [{"color": "#2C2C2E"}]},
  {"featureType": "landscape", "elementType": "geometry", "stylers": [{"color": "#181C22"}]},
  {"featureType": "poi", "elementType": "geometry", "stylers": [{"color": "#22272F"}]},
  {"featureType": "poi", "elementType": "labels.text.fill", "stylers": [{"color": "#8E8E93"}]},
  {"featureType": "road", "elementType": "geometry", "stylers": [{"color": "#2C323B"}]},
  {"featureType": "road.highway", "elementType": "geometry", "stylers": [{"color": "#38414E"}]},
  {"featureType": "road.highway", "elementType": "geometry.stroke", "stylers": [{"color": "#21262D"}]},
  {"featureType": "transit", "elementType": "geometry", "stylers": [{"color": "#22272F"}]},
  {"featureType": "water", "elementType": "geometry", "stylers": [{"color": "#0B1118"}]},
  {"featureType": "water", "elementType": "labels.text.fill", "stylers": [{"color": "#515C6B"}]}
]
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeshVisualizerScreen(
    viewModel: MeshLinkViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val activeNodes by viewModel.meshRoutingEngine.activeNodes.collectAsState()
    val metrics by viewModel.meshRoutingEngine.networkMetrics.collectAsState()
    val strategy by viewModel.meshRoutingEngine.routeStrategy.collectAsState()

    // Location States
    var userLat by remember { mutableDoubleStateOf(37.7749) } // Default SF / Central City
    var userLng by remember { mutableDoubleStateOf(-122.4194) }
    var userAccuracy by remember { mutableFloatStateOf(12f) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var selectedViewMode by remember { mutableIntStateOf(0) } // 0: Google Map, 1: Radar Scanner
    var mapTypeMode by remember { mutableStateOf(MapType.NORMAL) }
    var selectedNode by remember { mutableStateOf<NodeInfo?>(null) }

    // Camera State for Google Map
    val defaultLocation = LatLng(userLat, userLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Launch permission request once if not granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            try {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } catch (e: Exception) {
                // Ignore launcher error on edge platforms
            }
        }
    }

    // Request Location updates if permission granted
    DisposableEffect(hasLocationPermission) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    userLat = loc.latitude
                    userLng = loc.longitude
                    userAccuracy = loc.accuracy
                }
            }
        }

        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        userLat = loc.latitude
                        userLng = loc.longitude
                        userAccuracy = loc.accuracy
                    }
                }

                val priority = if (fineGranted) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
                val locationRequest = LocationRequest.Builder(priority, 5000L)
                    .setMinUpdateIntervalMillis(3000L)
                    .build()
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, context.mainLooper)
            } catch (e: Exception) {
                // Handle missing GPS provider or restriction gracefully
            }
        }

        onDispose {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            } catch (e: Exception) {
                // Ignore removal error
            }
        }
    }

    // Pulsing Animation for Radar & GPS Pin
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mesh Network Map",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = MeshPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MeshPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LIVE GPS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MeshPrimary
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (hasLocationPermission) "Lat: ${"%.4f".format(userLat)}, Lng: ${"%.4f".format(userLng)}" else "Enable Location for Real-Time Map",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val userPos = LatLng(userLat, userLng)
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userPos, 16f))
                        }
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Center Location", tint = MeshPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBackground)
            )
        },
        containerColor = NavyBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(padding)
                    .imePadding()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                // iOS Segmented Control Bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Option 0: Google Maps
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedViewMode == 0) DarkSurfaceElevated else Color.Transparent,
                            border = if (selectedViewMode == 0) androidx.compose.foundation.BorderStroke(0.5.dp, MeshPrimary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .bounceClick(onClick = { selectedViewMode = 0 })
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = if (selectedViewMode == 0) MeshPrimary else TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Google Maps",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedViewMode == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedViewMode == 0) TextPrimary else TextSecondary
                                )
                            }
                        }

                        // Option 1: Radar Scan
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedViewMode == 1) DarkSurfaceElevated else Color.Transparent,
                            border = if (selectedViewMode == 1) androidx.compose.foundation.BorderStroke(0.5.dp, MeshPrimary) else null,
                            modifier = Modifier
                                .weight(1f)
                                .bounceClick(onClick = { selectedViewMode = 1 })
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = null,
                                    tint = if (selectedViewMode == 1) MeshPrimary else TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Radar View",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedViewMode == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedViewMode == 1) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Strategy Selector Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    items(RouteStrategy.entries.toTypedArray()) { s ->
                        val isSelected = strategy == s
                        val label = when (s) {
                            RouteStrategy.SHORTEST_PATH -> "Shortest Path"
                            RouteStrategy.MIN_LATENCY -> "Min Latency"
                            RouteStrategy.BATTERY_SAVER -> "Battery Saver"
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateRouteStrategy(s) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MeshPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = DarkSurface,
                                labelColor = TextSecondary
                            ),
                            shape = CircleShape,
                            modifier = Modifier.testTag("strategy_chip_${s.name}")
                        )
                    }
                }

                // Main Map/Radar Content View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(0.5.dp, DarkCardBorder, RoundedCornerShape(18.dp))
                        .background(NavyBackground)
                        .testTag("mesh_map_container")
                ) {
                    if (selectedViewMode == 0) {
                        // GOOGLE MAPS REAL-TIME VIEW
                        val userLatLng = LatLng(userLat, userLng)

                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(
                                isMyLocationEnabled = false,
                                mapType = mapTypeMode,
                                mapStyleOptions = MapStyleOptions(IOS_DARK_MAP_STYLE)
                            ),
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                compassEnabled = true,
                                myLocationButtonEnabled = false
                            )
                        ) {
                            // User Location Pulse Circle
                            Circle(
                                center = userLatLng,
                                radius = (userAccuracy + (pulseRadius * 40f)).toDouble(),
                                fillColor = MeshPrimary.copy(alpha = 0.15f),
                                strokeColor = MeshPrimary.copy(alpha = 0.5f),
                                strokeWidth = 2f
                            )

                            // User Location Marker (Self Hub)
                            Marker(
                                state = MarkerState(position = userLatLng),
                                title = "Self Node (Me)",
                                snippet = "Mesh Gateway Hub",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            )

                            // Peer Nodes geographically distributed around user location
                            activeNodes.forEach { node ->
                                // Calculate geo offset based on node xRatio & yRatio
                                val latOffset = (node.yRatio - 0.5f) * 0.006 // ~300 meters range
                                val lngOffset = (node.xRatio - 0.5f) * 0.008
                                val peerLatLng = LatLng(userLat + latOffset, userLng + lngOffset)

                                // Draw Mesh Connection Vector Polyline
                                val lineColor = if (node.transport == TransportType.WIFI_DIRECT) EmeraldGreen else ElectricBlue
                                Polyline(
                                    points = listOf(userLatLng, peerLatLng),
                                    color = lineColor,
                                    width = if (node.isDirectPeer) 6f else 3f
                                )

                                // Peer Marker
                                val markerHue = if (node.isDirectPeer) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_ORANGE
                                Marker(
                                    state = MarkerState(position = peerLatLng),
                                    title = node.name,
                                    snippet = "Hop: ${node.hops} | RSSI: ${node.rssi} dBm | ${node.transport.name}",
                                    icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                                    onClick = {
                                        selectedNode = node
                                        false
                                    }
                                )
                            }
                        }

                        // iOS Floating Controls overlay on map
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Map Style Toggle Floating Button
                            Surface(
                                shape = CircleShape,
                                color = DarkSurface.copy(alpha = 0.9f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkCardBorder),
                                modifier = Modifier
                                    .size(42.dp)
                                    .clickable {
                                        mapTypeMode = if (mapTypeMode == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = "Toggle Layer",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Re-center Location Button
                            Surface(
                                shape = CircleShape,
                                color = DarkSurface.copy(alpha = 0.9f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkCardBorder),
                                modifier = Modifier
                                    .size(42.dp)
                                    .clickable {
                                        coroutineScope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(LatLng(userLat, userLng), 16f)
                                            )
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = "My Location",
                                        tint = MeshPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Map Legend Glass Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurface.copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, DarkCardBorder),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MeshPrimary))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Self Node (GPS)", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ElectricBlue))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("1-Hop Direct Peer", color = TextSecondary, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeonAmber))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Multi-hop Relay Node", color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                        }

                        // API Key Info Banner overlay
                        var dismissKeyNotice by remember { mutableStateOf(false) }
                        if (!dismissKeyNotice && (BuildConfig.MAPS_API_KEY == "DEFAULT_MAPS_KEY" || BuildConfig.MAPS_API_KEY.isBlank())) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkSurface.copy(alpha = 0.92f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, NeonAmber),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(12.dp)
                                    .fillMaxWidth(0.95f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Google Maps API Key Notice",
                                            fontWeight = FontWeight.Bold,
                                            color = NeonAmber,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Set MAPS_API_KEY in Secrets panel to load Google tiles, or use built-in Radar Mode.",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Row {
                                        Button(
                                            onClick = { selectedViewMode = 1 },
                                            colors = ButtonDefaults.buttonColors(containerColor = MeshPrimary, contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Radar Mode", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        // RADAR SCANNER VIEW
                        Box(modifier = Modifier.fillMaxSize()) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2f, size.height / 2f)

                                // Concentric Range Rings
                                listOf(0.25f, 0.50f, 0.75f).forEach { scale ->
                                    drawCircle(
                                        color = MeshPrimary.copy(alpha = 0.12f),
                                        radius = (size.width / 2f) * scale,
                                        center = center,
                                        style = Stroke(width = 1.dp.toPx())
                                    )
                                }

                                // Pulsing Search Wave
                                drawCircle(
                                    color = MeshPrimary.copy(alpha = (1f - pulseRadius) * 0.3f),
                                    radius = (size.width / 2f) * pulseRadius,
                                    center = center,
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                // Center Node (Self)
                                drawCircle(
                                    color = MeshPrimary,
                                    radius = 12.dp.toPx(),
                                    center = center
                                )

                                // Active Peer Nodes
                                activeNodes.forEach { node ->
                                    val nodeOffset = Offset(
                                        x = size.width * node.xRatio,
                                        y = size.height * node.yRatio
                                    )

                                    val lineColor = if (node.transport == TransportType.WIFI_DIRECT) EmeraldGreen else MeshPrimary

                                    drawLine(
                                        color = lineColor.copy(alpha = 0.6f),
                                        start = center,
                                        end = nodeOffset,
                                        strokeWidth = if (node.isDirectPeer) 2.dp.toPx() else 1.dp.toPx()
                                    )

                                    val packetOffset = Offset(
                                        x = center.x + (nodeOffset.x - center.x) * pulseRadius,
                                        y = center.y + (nodeOffset.y - center.y) * pulseRadius
                                    )
                                    drawCircle(
                                        color = NeonAmber,
                                        radius = 4.dp.toPx(),
                                        center = packetOffset
                                    )

                                    val nodeColor = if (node.isDirectPeer) ElectricBlue else NeonAmber
                                    drawCircle(
                                        color = nodeColor,
                                        radius = 9.dp.toPx(),
                                        center = nodeOffset
                                    )
                                }
                            }

                            // Legend Overlay
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Text("● Self (Hub)", color = MeshPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("● Direct Peer (1 Hop)", color = ElectricBlue, fontSize = 11.sp)
                                Text("● Relay Node (2+ Hops)", color = NeonAmber, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Selected Node Quick Detail Card (if clicked)
                selectedNode?.let { node ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MeshPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = node.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text(
                                    text = "Mesh ID: ${node.meshId.take(12)}... | Hops: ${node.hops} | RSSI: ${node.rssi} dBm",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            IconButton(onClick = { selectedNode = null }) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Close", tint = MeshPrimary)
                            }
                        }
                    }
                }

                // iOS Bottom Telemetry Sheet Glass Card
                GlassCard(
                    hasAccentGlow = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Hub, contentDescription = null, tint = MeshPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mesh Network Telemetry",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "${metrics.connectedPeersCount} Active Peers",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MeshPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IosMetricItem(title = "Peers", value = "${metrics.connectedPeersCount} / ${metrics.totalNodesCount}")
                            IosMetricItem(title = "Avg Latency", value = "${metrics.avgLatencyMs} ms")
                            IosMetricItem(title = "Packet Rate", value = "${metrics.packetsPerSec} pkts/s")
                            IosMetricItem(title = "Network Health", value = "${metrics.networkHealthPercent}%")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IosMetricItem(title: String, value: String) {
    Column {
        Text(text = title, fontSize = 10.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MeshPrimary)
    }
}
