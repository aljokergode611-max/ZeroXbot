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

// ===================== شاشة الإعدادات =====================

@Composable
fun SettingsScreen(prefs: SharedPreferences, snackbarHostState: SnackbarHostState) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showAppPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader("⚙️ إعدادات عامة", "التحكم بالوحدة")

        ModernCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ToggleRow("تفعيل الوحدة", "module_active", prefs, snackbarHostState)
                ToggleRow("وضع التخفي الكامل", "full_stealth", prefs, snackbarHostState)
                ToggleRow("تسجيل العمليات (Debug Log)", "debug_log", prefs, snackbarHostState)
            }
        }

        SectionHeader("🎯 التطبيقات المستهدفة", "اختر التطبيقات التي تريد تطبيق التخفي عليها")

        ModernCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { showAppPicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ModernBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Apps, null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "اختيار التطبيقات",
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(textDirection = TextDirection.Rtl)
                    )
                }
            }
        }

        if (showAppPicker) {
            AppPickerDialog(
                prefs = prefs,
                onDismiss = { showAppPicker = false }
            )
        }

        SectionHeader("ℹ️ حول", "معلومات التطبيق")

        ModernCard {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ModernBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_zerox),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
                Text(
                    "ZERO X",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = ModernBlue,
                    letterSpacing = 2.sp
                )
                Text(
                    "الإصدار 1.0.0",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    style = TextStyle(textDirection = TextDirection.Rtl)
                )
                Text(
                    "وحدة تخفي متقدمة لـ LSPosed",
                    fontSize = 12.sp,
                    color = TextTertiary,
                    style = TextStyle(textDirection = TextDirection.Rtl)
                )
                
                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 8.dp))
                
                // إضافة الشكر
                Text(
                    "Dev Group 0X",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    style = TextStyle(textDirection = TextDirection.Rtl)
                )
                Text(
                    "Thanks 3nad Alyami",
                    fontSize = 13.sp,
                    color = ModernBlue,
                    fontWeight = FontWeight.Medium,
                    style = TextStyle(textDirection = TextDirection.Rtl)
                )
            }
        }
    }
}

@Composable
fun AppPickerDialog(prefs: SharedPreferences, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    val installedApps = remember {
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // تطبيقات المستخدم فقط
            .sortedBy { packageManager.getApplicationLabel(it).toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "اختر التطبيقات",
                style = TextStyle(textDirection = TextDirection.Rtl, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(installedApps) { app ->
                    val packageName = app.packageName
                    var isEnabled by remember { 
                        mutableStateOf(prefs.getBoolean("target_$packageName", false)) 
                    }
                    
                    AppTargetRow(
                        appName = packageManager.getApplicationLabel(app).toString(),
                        packageName = packageName,
                        isEnabled = isEnabled,
                        onToggle = { enabled ->
                            isEnabled = enabled
                            prefs.edit().putBoolean("target_$packageName", enabled).apply()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("تم", color = ModernBlue, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AppTargetRow(appName: String, packageName: String, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isEnabled) ModernBlue.copy(alpha = 0.05f) else Color.Transparent)
            .border(
                1.dp,
                if (isEnabled) ModernBlue.copy(alpha = 0.2f) else BorderLight,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isEnabled) TextPrimary else TextSecondary,
                style = TextStyle(textDirection = TextDirection.Rtl)
            )
            Text(
                text = packageName,
                fontSize = 11.sp,
                color = TextTertiary
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ModernGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = BorderLight
            )
        )
    }
}

// ===================== مكونات مشتركة =====================

@Composable
fun ModernCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        content()
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textDirection = TextDirection.Rtl
            )
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    textDirection = TextDirection.Rtl
                )
            )
        }
    }
}

@Composable
fun ToggleRow(label: String, prefKey: String, prefs: SharedPreferences, snackbarHostState: SnackbarHostState) {
    var enabled by remember { mutableStateOf(prefs.getBoolean(prefKey, true)) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (enabled) TextPrimary else TextSecondary,
            modifier = Modifier.weight(1f),
            style = TextStyle(textDirection = TextDirection.Rtl)
        )
        Switch(
            checked = enabled,
            onCheckedChange = {
                enabled = it
                prefs.edit().putBoolean(prefKey, it).apply()
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = if (it) "$label ✓" else "$label تم الإيقاف",
                        duration = SnackbarDuration.Short
                    )
                }
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ModernGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = BorderLight
            )
        )
    }
}

@Composable
fun CoordinateInput(label: String, prefKey: String, defaultValue: String, prefs: SharedPreferences) {
    var value by remember { mutableStateOf(prefs.getString(prefKey, defaultValue) ?: defaultValue) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
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
                    .height(52.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = ModernBlue,
                    fontWeight = FontWeight.Medium
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ModernBlue,
                    unfocusedBorderColor = BorderLight,
                    cursorColor = ModernBlue,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = LightBg
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
    }
}

@Composable
fun DeviceField(label: String, prefKey: String, prefs: SharedPreferences) {
    var value by remember { mutableStateOf(prefs.getString(prefKey, "") ?: "") }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
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
                    .height(52.dp),
                placeholder = {
                    Text("تلقائي (عشوائي)", fontSize = 12.sp, color = TextTertiary)
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ModernBlue,
                    unfocusedBorderColor = BorderLight,
                    cursorColor = ModernBlue,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = LightBg
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
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

// ===================== مربع حوار البحث عن الموقع =====================

@Composable
fun SearchLocationDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (Double, Double, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // قائمة أماكن شائعة في السعودية كأمثلة
    val popularPlaces = remember {
        listOf(
            LocationSuggestion("الرياض", 24.7136, 46.6753),
            LocationSuggestion("جدة", 21.5433, 39.1728),
            LocationSuggestion("مكة المكرمة", 21.3891, 39.8579),
            LocationSuggestion("المدينة المنورة", 24.5247, 39.5692),
            LocationSuggestion("الدمام", 26.4207, 50.0888),
            LocationSuggestion("الخبر", 26.2172, 50.1971),
            LocationSuggestion("أبها", 18.2164, 42.5053),
            LocationSuggestion("الطائف", 21.2703, 40.4150),
            LocationSuggestion("تبوك", 28.3838, 36.5550),
            LocationSuggestion("بريدة", 26.3260, 43.9750),
        )
    }
    
    val filteredPlaces = if (searchQuery.isEmpty()) {
        popularPlaces
    } else {
        popularPlaces.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "البحث عن مكان",
                style = TextStyle(
                    textDirection = TextDirection.Rtl,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // حقل البحث
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "اكتب اسم المدينة...",
                            style = TextStyle(textDirection = TextDirection.Rtl)
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, null, tint = ModernBlue)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ModernBlue,
                        unfocusedBorderColor = BorderLight,
                        cursorColor = ModernBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = TextStyle(textDirection = TextDirection.Rtl)
                )
                
                // قائمة الأماكن
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPlaces) { place ->
                        LocationSuggestionItem(
                            suggestion = place,
                            onClick = {
                                onLocationSelected(place.lat, place.lng, place.name)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

data class LocationSuggestion(
    val name: String,
    val lat: Double,
    val lng: Double
)

@Composable
fun LocationSuggestionItem(
    suggestion: LocationSuggestion,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = ModernBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.LocationOn,
                            null,
                            tint = ModernBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = suggestion.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        style = TextStyle(textDirection = TextDirection.Rtl)
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "${suggestion.lat}, ${suggestion.lng}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            Icon(
                Icons.Filled.ArrowForward,
                null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
