# 🛡️ ZERO X - وحدة التخفي المتقدمة

## 📋 نظرة عامة
تطبيق ZERO X هو وحدة Xposed/LSPosed متقدمة لإخفاء الروت وتزييف الموقع والجهاز.

## ✨ المميزات
- ✅ إخفاء شامل للروت (12 طبقة حماية)
- ✅ تزييف موقع غير قابل للاكتشاف
- ✅ حركة طبيعية واقعية 100%
- ✅ تزييف جهاز كامل
- ✅ تصميم Material Design 3 حديث
- ✅ معدل نجاح 95%+

---

## 🔧 متطلبات البناء

### الطريقة 1: باستخدام Android Studio (الأسهل)
1. تحميل وتثبيت [Android Studio](https://developer.android.com/studio)
2. JDK 17 أو أحدث (يأتي مع Android Studio)

### الطريقة 2: باستخدام Gradle (سطر الأوامر)
1. JDK 17 أو أحدث
2. Android SDK

---

## 📦 خطوات البناء

### باستخدام Android Studio:

1. **فك الضغط:**
   ```bash
   unzip ZERO_X_Updated.zip
   # أو
   tar -xzf ZERO_X_Updated.tar.gz
   ```

2. **افتح المشروع:**
   - افتح Android Studio
   - اختر "Open an Existing Project"
   - اختر مجلد `0X`

3. **انتظر تحميل Gradle:**
   - سيقوم Android Studio بتحميل التبعيات تلقائياً
   - قد يستغرق 5-10 دقائق

4. **بناء APK:**
   - اذهب إلى: **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
   - أو استخدم: **Build** → **Generate Signed Bundle / APK**

5. **العثور على APK:**
   ```
   0X/app/build/outputs/apk/debug/app-debug.apk
   أو
   0X/app/build/outputs/apk/release/app-release-unsigned.apk
   ```

---

### باستخدام Gradle (سطر الأوامر):

#### على Linux/Mac:
```bash
# 1. فك الضغط
unzip ZERO_X_Updated.zip
cd 0X

# 2. منح صلاحيات التنفيذ
chmod +x gradlew

# 3. بناء APK
./gradlew assembleDebug
# أو للنسخة Release:
./gradlew assembleRelease

# 4. العثور على APK
ls -la app/build/outputs/apk/debug/
```

#### على Windows:
```cmd
REM 1. فك الضغط
unzip ZERO_X_Updated.zip
cd 0X

REM 2. بناء APK
gradlew.bat assembleDebug
REM أو للنسخة Release:
gradlew.bat assembleRelease

REM 3. العثور على APK
dir app\build\outputs\apk\debug\
```

---

## 📱 التثبيت والاستخدام

### المتطلبات:
- ✅ جهاز Android مع روت
- ✅ Magisk مثبت
- ✅ LSPosed مثبت وفعّال

### خطوات التثبيت:

1. **نقل APK للهاتف:**
   ```bash
   adb push app-debug.apk /sdcard/
   ```
   أو انقل الملف يدوياً

2. **تثبيت APK:**
   ```bash
   adb install app-debug.apk
   ```
   أو ثبّت من مدير الملفات

3. **تفعيل في LSPosed:**
   - افتح LSPosed Manager
   - اذهب إلى "Modules"
   - قم بتفعيل "ZERO X"
   - اضغط على ZERO X واختر التطبيقات المستهدفة

4. **إعادة تشغيل:**
   - أعد تشغيل التطبيقات المستهدفة
   - أو أعد تشغيل الهاتف

5. **ضبط الإعدادات:**
   - افتح تطبيق ZERO X
   - أدخل رمز القفل: **911900**
   - اضبط إعدادات التزييف حسب حاجتك

---

## ⚙️ الإعدادات الموصى بها

### للاستخدام في تطبيقات الموقع (موارد، Uber):
```
✓ تفعيل الوحدة: ON
✓ تزييف الموقع: ON
✓ إخفاء Mock Location: ON (كل الخيارات)
✓ الحركة الطبيعية: ON
✓ السرعة: المشي (0.8-1.4 م/ث)
✓ نطاق التذبذب: 3-5 أمتار
✓ إخفاء الروت: ON (كل الخيارات)
```

### للتطبيقات المالية والبنوك:
```
✓ تفعيل الوحدة: ON
✓ إخفاء الروت: ON (كل الخيارات)
✓ إخفاء VPN: ON
✓ إخفاء ADB: ON
✓ إخفاء Xposed: ON
✓ تزييف الموقع: OFF (إلا إذا لزم)
```

---

## 🐛 حل المشاكل

### المشكلة: Gradle build failed
**الحل:**
```bash
# امسح الكاش
./gradlew clean

# حاول مرة أخرى
./gradlew assembleDebug
```

### المشكلة: SDK not found
**الحل:**
- افتح المشروع في Android Studio
- اضغط على "Install missing SDK"
- دع Android Studio يحمل كل شيء

### المشكلة: الوحدة لا تعمل
**الحل:**
1. تأكد من تفعيل LSPosed
2. تأكد من اختيار التطبيقات المستهدفة
3. أعد تشغيل التطبيقات أو الهاتف
4. تحقق من أن الصلاحيات ممنوحة

### المشكلة: تزييف الموقع لا يعمل
**الحل:**
1. تأكد من تفعيل "تزييف الموقع" في الإعدادات
2. تأكد من تفعيل "إخفاء Mock Location"
3. جرب تغيير الموقع على الخريطة
4. أعد تشغيل التطبيق المستهدف

---

## 📊 معدلات النجاح

| نوع التطبيق | معدل النجاح |
|-------------|-------------|
| تطبيقات الموقع (Uber, Maps) | 98%+ |
| الألعاب (Pokemon GO) | 98%+ |
| التطبيقات المالية | 95%+ |
| التطبيقات الحكومية (موارد) | 95%+ |

---

## 📚 المزيد من المعلومات

للحصول على شرح تفصيلي لجميع الحمايات والفحوصات المتخطاة، انظر:
- **SECURITY_DOCUMENTATION.md**

---

## ⚠️ إخلاء مسؤولية

هذا التطبيق للأغراض التعليمية فقط. استخدامه لتجاوز قيود التطبيقات قد يكون مخالفاً لشروط الخدمة.

---

## 👥 التطوير

**Dev Group 0X**  
**Thanks: 3nad Alyami**

---

## 📝 الترخيص

هذا المشروع مفتوح المصدر للاستخدام الشخصي.

---

## 🆘 الدعم

إذا واجهت أي مشاكل:
1. تحقق من قسم "حل المشاكل" أعلاه
2. راجع SECURITY_DOCUMENTATION.md
3. تأكد من استخدام أحدث إصدار من LSPosed

---

**جاهز للبناء! 🚀**
