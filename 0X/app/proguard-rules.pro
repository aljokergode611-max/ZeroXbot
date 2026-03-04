# 0X Stealth Module - ProGuard Rules
-keep class com.ox.stealth.hooks.** { *; }
-keep class de.robv.android.xposed.** { *; }
-keepclassmembers class * implements de.robv.android.xposed.IXposedHookLoadPackage {
    public void handleLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam);
}
-dontwarn de.robv.android.xposed.**

# OSMDroid
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**
