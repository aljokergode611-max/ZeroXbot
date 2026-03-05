#!/bin/bash
# سكريبت البناء السريع - أمر واحد فقط!
chmod +x gradlew && ./gradlew assembleDebug && echo "✅ تم! APK في: app/build/outputs/apk/debug/app-debug.apk"
