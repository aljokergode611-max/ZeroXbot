package com.ox.stealth.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ox.stealth.ui.theme.OXTheme
import java.io.File

/**
 * 0X Stealth Module - Main Activity
 * واجهة مستخدم حديثة بتصميم Material 3 مع قفل أمان
 */
class MainActivity : ComponentActivity() {

    companion object {
        const val PREFS_NAME = "ox_config"
        const val LOCK_CODE = "911900"
        const val PREF_IS_UNLOCKED = "is_unlocked"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // استخدام MODE_WORLD_READABLE للسماح لـ Xposed بقراءة الإعدادات
        val prefs = try {
            @Suppress("DEPRECATION")
            getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE)
        } catch (e: SecurityException) {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).also {
                // محاولة جعل الملف قابل للقراءة من الخارج
                try {
                    val prefsFile = File(applicationInfo.dataDir, "shared_prefs/$PREFS_NAME.xml")
                    prefsFile.setReadable(true, false)
                } catch (_: Exception) {}
            }
        }

        setContent {
            OXTheme {
                // التحقق من حالة الفتح المحفوظة
                var isUnlocked by remember { mutableStateOf(prefs.getBoolean(PREF_IS_UNLOCKED, false)) }

                if (!isUnlocked) {
                    LockScreen(onUnlock = { 
                        isUnlocked = true
                        // حفظ حالة الفتح
                        prefs.edit().putBoolean(PREF_IS_UNLOCKED, true).apply()
                    })
                } else {
                    MainApp(prefs = prefs)
                }
            }
        }
    }
}
