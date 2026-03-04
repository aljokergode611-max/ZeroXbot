package com.ox.stealth.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ox.stealth.R
import com.ox.stealth.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * شاشة القفل - تصميم Material Design 3 الحديث
 * رمز القفل: 911900
 */
@Composable
fun LockScreen(onUnlock: () -> Unit) {
    var enteredCode by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(isError) {
        if (isError) {
            delay(800)
            isError = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB),
                        Color(0xFF90CAF9)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                // شعار Zero X الحديث
                ModernLogo()

                // عنوان
                Text(
                    text = "ZERO X",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        color = ModernBlue,
                        letterSpacing = 3.sp
                    )
                )

                Text(
                    text = "وحدة التخفي المتقدمة",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextSecondary,
                        textDirection = TextDirection.Rtl
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // صندوق إدخال الرمز الحديث
                ModernCodeBox(
                    enteredCode = enteredCode,
                    isError = isError
                )

                Spacer(modifier = Modifier.height(8.dp))

                // لوحة الأرقام الحديثة
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModernKeypad(
                        onDigit = { digit ->
                            isError = false
                            if (enteredCode.length < 6) {
                                enteredCode += digit
                                if (enteredCode.length == 6) {
                                    if (enteredCode == MainActivity.LOCK_CODE) {
                                        onUnlock()
                                    } else {
                                        isError = true
                                        enteredCode = ""
                                    }
                                }
                            }
                        },
                        onDelete = {
                            if (enteredCode.isNotEmpty()) {
                                enteredCode = enteredCode.dropLast(1)
                            }
                        }
                    )
                }

                // تحذير بسيط
                Text(
                    text = "⚠ الوصول غير المصرح به ممنوع",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    style = TextStyle(textDirection = TextDirection.Rtl)
                )
            }
        }
    }
}

@Composable
fun ModernLogo() {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.size(120.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_zerox),
                contentDescription = "Zero X Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
fun ModernCodeBox(enteredCode: String, isError: Boolean) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = if (isError) "❌ رمز خاطئ" else "أدخل رمز الوصول",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = if (isError) ErrorRed else TextSecondary,
                    textDirection = TextDirection.Rtl
                )
            )

            // نقاط الرمز الحديثة
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(6) { index ->
                        val isFilled = index < enteredCode.length
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isError -> ErrorRed.copy(alpha = 0.1f)
                                isFilled -> ModernBlue
                                else -> LightBg
                            },
                            border = if (!isFilled) BorderStroke(2.dp, BorderLight) else null,
                            modifier = Modifier.size(14.dp)
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
fun ModernKeypad(onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        buttons.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { label ->
                    if (label.isEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Button(
                            onClick = {
                                if (label == "DEL") onDelete() else onDigit(label)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = if (label == "DEL") ErrorRed else ModernBlue
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            if (label == "DEL") {
                                Icon(
                                    Icons.Default.Backspace,
                                    contentDescription = "حذف",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = label,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
