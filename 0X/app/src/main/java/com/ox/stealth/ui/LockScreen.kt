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
 * شاشة القفل - تصميم Zero X هاكر
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

    Box(modifier = Modifier.fillMaxSize()) {
        // خلفية Matrix
        MatrixRainBackground()

        // طبقة شفافة فوق Matrix
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // شعار Zero X - الأيقونة الأصلية
                    ZeroXLogo()

                    // عنوان
                    Text(
                        text = "ZERO X",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = ZeroXRed,
                            shadow = Shadow(
                                color = ZeroXRedGlow.copy(alpha = 0.6f),
                                offset = Offset(0f, 0f),
                                blurRadius = 15f
                            ),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 6.sp
                        )
                    )

                    Text(
                        text = "وحدة التخفي",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = HackerGreen.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace,
                            textDirection = TextDirection.Rtl,
                            letterSpacing = 2.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // صندوق إدخال الرمز
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0A0A0A))
                            .border(
                                1.dp,
                                if (isError) HackerRed.copy(alpha = 0.5f)
                                else HackerGreen.copy(alpha = 0.15f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "[ أدخل رمز الوصول ]",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = if (isError) HackerRed else HackerGreen.copy(alpha = 0.6f),
                                    fontFamily = FontFamily.Monospace,
                                    textDirection = TextDirection.Rtl
                                )
                            )

                            // نقاط الرمز
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    repeat(6) { index ->
                                        val isFilled = index < enteredCode.length
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .then(
                                                    if (isFilled) {
                                                        Modifier.background(
                                                            if (isError) HackerRed else HackerGreen
                                                        )
                                                    } else {
                                                        Modifier
                                                            .background(Color(0xFF1A1A1A))
                                                            .border(
                                                                1.dp,
                                                                HackerGreen.copy(alpha = 0.2f),
                                                                CircleShape
                                                            )
                                                    }
                                                )
                                        )
                                    }
                                }
                            }

                            if (isError) {
                                Text(
                                    text = "⛔ تم رفض الوصول",
                                    color = HackerRed,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    style = TextStyle(textDirection = TextDirection.Rtl)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // لوحة الأرقام
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        HackerKeypad(
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

                    // تحذير
                    Text(
                        text = "⚠ الوصول غير المصرح به ممنوع",
                        color = ZeroXRed.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        style = TextStyle(textDirection = TextDirection.Rtl)
                    )
                }
            }
        }
    }
}

@Composable
fun ZeroXLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logoGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .size(110.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                2.dp,
                Brush.linearGradient(
                    colors = listOf(
                        ZeroXRed.copy(alpha = glowAlpha),
                        HackerGreen.copy(alpha = glowAlpha)
                    )
                ),
                RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_zerox),
            contentDescription = "Zero X Logo",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(20.dp))
        )
    }
}

@Composable
fun HackerKeypad(onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                .height(56.dp)
                                .border(
                                    1.dp,
                                    if (label == "DEL") ZeroXRed.copy(alpha = 0.2f)
                                    else HackerGreen.copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0C0C0C),
                                contentColor = if (label == "DEL") ZeroXRed else HackerGreen
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            if (label == "DEL") {
                                Icon(
                                    Icons.Default.Backspace,
                                    contentDescription = "حذف",
                                    tint = ZeroXRed.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = label,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixRainBackground() {
    val columns = 25
    val drops = remember {
        mutableStateListOf<Float>().apply {
            repeat(columns) { add(Random.nextFloat()) }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "matrix")
    val tick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tick"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val colWidth = size.width / columns

        for (i in 0 until columns) {
            val y = drops.getOrElse(i) { 0f } * size.height

            // نقطة خضراء متوهجة
            drawCircle(
                color = HackerGreen.copy(alpha = 0.3f),
                radius = 3f,
                center = Offset(i * colWidth + colWidth / 2, y)
            )

            // ذيل خافت
            for (j in 1..5) {
                drawCircle(
                    color = HackerGreen.copy(alpha = 0.05f * (5 - j)),
                    radius = 2f,
                    center = Offset(i * colWidth + colWidth / 2, y - j * 15f)
                )
            }

            if (i < drops.size) {
                drops[i] = if (drops[i] * size.height > size.height && Random.nextFloat() > 0.97f) {
                    0f
                } else {
                    drops[i] + 0.008f
                }
            }
        }
    }
}
