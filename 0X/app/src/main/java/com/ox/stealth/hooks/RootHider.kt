package com.ox.stealth.hooks

import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

/**
 * 0X Root Hider - إخفاء شامل للروت
 *
 * مصمم خصيصاً لتخطي حمايات تطبيق موارد:
 * - RootCheck.Droid (asm_094.dll): فحص test-keys, مسارات su, صلاحيات العمليات
 * - libucs-credential.so (Bitaqati SDK): فحص su, Superuser, Magisk, BusyBox
 * - IsFridaDetected (asm_002.dll): اكتشاف Frida
 * - AddRootedDeviceAsync (asm_003.dll): إبلاغ الخادم
 *
 * الطرق المستخدمة:
 * 1. File.exists() → false لمسارات الروت
 * 2. Runtime.exec() → فشل لأوامر su/which
 * 3. Build.TAGS → release-keys
 * 4. PackageManager → إخفاء تطبيقات الروت
 * 5. SystemProperties → إخفاء خصائص الروت
 * 6. /proc/mounts → إخفاء أقسام rw
 * 7. Native file access → إخفاء ملفات الروت
 * 8. SELinux → Enforcing
 * 9. Frida detection → إخفاء
 * 10. Magisk socket → إخفاء
 */
object RootHider {

    // مسارات الروت المعروفة التي يجب إخفاؤها
    private val ROOT_PATHS = setOf(
        "/system/bin/su", "/system/xbin/su", "/sbin/su",
        "/system/su", "/system/bin/.ext/.su", "/system/usr/we-need-root/su",
        "/data/local/su", "/data/local/bin/su", "/data/local/xbin/su",
        "/system/app/Superuser.apk", "/system/app/SuperSU.apk",
        "/system/app/SuperSU", "/system/app/Superuser",
        "/system/etc/init.d/99SuperSUDaemon",
        "/dev/com.koushikdutta.superuser.daemon/",
        "/system/xbin/daemonsu", "/system/xbin/busybox",
        "/system/bin/busybox", "/sbin/magisk",
        "/system/bin/magisk", "/system/xbin/magisk",
        "/data/adb/magisk", "/data/adb/modules",
        "/data/data/com.topjohnwu.magisk",
        "/data/user_de/0/com.topjohnwu.magisk",
        // Frida paths
        "/data/local/tmp/frida-server",
        "/data/local/tmp/re.frida.server",
        "/system/bin/frida-server",
        // Xposed paths (إخفاء ذاتي)
        "/system/framework/XposedBridge.jar",
        "/data/adb/lspd",
        "/data/adb/lsposed",
    )

    // أسماء حزم تطبيقات الروت
    private val ROOT_PACKAGES = setOf(
        "com.topjohnwu.magisk",
        "com.koushikdutta.superuser",
        "eu.chainfire.supersu",
        "com.noshufou.android.su",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.koushikdutta.rommanager",
        "com.dimonvideo.luckypatcher",
        "com.chelpus.lackypatch",
        "com.ramdroid.appquarantine",
        "com.devadvance.rootcloak",
        "com.devadvance.rootcloakplus",
        "de.robv.android.xposed.installer",
        "org.lsposed.manager",
        // Frida
        "re.frida.server",
    )

    // أوامر الروت
    private val ROOT_COMMANDS = setOf(
        "su", "which su", "busybox", "magisk",
        "which magisk", "id", "mount"
    )

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookFileExists(lpparam)
        hookFileCanRead(lpparam)
        hookRuntimeExec(lpparam)
        hookBuildTags(lpparam)
        hookPackageManager(lpparam)
        hookSystemProperties(lpparam)
        hookProcessBuilder(lpparam)
        hookNativeAccess(lpparam)
        hookFridaDetection(lpparam)
        hookShellCommands(lpparam)
        hookMagiskSocket(lpparam)
        hookSELinux(lpparam)
    }

    /**
     * Hook 1: File.exists() → false لمسارات الروت
     * يتخطى: RootCheck.Droid + libucs-credential.so
     */
    private fun hookFileExists(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedHelpers.findAndHookMethod(
            "java.io.File", lpparam.classLoader,
            "exists",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val file = param.thisObject as File
                    val path = file.absolutePath

                    if (ROOT_PATHS.any { path.contains(it, ignoreCase = true) } ||
                        path.contains("magisk", ignoreCase = true) ||
                        path.contains("supersu", ignoreCase = true) ||
                        path.contains("/su", ignoreCase = true) ||
                        path.contains("busybox", ignoreCase = true) ||
                        path.contains("frida", ignoreCase = true) ||
                        path.contains("xposed", ignoreCase = true) ||
                        path.contains("lsposed", ignoreCase = true) ||
                        path.contains("edxposed", ignoreCase = true)) {
                        param.result = false
                    }
                }
            }
        )
    }

    /**
     * Hook 2: File.canRead() / canWrite() / canExecute()
     */
    private fun hookFileCanRead(lpparam: XC_LoadPackage.LoadPackageParam) {
        val methods = listOf("canRead", "canWrite", "canExecute")
        for (method in methods) {
            try {
                XposedHelpers.findAndHookMethod(
                    "java.io.File", lpparam.classLoader,
                    method,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val file = param.thisObject as File
                            val path = file.absolutePath
                            if (ROOT_PATHS.any { path.contains(it, ignoreCase = true) }) {
                                param.result = false
                            }
                        }
                    }
                )
            } catch (_: Exception) {}
        }
    }

    /**
     * Hook 3: Runtime.exec() → فشل لأوامر الروت
     * يتخطى: RootCheck.Droid "which su" check
     */
    private fun hookRuntimeExec(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Hook exec(String)
        XposedHelpers.findAndHookMethod(
            "java.lang.Runtime", lpparam.classLoader,
            "exec", String::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val cmd = param.args[0] as? String ?: return
                    if (isRootCommand(cmd)) {
                        param.throwable = java.io.IOException("Permission denied")
                    }
                }
            }
        )

        // Hook exec(String[])
        XposedHelpers.findAndHookMethod(
            "java.lang.Runtime", lpparam.classLoader,
            "exec", Array<String>::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val cmds = param.args[0] as? Array<*> ?: return
                    val cmdStr = cmds.joinToString(" ")
                    if (isRootCommand(cmdStr)) {
                        param.throwable = java.io.IOException("Permission denied")
                    }
                }
            }
        )

        // Hook exec(String[], String[], File)
        try {
            XposedHelpers.findAndHookMethod(
                "java.lang.Runtime", lpparam.classLoader,
                "exec", Array<String>::class.java, Array<String>::class.java,
                File::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val cmds = param.args[0] as? Array<*> ?: return
                        val cmdStr = cmds.joinToString(" ")
                        if (isRootCommand(cmdStr)) {
                            param.throwable = java.io.IOException("Permission denied")
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    private fun isRootCommand(cmd: String): Boolean {
        val lower = cmd.lowercase()
        return lower.contains("su") || lower.contains("magisk") ||
               lower.contains("busybox") || lower.contains("which") ||
               lower.contains("mount") && lower.contains("/system")
    }

    /**
     * Hook 4: Build.TAGS → "release-keys"
     * يتخطى: RootCheck.Droid test-keys check
     */
    private fun hookBuildTags(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.setStaticObjectField(Build::class.java, "TAGS", "release-keys")
        } catch (_: Exception) {}

        // Hook SystemProperties.get("ro.build.tags")
        try {
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties", lpparam.classLoader,
                "get", String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        if (key == "ro.build.tags") {
                            param.result = "release-keys"
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 5: PackageManager → إخفاء تطبيقات الروت
     * يتخطى: libucs-credential.so Superuser/Magisk app check
     */
    private fun hookPackageManager(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Hook getPackageInfo
        XposedHelpers.findAndHookMethod(
            "android.app.ApplicationPackageManager", lpparam.classLoader,
            "getPackageInfo", String::class.java, Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val pkgName = param.args[0] as? String ?: return
                    if (pkgName in ROOT_PACKAGES) {
                        param.throwable = android.content.pm.PackageManager.NameNotFoundException(pkgName)
                    }
                }
            }
        )

        // Hook getInstalledPackages
        XposedHelpers.findAndHookMethod(
            "android.app.ApplicationPackageManager", lpparam.classLoader,
            "getInstalledPackages", Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val packages = param.result as? MutableList<*> ?: return
                    packages.removeAll { pkg ->
                        try {
                            val pkgName = XposedHelpers.getObjectField(pkg, "packageName") as? String
                            pkgName in ROOT_PACKAGES
                        } catch (_: Exception) { false }
                    }
                }
            }
        )

        // Hook getInstalledApplications
        XposedHelpers.findAndHookMethod(
            "android.app.ApplicationPackageManager", lpparam.classLoader,
            "getInstalledApplications", Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val apps = param.result as? MutableList<*> ?: return
                    apps.removeAll { app ->
                        try {
                            val pkgName = XposedHelpers.getObjectField(app, "packageName") as? String
                            pkgName in ROOT_PACKAGES
                        } catch (_: Exception) { false }
                    }
                }
            }
        )
    }

    /**
     * Hook 6: SystemProperties → إخفاء خصائص الروت
     */
    private fun hookSystemProperties(lpparam: XC_LoadPackage.LoadPackageParam) {
        val propsToHide = mapOf(
            "ro.build.selinux" to "1",
            "ro.debuggable" to "0",
            "ro.secure" to "1",
            "ro.build.type" to "user",
            "ro.build.tags" to "release-keys",
            "service.bootanim.exit" to "1",
            "init.svc.adbd" to "stopped",
            "ro.boot.vbmeta.device_state" to "locked",
            "ro.boot.verifiedbootstate" to "green",
            "ro.boot.flash.locked" to "1",
            "ro.boot.veritymode" to "enforcing",
        )

        try {
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties", lpparam.classLoader,
                "get", String::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        propsToHide[key]?.let { param.result = it }
                    }
                }
            )

            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties", lpparam.classLoader,
                "get", String::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return
                        propsToHide[key]?.let { param.result = it }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 7: ProcessBuilder → إخفاء أوامر الروت
     */
    private fun hookProcessBuilder(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "java.lang.ProcessBuilder", lpparam.classLoader,
                "start",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val pb = param.thisObject as ProcessBuilder
                        val cmd = pb.command().joinToString(" ")
                        if (isRootCommand(cmd)) {
                            param.throwable = java.io.IOException("Permission denied")
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 8: Native file access - /proc/mounts و /proc/self/mountinfo
     */
    private fun hookNativeAccess(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Hook BufferedReader لاعتراض قراءة /proc/mounts
        try {
            XposedHelpers.findAndHookMethod(
                "java.io.BufferedReader", lpparam.classLoader,
                "readLine",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val line = param.result as? String ?: return
                        // إخفاء أقسام rw التي تشير للروت
                        if (line.contains("/system") && line.contains("rw,")) {
                            param.result = line.replace("rw,", "ro,")
                        }
                        // إخفاء Magisk mounts
                        if (line.contains("magisk") || line.contains("tmpfs /system") ||
                            line.contains("tmpfs /vendor")) {
                            param.result = null
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 9: Frida Detection → إخفاء
     * يتخطى: IsFridaDetected في asm_002.dll
     */
    private fun hookFridaDetection(lpparam: XC_LoadPackage.LoadPackageParam) {
        // إخفاء منافذ Frida الافتراضية
        try {
            XposedHelpers.findAndHookMethod(
                "java.net.ServerSocket", lpparam.classLoader,
                "bind", java.net.SocketAddress::class.java,
                object : XC_MethodHook() {
                    // لا نمنع الربط - فقط نراقب
                }
            )
        } catch (_: Exception) {}

        // إخفاء /proc/self/maps من Frida libraries
        try {
            XposedHelpers.findAndHookMethod(
                "java.io.FileInputStream", lpparam.classLoader,
                "<init>", String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val path = param.args[0] as? String ?: return
                        if (path.contains("frida") || path.contains("gadget")) {
                            param.throwable = java.io.FileNotFoundException("No such file")
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 10: Shell commands - إخفاء الروت من أوامر shell
     */
    private fun hookShellCommands(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "java.lang.Runtime", lpparam.classLoader,
                "exec", String::class.java, Array<String>::class.java,
                File::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val cmd = param.args[0] as? String ?: return
                        if (isRootCommand(cmd)) {
                            param.throwable = java.io.IOException("not found")
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 11: Magisk Unix Domain Socket
     * يتخطى: RootCheck.Droid socket check
     */
    private fun hookMagiskSocket(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.net.LocalSocket", lpparam.classLoader,
                "connect",
                XposedHelpers.findClass("android.net.LocalSocketAddress", lpparam.classLoader),
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val address = param.args[0]
                            val name = XposedHelpers.callMethod(address, "getName") as? String
                            if (name != null && (name.contains("magisk") || name.contains("superuser"))) {
                                param.throwable = java.io.IOException("Connection refused")
                            }
                        } catch (_: Exception) {}
                    }
                }
            )
        } catch (_: Exception) {}
    }

    /**
     * Hook 12: SELinux → Enforcing
     */
    private fun hookSELinux(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val seLinuxClass = XposedHelpers.findClassIfExists(
                "android.os.SELinux", lpparam.classLoader
            )
            if (seLinuxClass != null) {
                XposedHelpers.findAndHookMethod(
                    seLinuxClass, "isSELinuxEnforced",
                    XC_MethodReplacement.returnConstant(true)
                )
            }
        } catch (_: Exception) {}
    }
}
