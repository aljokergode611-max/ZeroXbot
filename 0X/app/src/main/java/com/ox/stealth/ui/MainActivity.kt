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

        val prefs = try {
            getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE)
        } catch (e: SecurityException) {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
