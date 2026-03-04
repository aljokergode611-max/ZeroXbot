package com.ox.stealth.hooks

import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 0X Mock Location Hider - إخفاء كامل لعلامات الموقع الوهمي
 *
 * يتخطى كل طرق الكشف المعروفة:
 * 1. Location.isFromMockProvider() → false
 * 2. Location.isMock() (Android 12+) → false
 * 3. Location.getExtras() → إزالة mockProvider
 * 4. Settings.Secure "allow_mock_location" → "0"
 * 5. Settings.Secure "mock_location" → "0"
 * 6. AppOpsManager MOCK_LOCATION check → MODE_IGNORED
 * 7. PackageManager "android.permission.ACCESS_MOCK_LOCATION" → deny
 * 8. Developer Options detection → hidden
 *
 * مصمم خصيصاً لتخطي فحوصات تطبيق موارد:
 * - isFromMockProvider() في asm_187.dll
 * - IsBetterLocation accuracy checks
 * - HmsIsSpoof في asm_018.dll / asm_020.dll
 */
object MockLocationHider {

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookIsFromMockProvider(lpparam)
        hookIsMock(lpparam)
        hookLocationExtras(lpparam)
        hookSettingsSecure(lpparam)
        hookAppOpsManager(lpparam)
        hookDeveloperOptions(lpparam)
        hookPackageManager(lpparam)
    }

    /**
     * Hook 1: Location.isFromMockProvider() → false
     * هذا هو الفحص الأساسي في تطبيق موارد (asm_187.dll)
     */
    private fun hookIsFromMockProvider(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.location.Location", lpparam.classLoader,
                "isFromMockProvider",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = false
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 2: Location.isMock() (Android 12+) → false
     */
    private fun hookIsMock(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                XposedHelpers.findAndHookMethod(
                    "android.location.Location", lpparam.classLoader,
                    "isMock",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = false
                        }
                    }
                )
            } catch (_: Exception) {}
        }
    }

    /**
     * Hook 3: Location.getExtras() → إزالة كل علامات Mock
     */
    private fun hookLocationExtras(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.location.Location", lpparam.classLoader,
                "getExtras",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val extras = param.result as? Bundle ?: return
                        // إزالة كل العلامات المعروفة
                        extras.remove("mockProvider")
                        extras.remove("isFromMockProvider")
                        extras.remove("isMock")
                        extras.remove("mock")
                        extras.remove("mockLocation")
                        param.result = extras
                    }
                }
            )
        } catch (_: Exception) {}

        // Hook setExtras أيضاً لمنع إضافة علامات Mock
        try {
            XposedHelpers.findAndHookMethod(
                "android.location.Location", lpparam.classLoader,
                "setExtras", Bundle::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val extras = param.args[0] as? Bundle ?: return
                        extras.remove("mockProvider")
                        extras.remove("isFromMockProvider")
                        extras.remove("isMock")
                        extras.remove("mock")
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 4: Settings.Secure - إخفاء إعدادات Mock Location
     */
    private fun hookSettingsSecure(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook getString
            XposedHelpers.findAndHookMethod(
                "android.provider.Settings\$Secure", lpparam.classLoader,
                "getString",
                android.content.ContentResolver::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[1] as? String ?: return
                        when (key) {
                            "mock_location",
                            "allow_mock_location" -> param.result = "0"
                            Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED -> param.result = "0"
                        }
                    }
                }
            )

            // Hook getInt
            XposedHelpers.findAndHookMethod(
                "android.provider.Settings\$Secure", lpparam.classLoader,
                "getInt",
                android.content.ContentResolver::class.java, String::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[1] as? String ?: return
                        when (key) {
                            "mock_location",
                            "allow_mock_location" -> param.result = 0
                            Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED -> param.result = 0
                        }
                    }
                }
            )

            // Hook getInt بدون default value
            XposedHelpers.findAndHookMethod(
                "android.provider.Settings\$Secure", lpparam.classLoader,
                "getInt",
                android.content.ContentResolver::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[1] as? String ?: return
                        when (key) {
                            "mock_location",
                            "allow_mock_location" -> param.result = 0
                            Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED -> param.result = 0
                        }
                    }
                }
            )
        } catch (_: Exception) {}

        // Hook Settings.Global أيضاً
        try {
            XposedHelpers.findAndHookMethod(
                "android.provider.Settings\$Global", lpparam.classLoader,
                "getInt",
                android.content.ContentResolver::class.java, String::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[1] as? String ?: return
                        if (key == Settings.Global.DEVELOPMENT_SETTINGS_ENABLED) {
                            param.result = 0
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 5: AppOpsManager - إخفاء صلاحية Mock Location
     */
    private fun hookAppOpsManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.app.AppOpsManager", lpparam.classLoader,
                "checkOp",
                Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val op = param.args[0] as Int
                        // OP_MOCK_LOCATION = 58
                        if (op == 58) {
                            param.result = 1 // MODE_IGNORED
                        }
                    }
                }
            )

            XposedHelpers.findAndHookMethod(
                "android.app.AppOpsManager", lpparam.classLoader,
                "checkOpNoThrow",
                Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val op = param.args[0] as Int
                        if (op == 58) {
                            param.result = 1 // MODE_IGNORED
                        }
                    }
                }
            )

            // Hook noteOp أيضاً
            XposedHelpers.findAndHookMethod(
                "android.app.AppOpsManager", lpparam.classLoader,
                "noteOp",
                Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
                String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val op = param.args[0] as Int
                        if (op == 58) {
                            param.result = 1
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 6: إخفاء وضع المطور (Developer Options)
     */
    private fun hookDeveloperOptions(lpparam: XC_LoadPackage.LoadPackageParam) {
        // تم التعامل معه في hookSettingsSecure
        // إضافة hook لـ SystemProperties أيضاً
        try {
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties", lpparam.classLoader,
                "get", String::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        if (key.contains("mock") || key.contains("debug")) {
                            param.result = param.args[1] // إرجاع القيمة الافتراضية
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 7: PackageManager - إخفاء صلاحية ACCESS_MOCK_LOCATION
     */
    private fun hookPackageManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.app.ApplicationPackageManager", lpparam.classLoader,
                "checkPermission",
                String::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val permission = param.args[0] as? String ?: return
                        if (permission == "android.permission.ACCESS_MOCK_LOCATION") {
                            param.result = -1 // PERMISSION_DENIED
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }
}
