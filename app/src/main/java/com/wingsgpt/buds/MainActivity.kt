package com.wingsgpt.buds

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private val Background = Color(0xFF080B12)
private val SurfaceDark = Color(0xFF111827)
private val SurfaceLight = Color(0xFF1A2638)
private val Accent = Color(0xFF65E8F2)
private val AccentBlue = Color(0xFF718BFF)
private val TextPrimary = Color(0xFFF4F7FC)
private val TextMuted = Color(0xFF8C9AAF)
private val Success = Color(0xFF68E0B1)
private val Warning = Color(0xFFFFC66D)

private const val TARGET_DEVICE_NAME = "keshav ke airpods"

data class NearbyDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val lastSeen: Long,
    val serviceDetected: Boolean
) {
    val signalStrength: String
        get() = when {
            rssi >= -55 -> "Very close"
            rssi >= -68 -> "Nearby"
            rssi >= -80 -> "Far away"
            else -> "Weak signal"
        }

    val signalPercent: Int
        get() = ((rssi + 100) * 2).coerceIn(0, 100)
}

enum class WingsTab {
    HOME, FIND, DEVICE, SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.rgb(8, 11, 18)
        window.navigationBarColor = android.graphics.Color.rgb(8, 11, 18)

        setContent {
            WingsTheme {
                WingsApplication()
            }
        }
    }
}

@Composable
private fun WingsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            secondary = AccentBlue,
            background = Background,
            surface = SurfaceDark,
            onPrimary = Background,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        ),
        content = content
    )
}

@Composable
private fun WingsApplication() {
    val context = LocalContext.current
    val bluetooth = remember { WingsBluetooth(context) }

    var currentTab by remember { mutableStateOf(WingsTab.HOME) }
    var scanning by remember { mutableStateOf(false) }
    var bluetoothEnabled by remember { mutableStateOf(bluetooth.enabled()) }
    var status by remember { mutableStateOf("Ready to discover devices") }
    var selectedDevice by remember { mutableStateOf<NearbyDevice?>(null) }

    val devices = remember { mutableStateListOf<NearbyDevice>() }

    val permissions = if (Build.VERSION.SDK_INT >= 31) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (result.values.all { it }) {
                status = "Permission granted. Ready to scan."
            } else {
                status = "Bluetooth permissions are required."
            }
        }

    fun scan() {
        val granted = permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (!granted) {
            permissionLauncher.launch(permissions)
            return
        }

        if (!bluetooth.enabled()) {
            bluetoothEnabled = false
            status = "Please enable Bluetooth."
            return
        }

        devices.clear()
        scanning = true
        status = "Scanning nearby Bluetooth devices..."

        bluetooth.scan(
            onDevice = { device ->
                val index = devices.indexOfFirst {
                    it.address == device.address
                }

                if (index >= 0) {
                    devices[index] = device
                } else {
                    devices.add(device)
                }

                selectedDevice =
                    devices.firstOrNull {
                        it.name.equals(TARGET_DEVICE_NAME, true)
                    } ?: devices.firstOrNull()
            },
            onError = {
                scanning = false
                status = it
            },
            onComplete = {
                scanning = false
                status = if (devices.isEmpty()) {
                    "No nearby BLE devices detected."
                } else {
                    "${devices.size} devices detected."
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            bluetooth.stop()
        }
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            WingsBottomBar(
                selected = currentTab,
                onSelected = { currentTab = it }
            )
        }
    ) { padding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            AnimatedContent(
                targetState = currentTab,
                label = "screen_transition"
            ) { tab ->

                when (tab) {
                    WingsTab.HOME -> HomeScreen(
                        devices = devices,
                        selectedDevice = selectedDevice,
                        scanning = scanning,
                        bluetoothEnabled = bluetoothEnabled,
                        status = status,
                        onScan = { scan() },
                        onFind = {
                            currentTab = WingsTab.FIND
                        },
                        onDevice = {
                            currentTab = WingsTab.DEVICE
                        }
                    )

                    WingsTab.FIND -> FindScreen(
                        devices = devices,
                        selectedDevice = selectedDevice,
                        scanning = scanning,
                        status = status,
                        onScan = { scan() }
                    )

                    WingsTab.DEVICE -> DeviceScreen(
                        devices = devices,
                        selectedDevice = selectedDevice,
                        onScan = { scan() },
                        onSelect = { selectedDevice = it }
                    )

                    WingsTab.SETTINGS -> SettingsScreen(
                        bluetoothEnabled = bluetoothEnabled,
                        onBluetoothSettings = {
                            context.startActivity(
                                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                            )
                        },
                        onRefresh = {
                            bluetoothEnabled = bluetooth.enabled()
                        }
                    )
                }
            }
        }
    }
}

private class WingsBluetooth(
    private val context: Context
) {
    private val manager =
        context.getSystemService(Context.BLUETOOTH_SERVICE)
                as BluetoothManager

    private val adapter: BluetoothAdapter?
        get() = manager.adapter

    private val scanner: BluetoothLeScanner?
        get() = adapter?.bluetoothLeScanner

    private var callback: ScanCallback? = null

    fun enabled(): Boolean {
        return try {
            adapter?.isEnabled == true
        } catch (_: SecurityException) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun scan(
        onDevice: (NearbyDevice) -> Unit,
        onError: (String) -> Unit,
        onComplete: () -> Unit
    ) {
        stop()

        val activeScanner = scanner

        if (activeScanner == null) {
            onError("BLE scanner is unavailable.")
            return
        }

        val scanCallback = object : ScanCallback() {

            override fun onScanResult(
                callbackType: Int,
                result: ScanResult
            ) {
                val device = result.device
                val record = result.scanRecord

                val name = try {
                    record?.deviceName
                        ?: device.name
                        ?: "Unknown device"
                } catch (_: SecurityException) {
                    "Unknown device"
                }

                val serviceDetected =
                    record?.serviceUuids?.any {
                        it.uuid.toString().contains(
                            "fd50",
                            ignoreCase = true
                        )
                    } == true

                val address = try {
                    device.address
                } catch (_: SecurityException) {
                    "Unavailable"
                }

                onDevice(
                    NearbyDevice(
                        name = name,
                        address = address,
                        rssi = result.rssi,
                        lastSeen = System.currentTimeMillis(),
                        serviceDetected = serviceDetected
                    )
                )
            }

            override fun onScanFailed(errorCode: Int) {
                onError("Bluetooth scan failed: $errorCode")
            }
        }

        callback = scanCallback

        try {
            activeScanner.startScan(scanCallback)

            android.os.Handler(context.mainLooper).postDelayed(
                {
                    stop()
                    onComplete()
                },
                12000
            )

        } catch (_: SecurityException) {
            onError("Bluetooth permission denied.")
        } catch (error: Exception) {
            onError(
                error.message ?: "Unable to start Bluetooth scan."
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        val oldCallback = callback ?: return

        try {
            scanner?.stopScan(oldCallback)
        } catch (_: Exception) {
        }

        callback = null
    }
}

@Composable
private fun HomeScreen(
    devices: List<NearbyDevice>,
    selectedDevice: NearbyDevice?,
    scanning: Boolean,
    bluetoothEnabled: Boolean,
    status: String,
    onScan: () -> Unit,
    onFind: () -> Unit,
    onDevice: () -> Unit
) {
    ScreenContainer {

        ScreenHeader(
            title = "WINGS GPT",
            subtitle = "Your personal audio companion"
        )

        Spacer(Modifier.height(22.dp))

        DeviceHero(
            deviceName = selectedDevice?.name
                ?: "No device detected",
            subtitle = if (selectedDevice != null) {
                "Bluetooth advertisement detected"
            } else {
                "Bring your earbuds nearby"
            },
            onClick = onDevice
        )

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Bluetooth",
                value = if (bluetoothEnabled) "ON" else "OFF",
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Nearby devices",
                value = devices.size.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(18.dp))

        MainButton(
            text = if (scanning) "Scanning..." else "Scan for earbuds",
            loading = scanning,
            onClick = onScan
        )

        Spacer(Modifier.height(10.dp))

        OutlineAction(
            text = "Find my buds",
            onClick = onFind
        )

        Spacer(Modifier.height(20.dp))

        SectionHeading("DISCOVERY STATUS")

        GlassPanel {
            StatusDot(
                active = scanning
            )

            Spacer(Modifier.height(10.dp))

            Text(
                status,
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Bluetooth scanning detects nearby advertisements. " +
                    "It does not confirm an active connection, battery level, " +
                    "case lid state or exact location.",
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }

        Spacer(Modifier.height(18.dp))

        SectionHeading("NEARBY DEVICES")

        if (devices.isEmpty()) {
            EmptyPanel(
                title = "No devices found",
                subtitle = "Tap Scan for earbuds to begin."
            )
        } else {
            devices.take(5).forEach {
                DeviceListItem(it)
            }
        }
    }
}

@Composable
private fun FindScreen(
    devices: List<NearbyDevice>,
    selectedDevice: NearbyDevice?,
    scanning: Boolean,
    status: String,
    onScan: () -> Unit
) {
    ScreenContainer {

        ScreenHeader(
            title = "FIND MY BUDS",
            subtitle = "Bluetooth proximity radar"
        )

        Spacer(Modifier.height(20.dp))

        RadarView(
            scanning = scanning,
            detected = selectedDevice != null
        )

        Spacer(Modifier.height(18.dp))

        GlassPanel {

            SectionHeading("SIGNAL ESTIMATE")

            Text(
                selectedDevice?.signalStrength ?: "No signal detected",
                color = TextPrimary,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                selectedDevice?.let {
                    "RSSI: ${it.rssi} dBm"
                } ?: status,
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(14.dp))

            SignalProgress(
                selectedDevice?.signalPercent ?: 0
            )
        }

        Spacer(Modifier.height(14.dp))

        MainButton(
            text = if (scanning) "Scanning..." else "Start scan",
            loading = scanning,
            onClick = onScan
        )

        Spacer(Modifier.height(14.dp))

        GlassPanel {
            SectionHeading("LOCATION ACCURACY")

            Text(
                "This radar uses Bluetooth signal strength. " +
                    "RSSI is affected by walls, interference and phone position. " +
                    "It cannot provide exact distance or GPS location.",
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        SectionHeading("DETECTED DEVICES")

        if (devices.isEmpty()) {
            EmptyPanel(
                "No signal data",
                "Run a scan to detect Bluetooth advertisements."
            )
        } else {
            devices.forEach {
                DeviceListItem(it)
            }
        }
    }
}

@Composable
private fun DeviceScreen(
    devices: List<NearbyDevice>,
    selectedDevice: NearbyDevice?,
    onScan: () -> Unit,
    onSelect: (NearbyDevice) -> Unit
) {
    ScreenContainer {

        ScreenHeader(
            "MY DEVICE",
            "Hardware discovery and diagnostics"
        )

        Spacer(Modifier.height(20.dp))

        DeviceHero(
            deviceName = selectedDevice?.name ?: "No device selected",
            subtitle = "Select a discovered Bluetooth device",
            onClick = onScan
        )

        Spacer(Modifier.height(18.dp))

        SectionHeading("HARDWARE STATUS")

        GlassPanel {
            InformationLine(
                "Bluetooth connection",
                "Not connected"
            )

            InformationLine(
                "Battery telemetry",
                "Unknown"
            )

            InformationLine(
                "Charging case state",
                "Not verified"
            )

            InformationLine(
                "Left / right bud state",
                "Not verified"
            )

            InformationLine(
                "Audio command protocol",
                "Undocumented"
            )

            InformationLine(
                "GPS location",
                "Unavailable"
            )
        }

        Spacer(Modifier.height(18.dp))

        SectionHeading("DISCOVERED DEVICES")

        if (devices.isEmpty()) {
            EmptyPanel(
                "No devices discovered",
                "Start a scan to populate this list."
            )
        } else {
            devices.forEach {
                Column {
                    DeviceListItem(
                        device = it,
                        onClick = {
                            onSelect(it)
                        }
                    )

                    if (selectedDevice?.address == it.address) {
                        Text(
                            "SELECTED",
                            color = Accent,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(
                                start = 12.dp,
                                bottom = 8.dp
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlineAction(
            "Refresh device scan",
            onClick = onScan
        )
    }
}

@Composable
private fun SettingsScreen(
    bluetoothEnabled: Boolean,
    onBluetoothSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    ScreenContainer {

        ScreenHeader(
            "SETTINGS",
            "Configure Wings GPT"
        )

        Spacer(Modifier.height(20.dp))

        GlassPanel {

            SettingItem(
                title = "Bluetooth status",
                subtitle = if (bluetoothEnabled) {
                    "Bluetooth is enabled"
                } else {
                    "Bluetooth is disabled"
                },
                onClick = onRefresh
            )

            HorizontalDivider()

            SettingItem(
                title = "Bluetooth settings",
                subtitle = "Manage paired devices",
                onClick = onBluetoothSettings
            )

            HorizontalDivider()

            SettingItem(
                title = "Refresh status",
                subtitle = "Read Bluetooth adapter state",
                onClick = onRefresh
            )
        }

        Spacer(Modifier.height(18.dp))

        SectionHeading("ABOUT")

        GlassPanel {
            Text(
                "Wings GPT",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                "Version 0.1.0 • Experimental BLE companion",
                color = TextMuted,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "This version scans BLE advertisements. " +
                    "It does not send undocumented GATT commands " +
                    "or invent battery and GPS readings.",
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun ScreenContainer(
    content: @Composable ColumnScope.() -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
private fun ScreenHeader(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                color = TextPrimary,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.7.sp
            )

            Spacer(Modifier.height(5.dp))

            Text(
                subtitle,
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceLight)
                .border(
                    1.dp,
                    Accent.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Headphones,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun DeviceHero(
    deviceName: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1B3B51),
                        Color(0xFF17213A),
                        SurfaceDark
                    )
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(26.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "YOUR AUDIO GEAR",
                color = Accent,
                fontSize = 9.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.weight(1f)
            )

            StatusBadge(
                "NOT CONNECTED",
                Warning
            )
        }

        Spacer(Modifier.height(22.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            EarbudArtwork()

            Spacer(Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    deviceName,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    subtitle,
                    color = TextMuted,
                    fontSize = 10.sp
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "VIEW DEVICE",
                        color = Accent,
                        fontSize = 9.sp,
                        letterSpacing = 1.1.sp
                    )

                    Icon(
                        Icons.Default.ChevronRight,
                        null,
                        tint = Accent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EarbudArtwork() {
    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(86.dp, 65.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF48596D),
                            Color(0xFF121A26)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EarbudShape()
                EarbudShape()
            }
        }
    }
}

@Composable
private fun EarbudShape() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(0xFF9BAABD))
        )

        Box(
            Modifier
                .width(7.dp)
                .height(25.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF8191A5),
                            Color(0xFF1C2938)
                        )
                    )
                )
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    GlassPanel(
        modifier = modifier
    ) {
        Text(
            title,
            color = TextMuted,
            fontSize = 10.sp
        )

        Spacer(Modifier.height(9.dp))

        Text(
            value,
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(21.dp))
            .clip(RoundedCornerShape(21.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF192536),
                        Color(0xFF101824)
                    )
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.07f),
                RoundedCornerShape(21.dp)
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun MainButton(
    text: String,
    loading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = Background
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Background,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                Icons.Default.BluetoothSearching,
                null
            )
        }

        Spacer(Modifier.width(9.dp))

        Text(
            text,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun OutlineAction(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimary
        )
    ) {
        Icon(
            Icons.Default.Explore,
            null,
            tint = Accent
        )

        Spacer(Modifier.width(9.dp))

        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text,
        color = TextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(
            start = 3.dp,
            bottom = 10.dp,
            top = 3.dp
        )
    )
}

@Composable
private fun StatusBadge(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .border(
                1.dp,
                color.copy(alpha = 0.3f),
                CircleShape
            )
            .padding(
                horizontal = 10.dp,
                vertical = 6.dp
            )
    ) {
        Text(
            text,
            color = color,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DeviceListItem(
    device: NearbyDevice,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.045f))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xFF1B3548)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Bluetooth,
                null,
                tint = Accent
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                device.name,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                device.address,
                color = TextMuted,
                fontSize = 9.sp
            )

            if (device.serviceDetected) {
                Text(
                    "FD50 service detected",
                    color = Success,
                    fontSize = 9.sp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                "${device.rssi}",
                color = Accent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Text(
                "dBm",
                color = TextMuted,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun RadarView(
    scanning: Boolean,
    detected: Boolean
) {
    val transition = rememberInfiniteTransition(
        label = "radar"
    )

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2600,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF132B3D),
                        Color(0xFF0B1422)
                    )
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(27.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(235.dp)
        ) {
            val center = Offset(
                size.width / 2,
                size.height / 2
            )

            val radius = size.minDimension * 0.46f

            for (i in 1..4) {
                drawCircle(
                    color = Accent.copy(alpha = 0.15f),
                    radius = radius * i / 4,
                    center = center,
                    style = Stroke(1.dp.toPx())
                )
            }

            drawLine(
                color = Accent.copy(alpha = 0.12f),
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height)
            )

            drawLine(
                color = Accent.copy(alpha = 0.12f),
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y)
            )

            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Transparent,
                        Accent.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                ),
                startAngle = rotation,
                sweepAngle = 65f,
                useCenter = true,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = androidx.compose.ui.geometry.Size(
                    radius * 2,
                    radius * 2
                )
            )

            if (detected) {
                drawCircle(
                    color = Accent.copy(alpha = 0.2f),
                    radius = 9.dp.toPx() * pulse,
                    center = center
                )

                drawCircle(
                    color = Accent,
                    radius = 3.dp.toPx(),
                    center = center
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Headphones,
                null,
                tint = if (detected) Accent else TextMuted,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                when {
                    scanning -> "SEARCHING"
                    detected -> "SIGNAL DETECTED"
                    else -> "READY TO SCAN"
                },
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Text(
                "BLUETOOTH RADAR",
                color = TextMuted,
                fontSize = 8.sp,
                letterSpacing = 1.2.sp
            )
        }

        Text(
            "BLE",
            color = Accent,
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(17.dp)
        )
    }
}

@Composable
private fun SignalProgress(value: Int) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value / 100f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(AccentBlue, Accent)
                        )
                    )
            )
        }

        Spacer(Modifier.height(7.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("WEAK", color = TextMuted, fontSize = 8.sp)
            Text("STRONG", color = TextMuted, fontSize = 8.sp)
        }
    }
}

@Composable
private fun InformationLine(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            title,
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            value,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1.1f)
        )
    }
}

@Composable
private fun EmptyPanel(
    title: String,
    subtitle: String
) {
    GlassPanel {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.BluetoothSearching,
                    null,
                    tint = TextMuted,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    subtitle,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                subtitle,
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = TextMuted
        )
    }
}

@Composable
private fun StatusDot(
    active: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (active) Warning else Success
                )
        )

        Spacer(Modifier.width(9.dp))

        Text(
            if (active) "SCAN IN PROGRESS" else "SYSTEM READY",
            color = TextMuted,
            fontSize = 9.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun HorizontalDivider() {
    Divider(
        color = Color.White.copy(alpha = 0.07f),
        thickness = 1.dp
    )
}

@Composable
private fun WingsBottomBar(
    selected: WingsTab,
    onSelected: (WingsTab) -> Unit
) {
    val tabs = listOf(
        WingsTab.HOME to Icons.Default.Home,
        WingsTab.FIND to Icons.Default.Explore,
        WingsTab.DEVICE to Icons.Default.Headphones,
        WingsTab.SETTINGS to Icons.Default.Settings
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1521))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.07f)
            )
            .padding(
                horizontal = 8.dp,
                vertical = 10.dp
            ),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { (tab, icon) ->
            val active = selected == tab

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        onSelected(tab)
                    }
                    .padding(vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    icon,
                    contentDescription = tab.name,
                    tint = if (active) Accent else TextMuted,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    tab.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    color = if (active) Accent else TextMuted,
                    fontSize = 9.sp,
                    fontWeight = if (active) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                )
            }
        }
    }
}
