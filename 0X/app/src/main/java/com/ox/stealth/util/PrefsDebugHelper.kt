package com.ox.stealth.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.io.File

/**
 * أداة مساعدة لتشخيص مشاكل SharedPreferences مع Xposed
 */
object PrefsDebugHelper {
    
    private const val TAG = "ZeroX-Debug"
    private const val PREFS_NAME = "ox_config"
    
    /**
     * إصلاح أذونات ملف SharedPreferences
     */
    fun fixPrefsPermissions(context: Context) {
        try {
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            val prefsFile = File(prefsDir, "$PREFS_NAME.xml")
            
            if (prefsFile.exists()) {
                // جعل المجلد قابل للقراءة والتنفيذ
                prefsDir.setReadable(true, false)
                prefsDir.setExecutable(true, false)
                
                // جعل الملف قابل للقراءة
                prefsFile.setReadable(true, false)
                
                Log.d(TAG, "✅ تم إصلاح أذونات الملف: ${prefsFile.absolutePath}")
                Log.d(TAG, "📄 الأذونات: قراءة=${prefsFile.canRead()} حجم=${prefsFile.length()}")
            } else {
                Log.w(TAG, "⚠️ ملف الإعدادات غير موجود بعد")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ خطأ في إصلاح الأذونات: ${e.message}")
        }
    }
    
    /**
     * طباعة جميع الإعدادات للتشخيص
     */
    fun debugPrintPrefs(prefs: SharedPreferences) {
        val all = prefs.all
        Log.d(TAG, "=== جميع الإعدادات ===")
        all.forEach { (key, value) ->
            Log.d(TAG, "$key = $value (${value?.javaClass?.simpleName})")
        }
        Log.d(TAG, "======================")
    }
    
    /**
     * التحقق من إعدادات تزييف الموقع
     */
    fun checkLocationSettings(prefs: SharedPreferences): LocationSettings {
        return LocationSettings(
            enabled = prefs.getBoolean("location_enabled", false),
            lat = prefs.getString("spoof_lat", "0.0")?.toDoubleOrNull() ?: 0.0,
            lng = prefs.getString("spoof_lng", "0.0")?.toDoubleOrNull() ?: 0.0,
            accuracy = prefs.getString("spoof_accuracy", "3.5")?.toFloatOrNull() ?: 3.5f
        )
    }
    
    data class LocationSettings(
        val enabled: Boolean,
        val lat: Double,
        val lng: Double,
        val accuracy: Float
    ) {
        override fun toString(): String {
            return """
                LocationSettings:
                  enabled: $enabled
                  lat: $lat
                  lng: $lng
                  accuracy: $accuracy
            """.trimIndent()
        }
    }
}
