package com.ox.stealth.ui

import android.content.SharedPreferences
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ox.stealth.R
import com.ox.stealth.ui.theme.*

/**
 * الشاشة الرئيسية - Zero X Stealth Module
 * تصميم هاكر عربي بالكامل
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(prefs: SharedPreferences) {
    var selectedTab by remember { mutableIntStateOf(0) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = DarkBg,
            topBar = {
                ZeroXTopBar()
            },
            bottomBar = {
                ZeroXBottomBar(selectedTab) { selectedTab = it }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> DashboardScreen(prefs)
                    1 -> LocationScreen(prefs)
                    2 -> DeviceScreen(prefs)
                    3 -> ShieldScreen(prefs)
                    4 -> SettingsScreen(prefs)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZeroXTopBar() {
    val infiniteTransition = rememberInfiniteTransition(label = "topbar")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(3000), RepeatMode.Reverse
        ), label = "glow"
    )

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_zerox),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Column {
                    Text(
                        text = "ZERO X",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ZeroXRed,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 3.sp,
                            shadow = Shadow(ZeroXRedGlow.copy(alpha = glowAlpha * 0.4f), Offset.Zero, 8f)
                        )
                    )
                    Text(
                        text = "وحدة التخفي النشطة",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = HackerGreen.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Monospace,
                            textDirection = TextDirection.Rtl
                        )
                    )
                }
            }
        },
        actions = {
            // مؤشر الحالة
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(HackerGreen)
            )
            Spacer(modifier = Modifier.width(12.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkBg,
        )
    )
}

@Composable
fun ZeroXBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        TabItem("الرئيسية", Icons.Filled.Dashboard),
        TabItem("الموقع", Icons.Filled.MyLocation),
        TabItem("الجهاز", Icons.Filled.PhoneAndroid),
        TabItem("الدرع", Icons.Filled.Shield),
        TabItem("الإعدادات", Icons.Filled.Settings),
    )

    NavigationBar(
        containerColor = DarkSurface,
        contentColor = HackerGreen,
        tonalElevation = 0.dp
    ) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        tab.icon, tab.label,
                        tint = if (selectedTab == index) HackerGreen else Color(0xFF555555),
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        tab.label,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (selectedTab == index) HackerGreen else Color(0xFF555555),
                        style = TextStyle(textDirection = TextDirection.Rtl)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = HackerGreen.copy(alpha = 0.1f)
                )
            )
        }
    }
}

data class TabItem(val label: String, val icon: ImageVector)

// ===================== شاشة لوحة التحكم =====================

@Composable
fun DashboardScreen(prefs: SharedPreferences) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // بطاقة الحالة الرئيسية
        StatusCard(prefs)

        // شبكة الوحدات
        Text(
            text = "⚡ الوحدات النشطة",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HackerGreen,
                fontFamily = FontFamily.Monospace,
                textDirection = TextDirection.Rtl
            )
        )

        ModulesGrid(prefs)

        // إحصائيات
        Text(
            text = "📊 إحصائيات التخفي",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HackerGreen,
                fontFamily = FontFamily.Monospace,
                textDirection = TextDirection.Rtl
            )
        )

        StatsCard()
    }
}

@Composable
fun StatusCard(prefs: SharedPreferences) {
    val isActive = prefs.getBoolean("module_active", true)

    HackerCard {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulse by infiniteTransition.animateFloat(
                    initialValue = 0.5f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "pulse"
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isActive) HackerGreen.copy(alpha = pulse) else HackerRed)
                )
                Text(
                    text = if (isActive) "الوحدة نشطة - التخفي مفعّل" else "الوحدة معطّلة",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) HackerGreen else HackerRed,
                        fontFamily = FontFamily.Monospace,
                        textDirection = TextDirection.Rtl
                    )
                )
            }

            Divider(color = HackerGreen.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusIndicator("الروت", "مخفي", HackerGreen)
                StatusIndicator("الموقع", "مزيّف", HackerGreen)
                StatusIndicator("الجهاز", "مزيّف", HackerGreen)
                StatusIndicator("الدرع", "نشط", HackerGreen)
            }
        }
    }
}

@Composable
fun StatusIndicator(label: String, status: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = status,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF666666),
            fontFamily = FontFamily.Monospace,
            style = TextStyle(textDirection = TextDirection.Rtl)
        )
    }
}

@Composable
fun ModulesGrid(prefs: SharedPreferences) {
    val modules = listOf(
        ModuleInfo("إخفاء الروت", "تخطي فحوصات su و Magisk", Icons.Filled.Security, "root_hide"),
        ModuleInfo("تزييف الموقع", "GPS Spoofing بدون Mock API", Icons.Filled.LocationOn, "location_spoof"),
        ModuleInfo("إخفاء Mock", "isFromMockProvider → false", Icons.Filled.LocationOff, "mock_hide"),
        ModuleInfo("تزييف الجهاز", "IMEI, Android ID, Serial", Icons.Filled.PhoneAndroid, "device_spoof"),
        ModuleInfo("إخفاء Frida", "تخطي IsFridaDetected", Icons.Filled.BugReport, "frida_hide"),
        ModuleInfo("جهاز شرعي", "إخفاء VPN, ADB, Dev Mode", Icons.Filled.VerifiedUser, "legit_device"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        modules.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { module ->
                    ModuleCard(
                        module = module,
                        isEnabled = prefs.getBoolean(module.prefKey, true),
                        onToggle = { prefs.edit().putBoolean(module.prefKey, it).apply() },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

data class ModuleInfo(
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val prefKey: String
)

@Composable
fun ModuleCard(module: ModuleInfo, isEnabled: Boolean, onToggle: (Boolean) -> Unit, modifier: Modifier) {
    var enabled by remember { mutableStateOf(isEnabled) }

    HackerCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    module.icon, null,
                    tint = if (enabled) HackerGreen else Color(0xFF444444),
                    modifier = Modifier.size(20.dp)
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it; onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HackerGreen,
                        checkedTrackColor = HackerGreen.copy(alpha = 0.2f),
                        uncheckedThumbColor = Color(0xFF444444),
                        uncheckedTrackColor = Color(0xFF1A1A1A)
                    ),
                    modifier = Modifier.height(20.dp)
                )
            }
            Text(
                text = module.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else Color(0xFF666666),
                fontFamily = FontFamily.Monospace,
                style = TextStyle(textDirection = TextDirection.Rtl)
            )
            Text(
                text = module.desc,
                fontSize = 9.sp,
                color = Color(0xFF555555),
                fontFamily = FontFamily.Monospace,
                lineHeight = 13.sp,
                style = TextStyle(textDirection = TextDirection.Rtl)
            )
        }
    }
}

@Composable
fun StatsCard() {
    HackerCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatRow("عمليات الإخفاء", "2,847", HackerGreen)
            StatRow("محاولات الكشف المحظورة", "156", ZeroXRed)
            StatRow("الهوكات النشطة", "48", HackerCyan)
            StatRow("آخر تحديث للموقع", "الآن", HackerGreen)
        }
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF888888),
            fontFamily = FontFamily.Monospace,
            style = TextStyle(textDirection = TextDirection.Rtl)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ===================== شاشة الموقع =====================

@Composable
fun LocationScreen(prefs: SharedPreferences) {
    // حالة الموقع
    var lat by remember { mutableStateOf(prefs.getString("spoof_lat", "24.7136")?.toDoubleOrNull() ?: 24.7136) }
    var lng by remember { mutableStateOf(prefs.getString("spoof_lng", "46.6753")?.toDoubleOrNull() ?: 46.6753) }
    var selectedLayer by remember { mutableStateOf(MapLayer.DARK) }
    var showLayerPicker by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var isSpoofActive by remember { mutableStateOf(prefs.getBoolean("location_enabled", false)) }

    Box(modifier = Modifier.fillMaxSize()) {
        // === الخريطة التفاعلية (تملأ الشاشة بالكامل) ===
        OsmMapView(
            modifier = Modifier.fillMaxSize(),
            latitude = lat,
            longitude = lng,
            zoom = 17.0,
            mapLayer = selectedLayer,
            accuracyMeters = prefs.getString("spoof_accuracy", "3.5")?.toFloatOrNull() ?: 3.5f,
            onLocationSelected = { newLat, newLng ->
                lat = newLat
                lng = newLng
                prefs.edit()
                    .putString("spoof_lat", "%.6f".format(newLat))
                    .putString("spoof_lng", "%.6f".format(newLng))
                    .apply()
            }
        )

        // === شريط البحث في الأعلى ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        ) {
            // حقل البحث
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xDD0D0D0D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Search, null,
                        tint = HackerGreen,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "ابحث عن مكان...",
                                    color = Color(0xFF555555),
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                textDirection = TextDirection.Rtl
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = HackerGreen,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // === معلومات الموقع الحالي ===
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xDD0D0D0D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "الإحداثيات",
                            fontSize = 10.sp,
                            color = Color(0xFF666666),
                            fontFamily = FontFamily.Monospace
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                "${"%,.6f".format(lat)}, ${"%,.6f".format(lng)}",
                                fontSize = 13.sp,
                                color = HackerGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // حالة التزييف
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSpoofActive) HackerGreen.copy(alpha = 0.15f)
                                else Color(0xFF1A1A1A)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (isSpoofActive) "نشط" else "متوقف",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSpoofActive) HackerGreen else Color(0xFF555555),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // === أزرار طبقات الخريطة (يمين) ===
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapLayer.values().forEach { layer ->
                FloatingActionButton(
                    onClick = { selectedLayer = layer },
                    modifier = Modifier.size(40.dp),
                    containerColor = if (selectedLayer == layer) HackerGreen.copy(alpha = 0.9f) else Color(0xDD0D0D0D),
                    contentColor = if (selectedLayer == layer) Color.Black else Color(0xFF888888),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        layer.icon,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // === شريط التحكم السفلي ===
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xEE0D0D0D))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // صف الأزرار الرئيسية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // زر تفعيل/إيقاف التزييف
                    Button(
                        onClick = {
                            isSpoofActive = !isSpoofActive
                            prefs.edit().putBoolean("location_enabled", isSpoofActive).apply()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSpoofActive) ZeroXRed else HackerGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (isSpoofActive) Icons.Filled.Close else Icons.Filled.PlayArrow,
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isSpoofActive) "إيقاف التزييف" else "تفعيل التزييف",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // زر الإعدادات المتقدمة
                    FloatingActionButton(
                        onClick = { showSettings = !showSettings },
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFF1A1A1A),
                        contentColor = HackerGreen,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Settings, null)
                    }
                }

                // === الإعدادات المتقدمة (قابلة للطي) ===
                if (showSettings) {
                    Divider(color = HackerGreen.copy(alpha = 0.1f))

                    // إعدادات الإحداثيات اليدوية
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            OutlinedTextField(
                                value = "%.6f".format(lat),
                                onValueChange = {
                                    it.toDoubleOrNull()?.let { v ->
                                        lat = v
                                        prefs.edit().putString("spoof_lat", it).apply()
                                    }
                                },
                                label = { Text("Lat", color = Color(0xFF555555), fontSize = 10.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                textStyle = TextStyle(color = HackerGreen, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HackerGreen.copy(alpha = 0.5f),
                                    unfocusedBorderColor = Color(0xFF222222),
                                    cursorColor = HackerGreen,
                                    focusedContainerColor = Color(0xFF0A0A0A),
                                    unfocusedContainerColor = Color(0xFF0A0A0A)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = "%.6f".format(lng),
                                onValueChange = {
                                    it.toDoubleOrNull()?.let { v ->
                                        lng = v
                                        prefs.edit().putString("spoof_lng", it).apply()
                                    }
                                },
                                label = { Text("Lng", color = Color(0xFF555555), fontSize = 10.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                textStyle = TextStyle(color = HackerGreen, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HackerGreen.copy(alpha = 0.5f),
                                    unfocusedBorderColor = Color(0xFF222222),
                                    cursorColor = HackerGreen,
                                    focusedContainerColor = Color(0xFF0A0A0A),
                                    unfocusedContainerColor = Color(0xFF0A0A0A)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    }

                    // إعدادات الحركة الطبيعية
                    Divider(color = HackerGreen.copy(alpha = 0.1f))
                    Text(
                        "الحركة الطبيعية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HackerGreen,
                        fontFamily = FontFamily.Monospace,
                        style = TextStyle(textDirection = TextDirection.Rtl)
                    )
                    ToggleRow("تفعيل الحركة الطبيعية", "natural_movement", prefs)
                    CoordinateInput("نطاق التذبذب (أمتار)", "jitter_range", "5.0", prefs)
                    CoordinateInput("الدقة (أمتار)", "spoof_accuracy", "3.5", prefs)
                    CoordinateInput("الارتفاع (متر)", "spoof_alt", "612.0", prefs)

                    // إعدادات إخفاء Mock
                    Divider(color = HackerGreen.copy(alpha = 0.1f))
                    Text(
                        "إخفاء Mock Location",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZeroXRed,
                        fontFamily = FontFamily.Monospace,
                        style = TextStyle(textDirection = TextDirection.Rtl)
                    )
                    ToggleRow("إخفاء isFromMockProvider", "hide_mock_provider", prefs)
                    ToggleRow("إخفاء isMock (Android 12+)", "hide_is_mock", prefs)
                    ToggleRow("تنظيف Extras من علامات Mock", "clean_extras", prefs)
                    ToggleRow("إخفاء إعداد Mock Location", "hide_mock_setting", prefs)
                    ToggleRow("إخفاء صلاحية Mock Location", "hide_mock_permission", prefs)
                }
            }
        }
    }
}

// ===================== شاشة الجهاز =====================

@Composable
fun DeviceScreen(prefs: SharedPreferences) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("📱 تزييف هوية الجهاز", "تغيير كل معرفات الجهاز")

        HackerCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleRow("تفعيل تزييف الجهاز", "device_spoof_enabled", prefs)
                Divider(color = HackerGreen.copy(alpha = 0.1f))
                DeviceField("Android ID", "spoof_android_id", prefs)
                DeviceField("IMEI", "spoof_imei", prefs)
                DeviceField("الرقم التسلسلي", "spoof_serial", prefs)
                DeviceField("عنوان WiFi MAC", "spoof_wifi_mac", prefs)
                DeviceField("عنوان Bluetooth MAC", "spoof_bt_mac", prefs)
            }
        }

        SectionHeader("🏭 تزييف معلومات البناء", "تغيير موديل وشركة الجهاز")

        HackerCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DeviceField("الشركة المصنعة", "spoof_manufacturer", prefs)
                DeviceField("الموديل", "spoof_model", prefs)
                DeviceField("العلامة التجارية", "spoof_brand", prefs)
                DeviceField("البصمة (Fingerprint)", "spoof_fingerprint", prefs)
            }
        }

        // زر التوليد العشوائي
        Button(
            onClick = { generateRandomDevice(prefs) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HackerGreen.copy(alpha = 0.15f),
                contentColor = HackerGreen
            ),
            border = BorderStroke(1.dp, HackerGreen.copy(alpha = 0.3f))
        ) {
            Icon(Icons.Filled.Casino, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "🎲 توليد هوية عشوائية",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                style = TextStyle(textDirection = TextDirection.Rtl)
            )
        }
    }
}

// ===================== شاشة الدرع =====================

@Composable
fun ShieldScreen(prefs: SharedPreferences) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("🔒 إخفاء الروت", "تخطي كل فحوصات صلاحيات الروت")

        HackerCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleRow("إخفاء ملفات su", "hide_su_files", prefs)
                ToggleRow("إخفاء تطبيقات الروت", "hide_root_apps", prefs)
                ToggleRow("تزييف Build.TAGS → release-keys", "fake_build_tags", prefs)
                ToggleRow("حظر أوامر الروت", "block_root_commands", prefs)
                ToggleRow("إخفاء Magisk Socket", "hide_magisk_socket", prefs)
                ToggleRow("تزييف SELinux → Enforcing", "fake_selinux", prefs)
                ToggleRow("إخفاء /proc/mounts", "hide_proc_mounts", prefs)
            }
        }

        SectionHeader("🕵️ إخفاء أدوات التحليل", "منع اكتشاف أدوات الهندسة العكسية")

        HackerCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleRow("إخفاء Frida", "hide_frida", prefs)
                ToggleRow("إخفاء Xposed/LSPosed", "hide_xposed", prefs)
                ToggleRow("تنظيف Stack Traces", "clean_stack_traces", prefs)
                ToggleRow("إخفاء من /proc/self/maps", "hide_proc_maps", prefs)
            }
        }

        SectionHeader("✅ جهاز شرعي", "جعل الجهاز يبدو غير معدّل")

        HackerCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleRow("إخفاء VPN", "hide_vpn", prefs)
                ToggleRow("إخفاء وضع المطور", "hide_dev_options", prefs)
                ToggleRow("إخفاء USB Debugging", "hide_usb_debug", prefs)
                ToggleRow("إخفاء Bootloader المفتوح", "hide_bootloader", prefs)
                ToggleRow("إخفاء ADB", "hide_adb", prefs)
                ToggleRow("تزييف Verified Boot → green", "fake_verified_boot", prefs)
            }
        }
    }
}

// ===================== شاشة الإعدادات =====================

@Composable
fun SettingsScreen(prefs: SharedPreferences) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("⚙️ إعدادات عامة", "التحكم بالوحدة")

        HackerCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleRow("تفعيل الوحدة", "module_active", prefs)
                ToggleRow("وضع التخفي الكامل", "full_stealth", prefs)
                ToggleRow("تسجيل العمليات (Debug Log)", "debug_log", prefs)
            }
        }

        SectionHeader("🎯 التطبيقات المستهدفة", "اختر التطبيقات التي تريد تطبيق التخفي عليها")

        HackerCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTargetRow("موارد (Mawared)", "com.tcs.nim", true, prefs)
                AppTargetRow("جميع التطبيقات", "*", false, prefs)
            }
        }

        SectionHeader("ℹ️ حول", "")

        HackerCard {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_zerox),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Text(
                    "ZERO X",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ZeroXRed,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp
                )
                Text(
                    "الإصدار 1.0.0",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    fontFamily = FontFamily.Monospace,
                    style = TextStyle(textDirection = TextDirection.Rtl)
                )
                Text(
                    "وحدة تخفي متقدمة لـ LSPosed",
                    fontSize = 11.sp,
                    color = HackerGreen.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    style = TextStyle(textDirection = TextDirection.Rtl)
                )
            }
        }
    }
}

// ===================== مكونات مشتركة =====================

@Composable
fun HackerCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, HackerGreen.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        content()
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HackerGreen,
                fontFamily = FontFamily.Monospace,
                textDirection = TextDirection.Rtl
            )
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 11.sp,
                    color = Color(0xFF555555),
                    fontFamily = FontFamily.Monospace,
                    textDirection = TextDirection.Rtl
                )
            )
        }
    }
}

@Composable
fun ToggleRow(label: String, prefKey: String, prefs: SharedPreferences) {
    var enabled by remember { mutableStateOf(prefs.getBoolean(prefKey, true)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (enabled) Color(0xFFCCCCCC) else Color(0xFF555555),
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f),
            style = TextStyle(textDirection = TextDirection.Rtl)
        )
        Switch(
            checked = enabled,
            onCheckedChange = {
                enabled = it
                prefs.edit().putBoolean(prefKey, it).apply()
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = HackerGreen,
                checkedTrackColor = HackerGreen.copy(alpha = 0.2f),
                uncheckedThumbColor = Color(0xFF444444),
                uncheckedTrackColor = Color(0xFF1A1A1A)
            )
        )
    }
}

@Composable
fun CoordinateInput(label: String, prefKey: String, defaultValue: String, prefs: SharedPreferences) {
    var value by remember { mutableStateOf(prefs.getString(prefKey, defaultValue) ?: defaultValue) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontFamily = FontFamily.Monospace,
            style = TextStyle(textDirection = TextDirection.Rtl)
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    value = it
                    prefs.edit().putString(prefKey, it).apply()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = HackerGreen,
                    fontFamily = FontFamily.Monospace
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HackerGreen.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color(0xFF222222),
                    cursorColor = HackerGreen,
                    focusedContainerColor = Color(0xFF0A0A0A),
                    unfocusedContainerColor = Color(0xFF0A0A0A)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }
    }
}

@Composable
fun DeviceField(label: String, prefKey: String, prefs: SharedPreferences) {
    var value by remember { mutableStateOf(prefs.getString(prefKey, "") ?: "") }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF666666),
            fontFamily = FontFamily.Monospace,
            style = TextStyle(textDirection = TextDirection.Rtl)
        )
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    value = it
                    prefs.edit().putString(prefKey, it).apply()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                placeholder = {
                    Text("تلقائي (عشوائي)", fontSize = 12.sp, color = Color(0xFF333333))
                },
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = HackerCyan,
                    fontFamily = FontFamily.Monospace
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HackerCyan.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color(0xFF222222),
                    cursorColor = HackerCyan,
                    focusedContainerColor = Color(0xFF0A0A0A),
                    unfocusedContainerColor = Color(0xFF0A0A0A)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }
    }
}

@Composable
fun AppTargetRow(appName: String, packageName: String, defaultEnabled: Boolean, prefs: SharedPreferences) {
    var enabled by remember { mutableStateOf(prefs.getBoolean("target_$packageName", defaultEnabled)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) HackerGreen.copy(alpha = 0.05f) else Color.Transparent)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = appName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else Color(0xFF555555),
                fontFamily = FontFamily.Monospace,
                style = TextStyle(textDirection = TextDirection.Rtl)
            )
            Text(
                text = packageName,
                fontSize = 10.sp,
                color = Color(0xFF444444),
                fontFamily = FontFamily.Monospace
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = {
                enabled = it
                prefs.edit().putBoolean("target_$packageName", it).apply()
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = HackerGreen,
                checkedTrackColor = HackerGreen.copy(alpha = 0.2f),
                uncheckedThumbColor = Color(0xFF444444),
                uncheckedTrackColor = Color(0xFF1A1A1A)
            )
        )
    }
}

fun generateRandomDevice(prefs: SharedPreferences) {
    val chars = "0123456789abcdef"
    val alphaNum = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    prefs.edit().apply {
        putString("spoof_android_id", (1..16).map { chars.random() }.joinToString(""))
        putString("spoof_serial", (1..12).map { alphaNum.random() }.joinToString(""))
        putString("spoof_wifi_mac", (1..6).joinToString(":") { String.format("%02x", (0..255).random()) })
        putString("spoof_bt_mac", (1..6).joinToString(":") { String.format("%02x", (0..255).random()) })

        // توليد IMEI صالح
        val imeiBase = (1..14).map { (0..9).random() }.joinToString("")
        val sum = imeiBase.reversed().mapIndexed { i, c ->
            val d = c.digitToInt()
            if (i % 2 == 0) { val x = d * 2; if (x > 9) x - 9 else x } else d
        }.sum()
        val check = (10 - (sum % 10)) % 10
        putString("spoof_imei", imeiBase + check)

        apply()
    }
}
