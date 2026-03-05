#!/bin/bash

# 🛡️ ZERO X - سكريبت البناء التلقائي لنظام macOS/Linux
# ================================================

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  🛡️  ZERO X - بناء التطبيق تلقائياً"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# التحقق من وجود Java
if ! command -v java &> /dev/null; then
    echo "❌ خطأ: Java غير مثبت!"
    echo "📥 قم بتثبيت JDK 17 أو أحدث من:"
    echo "   https://www.oracle.com/java/technologies/downloads/"
    echo "   أو استخدم: brew install openjdk@17"
    exit 1
fi

echo "✅ Java موجود: $(java -version 2>&1 | head -n 1)"
echo ""

# منح صلاحيات التنفيذ لـ gradlew
echo "🔧 منح صلاحيات التنفيذ..."
chmod +x gradlew
echo "✅ تم"
echo ""

# تنظيف البناء السابق (اختياري)
echo "🧹 تنظيف البناء السابق..."
./gradlew clean
echo "✅ تم"
echo ""

# بناء APK
echo "🔨 بناء APK..."
echo "⏳ قد يستغرق 2-5 دقائق في المرة الأولى..."
echo ""
./gradlew assembleDebug

# التحقق من نجاح البناء
if [ $? -eq 0 ]; then
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  ✅ تم بناء التطبيق بنجاح!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "📱 مسار APK:"
    echo "   $(pwd)/app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    
    # عرض حجم الملف
    APK_FILE="app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK_FILE" ]; then
        APK_SIZE=$(du -h "$APK_FILE" | cut -f1)
        echo "📦 حجم الملف: $APK_SIZE"
        echo ""
        
        # خيارات إضافية
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "📲 خيارات التثبيت:"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo ""
        echo "1️⃣  نسخ APK إلى سطح المكتب:"
        echo "   cp $APK_FILE ~/Desktop/ZERO-X.apk"
        echo ""
        echo "2️⃣  تثبيت مباشرة عبر ADB:"
        echo "   adb install $APK_FILE"
        echo ""
        echo "3️⃣  فتح مجلد APK:"
        echo "   open app/build/outputs/apk/debug/"
        echo ""
    fi
else
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "  ❌ فشل البناء!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "💡 جرب:"
    echo "   1. تأكد من تثبيت JDK 17+"
    echo "   2. تأكد من الاتصال بالإنترنت (لتحميل التبعيات)"
    echo "   3. جرب: ./gradlew clean assembleDebug"
    echo ""
    exit 1
fi
