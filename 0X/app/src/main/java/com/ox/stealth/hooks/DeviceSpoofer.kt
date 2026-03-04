package com.ox.stealth.hooks

import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.UUID

/**
 * 0X Device Spoofer - تزييف هوية الجهاز بالكامل
 *
 * يزيف كل معرفات الجهاز لمنع التتبع والكشف:
 * - Android ID
 * - IMEI / MEID
 * - Serial Number
 * - Build Properties (Model, Manufacturer, Fingerprint)
 * - WiFi MAC Address
 * - Bluetooth MAC Address
 * - Google Advertising ID
 * - GSF ID (Google Services Framework)
 *
 * مستوحى من AndroidFaker + DeviceSpoofLab-Hooks + Geergit
 */
object DeviceSpoofer {

    private var spoofedAndroidId: String = ""
    private var spoofedIMEI: String = ""
    private var spoofedSerial: String = ""
    private var spoofedModel: String = ""
    private var spoofedManufacturer: String = ""
    private var spoofedBrand: String = ""
    private var spoofedFingerprint: String = ""
    private var spoofedWifiMac: String = ""
    private var spoofedBluetoothMac: String = ""

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam, prefs: XSharedPreferences?) {
        loadSpoofedValues(prefs)
        hookAndroidId(lpparam)
        hookTelephonyManager(lpparam)
        hookBuildProperties(lpparam)
        hookSerialNumber(lpparam)
        hookWifiMac(lpparam)
        hookBluetoothMac(lpparam)
        hookAdvertisingId(lpparam)
        hookContentResolver(lpparam)
    }

    private fun loadSpoofedValues(prefs: XSharedPreferences?) {
        prefs?.reload()
        spoofedAndroidId = prefs?.getString("spoof_android_id", generateRandomHex(16)) ?: generateRandomHex(16)
        spoofedIMEI = prefs?.getString("spoof_imei", generateRandomIMEI()) ?: generateRandomIMEI()
        spoofedSerial = prefs?.getString("spoof_serial", generateRandomSerial()) ?: generateRandomSerial()
        spoofedModel = prefs?.getString("spoof_model", "") ?: ""
        spoofedManufacturer = prefs?.getString("spoof_manufacturer", "") ?: ""
        spoofedBrand = prefs?.getString("spoof_brand", "") ?: ""
        spoofedFingerprint = prefs?.getString("spoof_fingerprint", "") ?: ""
        spoofedWifiMac = prefs?.getString("spoof_wifi_mac", generateRandomMac()) ?: generateRandomMac()
        spoofedBluetoothMac = prefs?.getString("spoof_bt_mac", generateRandomMac()) ?: generateRandomMac()
    }

    /**
     * Hook Android ID (Settings.Secure.ANDROID_ID)
     */
    private fun hookAndroidId(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.provider.Settings\$Secure", lpparam.classLoader,
                "getString",
                android.content.ContentResolver::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[1] as? String ?: return
                        if (key == "android_id" && spoofedAndroidId.isNotEmpty()) {
                            param.result = spoofedAndroidId
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook TelephonyManager - IMEI, MEID, Phone Number, Subscriber ID
     */
    private fun hookTelephonyManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        val telephonyMethods = mapOf(
            "getDeviceId" to spoofedIMEI,
            "getImei" to spoofedIMEI,
            "getMeid" to spoofedIMEI,
            "getSubscriberId" to generateRandomNumeric(15),
            "getSimSerialNumber" to generateRandomNumeric(20),
            "getLine1Number" to "",
        )

        for ((method, value) in telephonyMethods) {
            try {
                // بدون args
                XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager", lpparam.classLoader,
                    method,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            if (value.isNotEmpty()) param.result = value
                        }
                    }
                )
            } catch (_: Exception) {}

            // مع slot index
            try {
                XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager", lpparam.classLoader,
                    method, Int::class.javaPrimitiveType,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            if (value.isNotEmpty()) param.result = value
                        }
                    }
                )
            } catch (_: Exception) {}
        }
    }

    /**
     * Hook Build Properties - Model, Manufacturer, Brand, Fingerprint
     */
    private fun hookBuildProperties(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            if (spoofedModel.isNotEmpty()) {
                XposedHelpers.setStaticObjectField(Build::class.java, "MODEL", spoofedModel)
                XposedHelpers.setStaticObjectField(Build::class.java, "PRODUCT", spoofedModel)
                XposedHelpers.setStaticObjectField(Build::class.java, "DEVICE", spoofedModel.lowercase())
            }
            if (spoofedManufacturer.isNotEmpty()) {
                XposedHelpers.setStaticObjectField(Build::class.java, "MANUFACTURER", spoofedManufacturer)
            }
            if (spoofedBrand.isNotEmpty()) {
                XposedHelpers.setStaticObjectField(Build::class.java, "BRAND", spoofedBrand)
            }
            if (spoofedFingerprint.isNotEmpty()) {
                XposedHelpers.setStaticObjectField(Build::class.java, "FINGERPRINT", spoofedFingerprint)
            }
            // دائماً إخفاء علامات الروت
            XposedHelpers.setStaticObjectField(Build::class.java, "TAGS", "release-keys")
            XposedHelpers.setStaticObjectField(Build::class.java, "TYPE", "user")
        } catch (_: Exception) {}
    }

    /**
     * Hook Serial Number
     */
    private fun hookSerialNumber(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.setStaticObjectField(Build::class.java, "SERIAL", spoofedSerial)
        } catch (_: Exception) {}

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                XposedHelpers.findAndHookMethod(
                    "android.os.Build", lpparam.classLoader,
                    "getSerial",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = spoofedSerial
                        }
                    }
                )
            } catch (_: Exception) {}
        }
    }

    /**
     * Hook WiFi MAC Address
     */
    private fun hookWifiMac(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.net.wifi.WifiInfo", lpparam.classLoader,
                "getMacAddress",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = spoofedWifiMac
                    }
                }
            )
        } catch (_: Exception) {}

        // Hook NetworkInterface.getHardwareAddress()
        try {
            XposedHelpers.findAndHookMethod(
                "java.net.NetworkInterface", lpparam.classLoader,
                "getHardwareAddress",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val ni = param.thisObject as java.net.NetworkInterface
                        if (ni.name == "wlan0" || ni.name == "eth0") {
                            param.result = macStringToBytes(spoofedWifiMac)
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook Bluetooth MAC Address
     */
    private fun hookBluetoothMac(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.bluetooth.BluetoothAdapter", lpparam.classLoader,
                "getAddress",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = spoofedBluetoothMac
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook Google Advertising ID
     */
    private fun hookAdvertisingId(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val adIdClass = XposedHelpers.findClassIfExists(
                "com.google.android.gms.ads.identifier.AdvertisingIdClient\$Info",
                lpparam.classLoader
            )
            if (adIdClass != null) {
                XposedHelpers.findAndHookMethod(
                    adIdClass, "getId",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = UUID.randomUUID().toString()
                        }
                    }
                )
            }
        } catch (_: Exception) {}
    }

    /**
     * Hook ContentResolver.query لاعتراض استعلامات GSF ID
     */
    private fun hookContentResolver(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.content.ContentResolver", lpparam.classLoader,
                "query",
                android.net.Uri::class.java,
                Array<String>::class.java,
                String::class.java,
                Array<String>::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val uri = param.args[0]?.toString() ?: return
                        if (uri.contains("com.google.android.gsf.gservices")) {
                            // إرجاع null لمنع الوصول لـ GSF ID
                            param.result = null
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    // === مولدات القيم العشوائية ===

    private fun generateRandomHex(length: Int): String {
        val chars = "0123456789abcdef"
        return (1..length).map { chars.random() }.joinToString("")
    }

    private fun generateRandomIMEI(): String {
        val digits = (1..14).map { (0..9).random() }.joinToString("")
        // حساب Luhn checksum
        val sum = digits.reversed().mapIndexed { i, c ->
            val d = c.digitToInt()
            if (i % 2 == 0) {
                val doubled = d * 2
                if (doubled > 9) doubled - 9 else doubled
            } else d
        }.sum()
        val check = (10 - (sum % 10)) % 10
        return digits + check
    }

    private fun generateRandomSerial(): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..12).map { chars.random() }.joinToString("")
    }

    private fun generateRandomMac(): String {
        return (1..6).joinToString(":") {
            String.format("%02x", (0..255).random())
        }
    }

    private fun generateRandomNumeric(length: Int): String {
        return (1..length).map { (0..9).random() }.joinToString("")
    }

    private fun macStringToBytes(mac: String): ByteArray {
        return mac.split(":").map { it.toInt(16).toByte() }.toByteArray()
    }
}
