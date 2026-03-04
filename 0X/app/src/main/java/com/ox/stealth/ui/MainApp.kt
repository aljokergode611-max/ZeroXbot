package com.ox.stealth.ui

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch

/**
 * التطبيق الرئيسي - تصميم Material Design 3 الحديث
 * واجهة احترافية ومريحة للعين
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(prefs: SharedPreferences) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = LightBg,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                ModernTopBar()
            },
            bottomBar = {
                ModernBottomBar(selectedTab) { selectedTab = it }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> DashboardScreen(prefs, snackbarHostState)
                    1 -> LocationScreen(prefs, snackbarHostState)
                    2 -> DeviceScreen(prefs, snackbarHostState)
                    3 -> ShieldScreen(prefs, snackbarHostState)
                    4 -> SettingsScreen(prefs, snackbarHostState)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar() {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ModernBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_zerox),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
                Column {
                    Text(
                        text = "ZERO X",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ModernBlue,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "وحدة التخفي النشطة",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            textDirection = TextDirection.Rtl
                        )
                    )
                }
            }
        },
        actions = {
            Surface(
                shape = CircleShape,
                color = SuccessGreen.copy(alpha = 0.1f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
        ),
        modifier = Modifier.shadow(2.dp)
    )
}

@Composable
fun ModernBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        TabItem("الرئيسية", Icons.Filled.Dashboard),
        TabItem("الموقع", Icons.Filled.MyLocation),
        TabItem("الجهاز", Icons.Filled.PhoneAndroid),
        TabItem("الدرع", Icons.Filled.Shield),
        TabItem("الإعدادات", Icons.Filled.Settings),
    )

    NavigationBar(
        containerColor = Color.White,
        contentColor = ModernBlue,
        tonalElevation = 8.dp,
        modifier = Modifier.shadow(8.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        tab.icon, tab.label,
                        tint = if (selectedTab == index) ModernBlue else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == index) ModernBlue else TextTertiary,
                        style = TextStyle(textDirection = TextDirection.Rtl)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = ModernBlue.copy(alpha = 0.1f),
                    selectedIconColor = ModernBlue,
                    unselectedIconColor = TextTertiary
                )
            )
        }
    }
}

data class TabItem(val label: String, val icon: ImageVector)

// ===================== شاشة لوحة التحكم =====================

@Composable
fun DashboardScreen(prefs: SharedPreferences, snackbarHostState: SnackbarHostState) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

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
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textDirection = TextDirection.Rtl
            )
        )

        ModulesGrid(prefs, snackbarHostState)

        // إحصائيات
        Text(
            text = "📊 إحصائيات التخفي",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textDirection = TextDirection.Rtl
            )
        )

        StatsCard()
    }
}

@Composable
fun StatusCard(prefs: SharedPreferences) {
    val isActive = prefs.getBoolean("module_active", true)

    ModernCard {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulse by infiniteTransition.animateFloat(
                    initialValue = 0.6f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "pulse"
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isActive) SuccessGreen.copy(alpha = pulse) else ErrorRed)
                )
                Text(
                    text = if (isActive) "الوحدة نشطة - التخفي مفعّل" else "الوحدة معطّلة",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) SuccessGreen else ErrorRed,
                        textDirection = TextDirection.Rtl
                    )
                )
            }

            HorizontalDivider(color = BorderLight)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusIndicator("الروت", "مخفي", SuccessGreen)
                StatusIndicator("الموقع", "مزيّف", SuccessGreen)
                StatusIndicator("الجهاز", "مزيّف", SuccessGreen)
                StatusIndicator("الدرع", "نشط", SuccessGreen)
            }
        }
    }
}

@Composable
fun StatusIndicator(label: String, status: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = status,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            style = TextStyle(textDirection = TextDirection.Rtl)
        )
    }
}

@Composable
fun ModulesGrid(prefs: SharedPreferences, snackbarHostState: SnackbarHostState) {
    val modules = listOf(
        ModuleInfo("إخفاء الروت", "تخطي فحوصات su و Magisk", Icons.Filled.Security, "root_hide"),
        ModuleInfo("تزييف الموقع", "GPS Spoofing بدون Mock API", Icons.Filled.LocationOn, "location_spoof"),
        ModuleInfo("إخفاء Mock", "isFromMockProvider → false", Icons.Filled.LocationOff, "mock_hide"),
        ModuleInfo("تزييف الجهاز", "IMEI, Android ID, Serial", Icons.Filled.PhoneAndroid, "device_spoof"),
        ModuleInfo("إخفاء Frida", "تخطي IsFridaDetected", Icons.Filled.BugReport, "frida_hide"),
        ModuleInfo("جهاز شرعي", "إخفاء VPN, ADB, Dev Mode", Icons.Filled.VerifiedUser, "legit_device"),
    )
    
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        modules.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { module ->
                    ModuleCard(
                        module = module,
                        isEnabled = prefs.getBoolean(module.prefKey, true),
                        onToggle = { 
                            prefs.edit().putBoolean(module.prefKey, it).apply()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (it) "${module.title} تم التفعيل ✓" else "${module.title} تم الإيقاف",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
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

    ModernCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (enabled) ModernBlue.copy(alpha = 0.1f) else Color(0xFFF0F0F0),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            module.icon, null,
                            tint = if (enabled) ModernBlue else TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it; onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ModernGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = BorderLight
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
            Text(
                text = module.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) TextPrimary else TextSecondary,
                style = TextStyle(textDirection = TextDirection.Rtl)
            )
            Text(
                text = module.desc,
                fontSize = 10.sp,
                color = TextTertiary,
                lineHeight = 14.sp,
                style = TextStyle(textDirection = TextDirection.Rtl)
            )
        }
    }
}

@Composable
fun StatsCard() {
    ModernCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatRow("عمليات الإخفاء", "2,847", SuccessGreen)
            StatRow("محاولات الكشف المحظورة", "156", ErrorRed)
            StatRow("الهوكات النشطة", "48", InfoBlue)
            StatRow("آخر تحديث للموقع", "الآن", SuccessGreen)
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
            fontSize = 13.sp,
            color = TextSecondary,
            style = TextStyle(textDirection = TextDirection.Rtl)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// سأكمل في الملف التالي...
