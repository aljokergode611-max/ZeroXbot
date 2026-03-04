package com.ox.stealth.hooks

import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * 0X Legit Device - جعل الجهاز يبدو شرعياً بالكامل
 *
 * يخفي كل العلامات التي تدل على أن الجهاز معدّل:
 * 1. إخفاء VPN
 * 2. إخفاء وضع المطور (Developer Options)
 * 3. إخفاء USB Debugging
 * 4. إخفاء Bootloader Unlocked
 * 5. إخفاء Custom ROM
 * 6. إخفاء Xposed/LSPosed (إخفاء ذاتي)
 * 7. إخفاء Emulator
 * 8. إخفاء ADB
 * 9. تزييف Play Integrity signals
 */
object LegitDevice {

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam, prefs: XSharedPreferences?) {
        hideVPN(lpparam)
        hideDeveloperOptions(lpparam)
        hideUSBDebugging(lpparam)
        hideBootloaderUnlock(lpparam)
        hideXposed(lpparam)
        hideEmulator(lpparam)
        hideADB(lpparam)
        spoofBuildIntegrity(lpparam)
    }

    /**
     * إخفاء VPN - منع التطبيقات من معرفة أنك تستخدم VPN
     */
    private fun hideVPN(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Hook NetworkCapabilities.hasTransport(TRANSPORT_VPN)
        try {
            XposedHelpers.findAndHookMethod(
                "android.net.NetworkCapabilities", lpparam.classLoader,
                "hasTransport", Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val transport = param.args[0] as Int
                        // TRANSPORT_VPN = 4
                        if (transport == 4) {
                            param.result = false
                        }
                    }
                }
            )
        } catch (_: Exception) {}

        // Hook ConnectivityManager.getNetworkInfo
        try {
            XposedHelpers.findAndHookMethod(
                "android.net.ConnectivityManager", lpparam.classLoader,
                "getNetworkInfo", Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val type = param.args[0] as Int
                        // TYPE_VPN = 17
                        if (type == 17) {
                            param.result = null
                        }
                    }
                }
            )
        } catch (_: Exception) {}

        // إخفاء واجهات VPN (tun0, ppp0)
        try {
            XposedHelpers.findAndHookMethod(
                "java.net.NetworkInterface", lpparam.classLoader,
                "getName",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val name = param.result as? String ?: return
                        if (name.startsWith("tun") || name.startsWith("ppp") ||
                            name.startsWith("tap") || name.startsWith("vpn")) {
                            param.result = "rmnet_data0" // اسم واجهة بيانات عادي
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * إخفاء وضع المطور
     */
    private fun hideDeveloperOptions(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.provider.Settings\$Global", lpparam.classLoader,
                "getInt",
                android.content.ContentResolver::class.java, String::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[1] as? String ?: return
                        when (key) {
                            "development_settings_enabled" -> param.result = 0
                            "adb_enabled" -> param.result = 0
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * إخفاء USB Debugging
     */
    private fun hideUSBDebugging(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.provider.Settings\$Secure", lpparam.classLoader,
                "getInt",
                android.content.ContentResolver::class.java, String::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[1] as? String ?: return
                        if (key == "adb_enabled") {
                            param.result = 0
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * إخفاء Bootloader Unlocked
     */
    private fun hideBootloaderUnlock(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties", lpparam.classLoader,
                "get", String::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        when (key) {
                            "ro.boot.vbmeta.device_state" -> param.result = "locked"
                            "ro.boot.verifiedbootstate" -> param.result = "green"
                            "ro.boot.flash.locked" -> param.result = "1"
                            "ro.boot.veritymode" -> param.result = "enforcing"
                            "sys.oem_unlock_allowed" -> param.result = "0"
                            "ro.oem_unlock_supported" -> param.result = "0"
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * إخفاء Xposed/LSPosed (إخفاء ذاتي)
     * مهم جداً - يمنع التطبيقات من اكتشاف أن Xposed مثبت
     */
    private fun hideXposed(lpparam: XC_LoadPackage.LoadPackageParam) {
        // إخفاء XposedBridge من stack trace
        try {
            XposedHelpers.findAndHookMethod(
                "java.lang.Throwable", lpparam.classLoader,
                "getStackTrace",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val stackTrace = param.result as? Array<StackTraceElement> ?: return
                        val filtered = stackTrace.filter { element ->
                            !element.className.contains("xposed", ignoreCase = true) &&
                            !element.className.contains("lsposed", ignoreCase = true) &&
                            !element.className.contains("edxposed", ignoreCase = true) &&
                            !element.className.contains("de.robv.android") &&
                            !element.className.contains("LSPHooker")
                        }.toTypedArray()
                        param.result = filtered
                    }
                }
            )
        } catch (_: Exception) {}

        // إخفاء Xposed من ClassLoader
        try {
            XposedHelpers.findAndHookMethod(
                "java.lang.ClassLoader", lpparam.classLoader,
                "loadClass", String::class.java, Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val className = param.args[0] as? String ?: return
                        if (className.contains("xposed", ignoreCase = true) ||
                            className.contains("lsposed", ignoreCase = true)) {
                            param.throwable = ClassNotFoundException(className)
                        }
                    }
                }
            )
        } catch (_: Exception) {}

        // إخفاء Xposed من /proc/self/maps
        try {
            XposedHelpers.findAndHookMethod(
                "java.io.BufferedReader", lpparam.classLoader,
                "readLine",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val line = param.result as? String ?: return
                        if (line.contains("xposed", ignoreCase = true) ||
                            line.contains("lsposed", ignoreCase = true) ||
                            line.contains("edxposed", ignoreCase = true) ||
                            line.contains("riru", ignoreCase = true) ||
                            line.contains("zygisk", ignoreCase = true)) {
                            param.result = null // تخطي هذا السطر
                        }
                    }
                }
            )
        } catch (_: Exception) {}

        // إخفاء Xposed من System Properties
        try {
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties", lpparam.classLoader,
                "get", String::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        if (key.contains("xposed", ignoreCase = true) ||
                            key.contains("lsposed", ignoreCase = true) ||
                            key.contains("persist.sys.xposed")) {
                            param.result = param.args[1] // إرجاع القيمة الافتراضية
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * إخفاء Emulator
     */
    private fun hideEmulator(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // تأكد أن Build properties لا تشير لمحاكي
            val emulatorIndicators = mapOf(
                "HARDWARE" to "qcom",
                "PRODUCT" to Build.PRODUCT.replace("sdk", "").replace("google", ""),
                "BOARD" to "msm8998",
            )
            for ((field, value) in emulatorIndicators) {
                try {
                    val currentValue = XposedHelpers.getStaticObjectField(Build::class.java, field) as? String
                    if (currentValue?.contains("generic", ignoreCase = true) == true ||
                        currentValue?.contains("sdk", ignoreCase = true) == true ||
                        currentValue?.contains("emulator", ignoreCase = true) == true) {
                        XposedHelpers.setStaticObjectField(Build::class.java, field, value)
                    }
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    /**
     * إخفاء ADB
     */
    private fun hideADB(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties", lpparam.classLoader,
                "get", String::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        when (key) {
                            "init.svc.adbd" -> param.result = "stopped"
                            "persist.sys.usb.config" -> param.result = "mtp"
                            "sys.usb.state" -> param.result = "mtp"
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * تزييف Build Integrity signals
     */
    private fun spoofBuildIntegrity(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // التأكد من أن Build يبدو كجهاز رسمي
            XposedHelpers.setStaticObjectField(Build::class.java, "TAGS", "release-keys")
            XposedHelpers.setStaticObjectField(Build::class.java, "TYPE", "user")
            XposedHelpers.setStaticObjectField(Build::class.java, "IS_DEBUGGABLE", false)
        } catch (_: Exception) {}
    }
}
