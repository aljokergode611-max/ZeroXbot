package com.ox.stealth.ui

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ox.stealth.R
import com.ox.stealth.ui.theme.*
import kotlinx.coroutines.launch

// ===================== شاشة الموقع =====================

@Composable
fun LocationScreen(prefs: SharedPreferences, snackbarHostState: SnackbarHostState) {
    var lat by remember { mutableStateOf(prefs.getString("spoof_lat", "24.7136")?.toDoubleOrNull() ?: 24.7136) }
    var lng by remember { mutableStateOf(prefs.getString("spoof_lng", "46.6753")?.toDoubleOrNull() ?: 46.6753) }
    var selectedLayer by remember { mutableStateOf(MapLayer.ROADS) }
    var showSettings by remember { mutableStateOf(false) }
    var isSpoofActive by remember { mutableStateOf(prefs.getBoolean("location_enabled", false)) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // الخريطة التفاعلية
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
                scope.launch {
                    snackbarHostState.showSnackbar("تم تحديث الموقع ✓")
                }
            }
        )

        // معلومات الموقع في الأعلى
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter)
        ) {
            // شريط البحث
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSearchDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Search,
                        null,
                        tint = ModernBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "ابحث عن مكان...",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        style = TextStyle(textDirection = TextDirection.Rtl)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "الإحداثيات",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                "${"%,.6f".format(lat)}, ${"%,.6f".format(lng)}",
                                fontSize = 13.sp,
                                color = ModernBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSpoofActive) SuccessGreen.copy(alpha = 0.15f) else BorderLight
                    ) {
                        Text(
                            if (isSpoofActive) "نشط" else "متوقف",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSpoofActive) SuccessGreen else TextTertiary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
        
        // مربع حوار البحث
        if (showSearchDialog) {
            SearchLocationDialog(
                onDismiss = { showSearchDialog = false },
                onLocationSelected = { newLat, newLng, placeName ->
                    lat = newLat
                    lng = newLng
                    prefs.edit()
                        .putString("spoof_lat", "%.6f".format(newLat))
                        .putString("spoof_lng", "%.6f".format(newLng))
                        .apply()
                    showSearchDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("تم الانتقال إلى: $placeName ✓")
                    }
                }
            )
        }

        // أزرار طبقات الخريطة
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapLayer.values().forEach { layer ->
                FloatingActionButton(
                    onClick = { selectedLayer = layer },
                    modifier = Modifier.size(48.dp),
                    containerColor = if (selectedLayer == layer) ModernBlue else Color.White,
                    contentColor = if (selectedLayer == layer) Color.White else TextPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(layer.icon, fontSize = 18.sp)
                }
            }
        }

        // شريط التحكم السفلي
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            isSpoofActive = !isSpoofActive
                            prefs.edit().putBoolean("location_enabled", isSpoofActive).apply()
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isSpoofActive) "تم تفعيل تزييف الموقع ✓" else "تم إيقاف تزييف الموقع"
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSpoofActive) ErrorRed else SuccessGreen
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            if (isSpoofActive) Icons.Filled.Close else Icons.Filled.PlayArrow,
                            null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isSpoofActive) "إيقاف التزييف" else "تفعيل التزييف",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FloatingActionButton(
                        onClick = { showSettings = !showSettings },
                        modifier = Modifier.size(50.dp),
                        containerColor = LightBg,
                        contentColor = ModernBlue,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Settings, null)
                    }
                }

                // الإعدادات المتقدمة
                if (showSettings) {
                    HorizontalDivider(color = BorderLight)
                    
                    // قابل للتمرير
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "الحركة الطبيعية",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            style = TextStyle(textDirection = TextDirection.Rtl)
                        )
                        ToggleRow("تفعيل الحركة الطبيعية", "natural_movement", prefs, snackbarHostState)
                        CoordinateInput("نطاق التذبذب (أمتار)", "jitter_range", "5.0", prefs)
                        CoordinateInput("الدقة (أمتار)", "spoof_accuracy", "3.5", prefs)
                        CoordinateInput("الارتفاع (متر)", "spoof_alt", "612.0", prefs)

                        HorizontalDivider(color = BorderLight)
                        Text(
                            "إخفاء Mock Location",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernBlue,
                            style = TextStyle(textDirection = TextDirection.Rtl)
                        )
                        ToggleRow("إخفاء isFromMockProvider", "hide_mock_provider", prefs, snackbarHostState)
                        ToggleRow("إخفاء isMock (Android 12+)", "hide_is_mock", prefs, snackbarHostState)
                        ToggleRow("تنظيف Extras من علامات Mock", "clean_extras", prefs, snackbarHostState)
                        ToggleRow("إخفاء إعداد Mock Location", "hide_mock_setting", prefs, snackbarHostState)
                        ToggleRow("إخفاء صلاحية Mock Location", "hide_mock_permission", prefs, snackbarHostState)
                    }
                }
            }
        }
    }
}

// ===================== شاشة الجهاز =====================

@Composable
fun DeviceScreen(prefs: SharedPreferences, snackbarHostState: SnackbarHostState) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("📱 تزييف هوية الجهاز", "تغيير كل معرفات الجهاز")

        ModernCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleRow("تفعيل تزييف الجهاز", "device_spoof_enabled", prefs, snackbarHostState)
                HorizontalDivider(color = BorderLight)
                DeviceField("Android ID", "spoof_android_id", prefs)
                DeviceField("IMEI", "spoof_imei", prefs)
                DeviceField("الرقم التسلسلي", "spoof_serial", prefs)
                DeviceField("عنوان WiFi MAC", "spoof_wifi_mac", prefs)
                DeviceField("عنوان Bluetooth MAC", "spoof_bt_mac", prefs)
            }
        }

        SectionHeader("🏭 تزييف معلومات البناء", "تغيير موديل وشركة الجهاز")

        ModernCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DeviceField("الشركة المصنعة", "spoof_manufacturer", prefs)
                DeviceField("الموديل", "spoof_model", prefs)
                DeviceField("العلامة التجارية", "spoof_brand", prefs)
                DeviceField("البصمة (Fingerprint)", "spoof_fingerprint", prefs)
            }
        }

        Button(
            onClick = { 
                generateRandomDevice(prefs)
                scope.launch {
                    snackbarHostState.showSnackbar("تم توليد هوية عشوائية جديدة ✓")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ModernBlue)
        ) {
            Icon(Icons.Filled.Casino, null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "🎲 توليد هوية عشوائية",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                style = TextStyle(textDirection = TextDirection.Rtl)
            )
        }
    }
}

// ===================== شاشة الدرع =====================

@Composable
fun ShieldScreen(prefs: SharedPreferences, snackbarHostState: SnackbarHostState) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("🔒 إخفاء الروت", "تخطي كل فحوصات صلاحيات الروت")

        ModernCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleRow("إخفاء ملفات su", "hide_su_files", prefs, snackbarHostState)
                ToggleRow("إخفاء تطبيقات الروت", "hide_root_apps", prefs, snackbarHostState)
                ToggleRow("تزييف Build.TAGS → release-keys", "fake_build_tags", prefs, snackbarHostState)
                ToggleRow("حظر أوامر الروت", "block_root_commands", prefs, snackbarHostState)
                ToggleRow("إخفاء Magisk Socket", "hide_magisk_socket", prefs, snackbarHostState)
                ToggleRow("تزييف SELinux → Enforcing", "fake_selinux", prefs, snackbarHostState)
                ToggleRow("إخفاء /proc/mounts", "hide_proc_mounts", prefs, snackbarHostState)
            }
        }

        SectionHeader("🕵️ إخفاء أدوات التحليل", "منع اكتشاف أدوات الهندسة العكسية")

        ModernCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleRow("إخفاء Frida", "hide_frida", prefs, snackbarHostState)
                ToggleRow("إخفاء Xposed/LSPosed", "hide_xposed", prefs, snackbarHostState)
                ToggleRow("تنظيف Stack Traces", "clean_stack_traces", prefs, snackbarHostState)
                ToggleRow("إخفاء من /proc/self/maps", "hide_proc_maps", prefs, snackbarHostState)
            }
        }

        SectionHeader("✅ جهاز شرعي", "جعل الجهاز يبدو غير معدّل")

        ModernCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleRow("إخفاء VPN", "hide_vpn", prefs, snackbarHostState)
                ToggleRow("إخفاء وضع المطور", "hide_dev_options", prefs, snackbarHostState)
                ToggleRow("إخفاء USB Debugging", "hide_usb_debug", prefs, snackbarHostState)
                ToggleRow("إخفاء Bootloader المفتوح", "hide_bootloader", prefs, snackbarHostState)
                ToggleRow("إخفاء ADB", "hide_adb", prefs, snackbarHostState)
                ToggleRow("تزييف Verified Boot → green", "fake_verified_boot", prefs, snackbarHostState)
            }
        }
    }
}

// سأكمل في الملف التالي...
