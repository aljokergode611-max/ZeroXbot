package com.ox.stealth.hooks

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method

/**
 * 0X Location Hook - أقوى نظام تزييف موقع
 *
 * يعمل بدون Mock Location API - يحقن الموقع مباشرة في كل واجهات برمجة الموقع
 * مستوحى من XposedFakeLocation + Geergit + MotionEmulator
 *
 * الـ APIs المستهدفة:
 * - LocationManager.getLastKnownLocation()
 * - LocationManager.requestLocationUpdates() (كل overloads)
 * - LocationManager.requestSingleUpdate()
 * - LocationManager.getCurrentLocation() (Android 11+)
 * - Location constructor and setters
 * - GnssStatus callbacks
 * - Google Play Services FusedLocationProviderClient
 */
object LocationHook {

    private var fakeLat: Double = 0.0
    private var fakeLng: Double = 0.0
    private var fakeAlt: Double = 0.0
    private var fakeAccuracy: Float = 3.5f
    private var fakeSpeed: Float = 0f
    private var fakeBearing: Float = 0f

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam, prefs: XSharedPreferences?) {
        loadCoordinates(prefs)

        hookLocationManager(lpparam)
        hookLocationObject(lpparam)
        hookLocationListener(lpparam)
        hookGooglePlayServices(lpparam)
        hookGnssStatus(lpparam)
        hookGeocoder(lpparam)
        hookCellLocation(lpparam)
        hookWifiLocation(lpparam)
    }

    private fun loadCoordinates(prefs: XSharedPreferences?) {
        prefs?.reload()
        fakeLat = java.lang.Double.longBitsToDouble(
            prefs?.getLong("fake_lat", java.lang.Double.doubleToLongBits(24.7136)) ?: java.lang.Double.doubleToLongBits(24.7136)
        )
        fakeLng = java.lang.Double.longBitsToDouble(
            prefs?.getLong("fake_lng", java.lang.Double.doubleToLongBits(46.6753)) ?: java.lang.Double.doubleToLongBits(46.6753)
        )
        fakeAlt = java.lang.Double.longBitsToDouble(
            prefs?.getLong("fake_alt", java.lang.Double.doubleToLongBits(612.0)) ?: java.lang.Double.doubleToLongBits(612.0)
        )
        fakeAccuracy = prefs?.getFloat("fake_accuracy", 3.5f) ?: 3.5f
    }

    /**
     * إنشاء كائن Location مزيف يبدو حقيقياً تماماً
     */
    private fun createFakeLocation(provider: String): Location {
        val location = Location(provider)
        location.latitude = fakeLat
        location.longitude = fakeLng
        location.altitude = fakeAlt
        location.accuracy = fakeAccuracy
        location.speed = fakeSpeed
        location.bearing = fakeBearing
        location.time = System.currentTimeMillis()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            location.bearingAccuracyDegrees = 5.0f
            location.speedAccuracyMetersPerSecond = 0.5f
            location.verticalAccuracyMeters = 3.0f
        }

        location.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()

        // إزالة أي علامة Mock - مهم جداً
        try {
            val extrasField = Location::class.java.getDeclaredField("mExtras")
            extrasField.isAccessible = true
            val extras = extrasField.get(location) as? Bundle
            extras?.remove("mockProvider")
            extras?.remove("isFromMockProvider")
        } catch (_: Exception) {}

        // إزالة علامة isMock
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                XposedHelpers.callMethod(location, "setMock", false)
            } else {
                XposedHelpers.callMethod(location, "setIsFromMockProvider", false)
            }
        } catch (_: Exception) {}

        return location
    }

    /**
     * Hook كل دوال LocationManager
     */
    private fun hookLocationManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        val classLoader = lpparam.classLoader

        // === getLastKnownLocation ===
        XposedHelpers.findAndHookMethod(
            "android.location.LocationManager", classLoader,
            "getLastKnownLocation", String::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val provider = param.args[0] as? String ?: LocationManager.GPS_PROVIDER
                    param.result = createFakeLocation(provider)
                }
            }
        )

        // === requestLocationUpdates (4 args - الأكثر شيوعاً) ===
        try {
            XposedHelpers.findAndHookMethod(
                "android.location.LocationManager", classLoader,
                "requestLocationUpdates",
                String::class.java, Long::class.javaPrimitiveType,
                Float::class.javaPrimitiveType, LocationListener::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val listener = param.args[3] as? LocationListener ?: return
                        val provider = param.args[0] as? String ?: LocationManager.GPS_PROVIDER
                        listener.onLocationChanged(createFakeLocation(provider))
                    }
                }
            )
        } catch (_: Exception) {}

        // === requestSingleUpdate ===
        try {
            XposedHelpers.findAndHookMethod(
                "android.location.LocationManager", classLoader,
                "requestSingleUpdate",
                String::class.java, LocationListener::class.java,
                android.os.Looper::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val listener = param.args[1] as? LocationListener ?: return
                        val provider = param.args[0] as? String ?: LocationManager.GPS_PROVIDER
                        listener.onLocationChanged(createFakeLocation(provider))
                    }
                }
            )
        } catch (_: Exception) {}

        // === getCurrentLocation (Android 11+) ===
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val locationManagerClass = XposedHelpers.findClass(
                    "android.location.LocationManager", classLoader
                )
                for (method in locationManagerClass.declaredMethods) {
                    if (method.name == "getCurrentLocation") {
                        XposedBridge.hookMethod(method, object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                // يتم إرسال الموقع المزيف عبر الـ callback
                                for (arg in param.args) {
                                    if (arg != null) {
                                        try {
                                            val acceptMethod = arg.javaClass.getMethod("accept", Any::class.java)
                                            acceptMethod.invoke(arg, createFakeLocation(LocationManager.GPS_PROVIDER))
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                        })
                    }
                }
            } catch (_: Exception) {}
        }

        // === isProviderEnabled - إرجاع true دائماً لـ GPS ===
        try {
            XposedHelpers.findAndHookMethod(
                "android.location.LocationManager", classLoader,
                "isProviderEnabled", String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val provider = param.args[0] as? String
                        if (provider == LocationManager.GPS_PROVIDER ||
                            provider == LocationManager.NETWORK_PROVIDER) {
                            param.result = true
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook كائن Location نفسه - لضمان أن أي Location يمر عبر التطبيق يحمل إحداثياتنا
     */
    private fun hookLocationObject(lpparam: XC_LoadPackage.LoadPackageParam) {
        val classLoader = lpparam.classLoader

        // Hook getLatitude
        XposedHelpers.findAndHookMethod(
            "android.location.Location", classLoader,
            "getLatitude",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = fakeLat
                }
            }
        )

        // Hook getLongitude
        XposedHelpers.findAndHookMethod(
            "android.location.Location", classLoader,
            "getLongitude",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = fakeLng
                }
            }
        )

        // Hook getAltitude
        XposedHelpers.findAndHookMethod(
            "android.location.Location", classLoader,
            "getAltitude",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = fakeAlt
                }
            }
        )

        // Hook getAccuracy
        XposedHelpers.findAndHookMethod(
            "android.location.Location", classLoader,
            "getAccuracy",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = fakeAccuracy
                }
            }
        )

        // Hook getSpeed
        XposedHelpers.findAndHookMethod(
            "android.location.Location", classLoader,
            "getSpeed",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = fakeSpeed
                }
            }
        )

        // Hook getBearing
        XposedHelpers.findAndHookMethod(
            "android.location.Location", classLoader,
            "getBearing",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = fakeBearing
                }
            }
        )
    }

    /**
     * Hook LocationListener لاعتراض كل تحديثات الموقع
     */
    private fun hookLocationListener(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.location.LocationListener", lpparam.classLoader,
                "onLocationChanged", Location::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val location = param.args[0] as? Location ?: return
                        location.latitude = fakeLat
                        location.longitude = fakeLng
                        location.altitude = fakeAlt
                        location.accuracy = fakeAccuracy
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook Google Play Services FusedLocationProviderClient
     * هذا مهم جداً لأن كثير من التطبيقات تستخدم Google Play Services بدلاً من LocationManager
     */
    private fun hookGooglePlayServices(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook FusedLocationProviderClient.getLastLocation()
            val fusedClass = XposedHelpers.findClass(
                "com.google.android.gms.location.FusedLocationProviderClient",
                lpparam.classLoader
            )
            for (method in fusedClass.declaredMethods) {
                if (method.name == "getLastLocation" || method.name == "getCurrentLocation") {
                    XposedBridge.hookMethod(method, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            // النتيجة هي Task<Location>
                            try {
                                val task = param.result ?: return
                                val resultField = task.javaClass.getDeclaredField("zzb")
                                resultField.isAccessible = true
                                resultField.set(task, createFakeLocation(LocationManager.FUSED_PROVIDER))
                            } catch (_: Exception) {}
                        }
                    })
                }
            }

            // Hook LocationCallback.onLocationResult
            try {
                XposedHelpers.findAndHookMethod(
                    "com.google.android.gms.location.LocationCallback",
                    lpparam.classLoader,
                    "onLocationResult",
                    XposedHelpers.findClass(
                        "com.google.android.gms.location.LocationResult",
                        lpparam.classLoader
                    ),
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            try {
                                val locationResult = param.args[0] ?: return
                                val locationsField = locationResult.javaClass.getDeclaredField("zzb")
                                locationsField.isAccessible = true
                                val locations = locationsField.get(locationResult) as? MutableList<Location>
                                locations?.let {
                                    it.clear()
                                    it.add(createFakeLocation(LocationManager.FUSED_PROVIDER))
                                }
                            } catch (_: Exception) {}
                        }
                    }
                )
            } catch (_: Exception) {}

        } catch (_: Exception) {
            // Google Play Services غير متوفر - نتجاهل
        }
    }

    /**
     * Hook GNSS Status لمحاكاة أقمار GPS حقيقية
     */
    private fun hookGnssStatus(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return

        try {
            // Hook GnssStatus.Callback.onSatelliteStatusChanged
            XposedHelpers.findAndHookMethod(
                "android.location.GnssStatus.Callback", lpparam.classLoader,
                "onSatelliteStatusChanged",
                XposedHelpers.findClass("android.location.GnssStatus", lpparam.classLoader),
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        // نسمح بمرور بيانات الأقمار الحقيقية لتبدو طبيعية
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook Geocoder لمنع الكشف عبر العنوان العكسي
     */
    private fun hookGeocoder(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.location.Geocoder", lpparam.classLoader,
                "getFromLocation",
                Double::class.javaPrimitiveType, Double::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.args[0] = fakeLat
                        param.args[1] = fakeLng
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook Cell Location لمنع الكشف عبر أبراج الاتصال
     */
    private fun hookCellLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.telephony.TelephonyManager", lpparam.classLoader,
                "getCellLocation",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = null
                    }
                }
            )

            XposedHelpers.findAndHookMethod(
                "android.telephony.TelephonyManager", lpparam.classLoader,
                "getAllCellInfo",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = emptyList<Any>()
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook WiFi Location لمنع الكشف عبر شبكات WiFi
     */
    private fun hookWifiLocation(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.net.wifi.WifiManager", lpparam.classLoader,
                "getScanResults",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = emptyList<Any>()
                    }
                }
            )
        } catch (_: Exception) {}
    }

    // === دوال تحديث الموقع من الواجهة ===

    fun updateLocation(lat: Double, lng: Double) {
        fakeLat = lat
        fakeLng = lng
    }

    fun updateMovement(speed: Float, bearing: Float) {
        fakeSpeed = speed
        fakeBearing = bearing
    }
}
