package com.ox.stealth.hooks

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Zero X Stealth Module - نقطة الدخول الرئيسية
 *
 * يتم تحميل كل الـ Hooks من هنا بالترتيب:
 * 1. إخفاء الروت (RootHider)
 * 2. إخفاء Mock Location (MockLocationHider)
 * 3. تزييف الموقع (LocationHook)
 * 4. الحركة الطبيعية (NaturalMovement)
 * 5. تزييف هوية الجهاز (DeviceSpoofer)
 * 6. جهاز شرعي (LegitDevice)
 */
class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        const val TAG = "ZeroX"
        const val PACKAGE_NAME = "com.ox.stealth"
        var prefs: XSharedPreferences? = null

        fun log(msg: String) {
            XposedBridge.log("[$TAG] $msg")
        }
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        prefs = XSharedPreferences(PACKAGE_NAME, "ox_config")
        prefs?.makeWorldReadable()
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // تجاهل التطبيق نفسه
        if (lpparam.packageName == PACKAGE_NAME) return

        // إعادة تحميل الإعدادات
        prefs?.reload()

        // التحقق من تفعيل الوحدة
        if (prefs?.getBoolean("module_active", true) != true) return

        // التحقق من التطبيقات المستهدفة
        val targetAll = prefs?.getBoolean("target_*", false) ?: false
        val targetMawared = prefs?.getBoolean("target_com.tcs.nim", true) ?: true

        if (!targetAll) {
            // وضع الاستهداف المحدد
            if (targetMawared && lpparam.packageName == "com.tcs.nim") {
                // مستهدف - نكمل
            } else if (!targetMawared) {
                return // لا يوجد أي تطبيق مستهدف
            } else if (lpparam.packageName != "com.tcs.nim") {
                return // ليس التطبيق المستهدف
            }
        }

        log("⚡ بدء التخفي في: ${lpparam.packageName}")

        val fullStealth = prefs?.getBoolean("full_stealth", true) ?: true

        try {
            // === الطبقة 1: إخفاء الروت ===
            if (fullStealth || prefs?.getBoolean("hide_su_files", true) == true) {
                RootHider.hook(lpparam)
                log("✅ إخفاء الروت - نشط")
            }

            // === الطبقة 2: إخفاء Mock Location ===
            if (fullStealth || prefs?.getBoolean("hide_mock_provider", true) == true) {
                MockLocationHider.hook(lpparam)
                log("✅ إخفاء Mock Location - نشط")
            }

            // === الطبقة 3: تزييف الموقع ===
            if (prefs?.getBoolean("location_enabled", false) == true) {
                LocationHook.hook(lpparam, prefs)
                log("✅ تزييف الموقع - نشط")

                // === الطبقة 3.5: الحركة الطبيعية ===
                if (prefs?.getBoolean("natural_movement", false) == true) {
                    NaturalMovement.hook(lpparam, prefs)
                    log("✅ الحركة الطبيعية - نشطة")
                }
            }

            // === الطبقة 4: تزييف هوية الجهاز ===
            if (prefs?.getBoolean("device_spoof_enabled", false) == true) {
                DeviceSpoofer.hook(lpparam, prefs)
                log("✅ تزييف الجهاز - نشط")
            }

            // === الطبقة 5: جهاز شرعي ===
            if (fullStealth || prefs?.getBoolean("hide_vpn", true) == true) {
                LegitDevice.hook(lpparam, prefs)
                log("✅ جهاز شرعي - نشط")
            }

            log("🛡️ التخفي مكتمل في: ${lpparam.packageName}")

        } catch (e: Throwable) {
            log("❌ خطأ في ${lpparam.packageName}: ${e.message}")
        }
    }
}
