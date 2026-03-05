package com.ox.stealth.hooks

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import android.os.SystemClock
import kotlin.math.*
import kotlin.random.Random
import java.util.Random as JavaRandom

/**
 * 0X Natural Movement Engine - محرك الحركة الطبيعية
 *
 * يحاكي حركة إنسان حقيقي بدلاً من القفزات:
 * - Jitter: تذبذب عشوائي صغير (1-5 أمتار) لمحاكاة عدم دقة GPS الطبيعية
 * - تسارع وتباطؤ تدريجي عند بدء وإيقاف الحركة
 * - تغيير اتجاه سلس (ليس حاد)
 * - محاكاة بيانات المستشعرات (accelerometer, gyroscope) لتتوافق مع الحركة
 * - سرعات واقعية (مشي: 1.2-1.8 م/ث، جري: 2.5-4 م/ث، قيادة: 8-30 م/ث)
 *
 * مستوحى من MotionEmulator + Lockito + Geergit
 */
object NaturalMovement {

    // ثوابت الحركة - أبطأ وأكثر واقعية
    private const val EARTH_RADIUS = 6371000.0 // متر
    private const val WALK_SPEED_MIN = 0.8f     // م/ث (بطيء)
    private const val WALK_SPEED_MAX = 1.4f     // م/ث (متوسط)
    private const val RUN_SPEED_MIN = 2.0f      // م/ث
    private const val RUN_SPEED_MAX = 3.5f      // م/ث
    private const val DRIVE_SPEED_MIN = 5.0f    // م/ث
    private const val DRIVE_SPEED_MAX = 20.0f   // م/ث

    // حالة الحركة
    private var isMoving = false
    private var currentSpeed = 0f
    private var targetSpeed = 0f
    private var currentBearing = 0f
    private var targetBearing = 0f
    private var currentLat = 0.0
    private var currentLng = 0.0
    private var jitterRadius = 3.0 // أمتار
    private var movementMode = MovementMode.WALK
    private var lastUpdateTime = 0L
    private var accelerationRate = 0.3f // م/ث²
    private var decelerationRate = 0.5f // م/ث²

    // مسار محدد مسبقاً
    private var routePoints: MutableList<Pair<Double, Double>> = mutableListOf()
    private var currentRouteIndex = 0

    enum class MovementMode {
        WALK, RUN, DRIVE, CUSTOM
    }

    fun hook(lpparam: XC_LoadPackage.LoadPackageParam, prefs: XSharedPreferences?) {
        loadSettings(prefs)
        hookSensors(lpparam)
        // تحميل الإحداثيات الأولية من LocationHook
        initializeFromLocationHook(prefs)
        startMovementEngine()
    }

    private fun loadSettings(prefs: XSharedPreferences?) {
        prefs?.reload()
        jitterRadius = prefs?.getFloat("jitter_radius", 3.0f)?.toDouble() ?: 3.0
        val modeStr = prefs?.getString("movement_mode", "WALK") ?: "WALK"
        movementMode = try { MovementMode.valueOf(modeStr) } catch (_: Exception) { MovementMode.WALK }
    }
    
    private fun initializeFromLocationHook(prefs: XSharedPreferences?) {
        prefs?.reload()
        val latStr = prefs?.getString("spoof_lat", "24.7136") ?: "24.7136"
        val lngStr = prefs?.getString("spoof_lng", "46.6753") ?: "46.6753"
        
        currentLat = latStr.toDoubleOrNull() ?: 24.7136
        currentLng = lngStr.toDoubleOrNull() ?: 46.6753
        
        MainHook.log("🚶 Natural Movement initialized at: $currentLat, $currentLng")
    }

    /**
     * محرك الحركة - يحدث الموقع بشكل مستمر
     */
    private fun startMovementEngine() {
        Thread {
            while (true) {
                try {
                    val now = System.currentTimeMillis()
                    val deltaTime = if (lastUpdateTime > 0) (now - lastUpdateTime) / 1000.0f else 0f
                    lastUpdateTime = now

                    if (deltaTime > 0 && isMoving) {
                        // === تحديث السرعة بتسارع/تباطؤ طبيعي ===
                        updateSpeed(deltaTime)

                        // === تحديث الاتجاه بسلاسة ===
                        updateBearing(deltaTime)

                        // === تحديث الموقع ===
                        if (currentSpeed > 0.01f) {
                            moveForward(deltaTime)
                        }

                        // === إضافة Jitter طبيعي ===
                        val jitteredLat = addJitter(currentLat, true)
                        val jitteredLng = addJitter(currentLng, false)

                        // === تحديث LocationHook بالموقع الجديد ===
                        LocationHook.updateLocation(jitteredLat, jitteredLng)
                        LocationHook.updateMovement(currentSpeed, currentBearing)
                    } else if (!isMoving) {
                        // حتى بدون حركة، نضيف Jitter صغير جداً لمحاكاة GPS الطبيعي
                        val microJitterLat = currentLat + (Math.random() - 0.5) * 0.00001 // ~1 متر
                        val microJitterLng = currentLng + (Math.random() - 0.5) * 0.00001
                        LocationHook.updateLocation(microJitterLat, microJitterLng)
                        LocationHook.updateMovement(0f, currentBearing)
                    }

                    // تحديث كل 2-4 ثانية (أبطأ وأكثر واقعية)
                    Thread.sleep(2000 + Random.nextLong(2000))
                } catch (_: Exception) {
                    Thread.sleep(2000)
                }
            }
        }.apply {
            isDaemon = true
            name = "0X-Movement"
            priority = Thread.MIN_PRIORITY // أولوية منخفضة لتجنب الاكتشاف
            start()
        }
    }

    /**
     * تحديث السرعة بتسارع/تباطؤ تدريجي طبيعي
     */
    private fun updateSpeed(deltaTime: Float) {
        if (!isMoving) {
            targetSpeed = 0f
        } else {
            // تحديد السرعة المستهدفة حسب وضع الحركة مع تغيير عشوائي
            targetSpeed = when (movementMode) {
                MovementMode.WALK -> Random.nextFloat() * (WALK_SPEED_MAX - WALK_SPEED_MIN) + WALK_SPEED_MIN
                MovementMode.RUN -> Random.nextFloat() * (RUN_SPEED_MAX - RUN_SPEED_MIN) + RUN_SPEED_MIN
                MovementMode.DRIVE -> Random.nextFloat() * (DRIVE_SPEED_MAX - DRIVE_SPEED_MIN) + DRIVE_SPEED_MIN
                MovementMode.CUSTOM -> targetSpeed
            }
        }

        // تسارع/تباطؤ تدريجي
        val diff = targetSpeed - currentSpeed
        if (diff > 0) {
            currentSpeed += min(accelerationRate * deltaTime, diff)
        } else if (diff < 0) {
            currentSpeed += max(-decelerationRate * deltaTime, diff)
        }

        // إضافة تذبذب صغير في السرعة (طبيعي)
        currentSpeed += (Random.nextFloat() - 0.5f) * 0.1f
        currentSpeed = max(0f, currentSpeed)
    }

    /**
     * تحديث الاتجاه بسلاسة (بدون تغيير حاد)
     */
    private fun updateBearing(deltaTime: Float) {
        if (routePoints.isNotEmpty() && currentRouteIndex < routePoints.size) {
            // حساب الاتجاه نحو النقطة التالية
            val target = routePoints[currentRouteIndex]
            targetBearing = calculateBearing(currentLat, currentLng, target.first, target.second)

            // التحقق من الوصول للنقطة
            val distance = calculateDistance(currentLat, currentLng, target.first, target.second)
            if (distance < 5.0) { // 5 أمتار
                currentRouteIndex++
                if (currentRouteIndex >= routePoints.size) {
                    currentRouteIndex = 0 // تكرار المسار
                }
            }
        }

        // تدوير سلس نحو الاتجاه المستهدف
        var diff = targetBearing - currentBearing
        // تطبيع الفرق بين -180 و 180
        while (diff > 180) diff -= 360
        while (diff < -180) diff += 360

        val maxTurnRate = when (movementMode) {
            MovementMode.WALK -> 45f  // درجة/ثانية
            MovementMode.RUN -> 30f
            MovementMode.DRIVE -> 15f
            MovementMode.CUSTOM -> 45f
        }

        val turnAmount = diff.coerceIn(-maxTurnRate * deltaTime, maxTurnRate * deltaTime)
        currentBearing += turnAmount

        // تطبيع الاتجاه بين 0 و 360
        currentBearing = ((currentBearing % 360) + 360) % 360

        // إضافة تذبذب صغير في الاتجاه
        currentBearing += (Random.nextFloat() - 0.5f) * 2f
    }

    /**
     * تحريك الموقع للأمام بناءً على السرعة والاتجاه
     */
    private fun moveForward(deltaTime: Float) {
        val distance = currentSpeed * deltaTime // المسافة بالأمتار

        val bearingRad = Math.toRadians(currentBearing.toDouble())
        val latRad = Math.toRadians(currentLat)

        val newLat = currentLat + (distance / EARTH_RADIUS) * cos(bearingRad) * (180 / PI)
        val newLng = currentLng + (distance / (EARTH_RADIUS * cos(latRad))) * sin(bearingRad) * (180 / PI)

        currentLat = newLat
        currentLng = newLng
    }

    /**
     * إضافة Jitter طبيعي - تذبذب عشوائي صغير يحاكي عدم دقة GPS
     */
    private fun addJitter(value: Double, isLatitude: Boolean): Double {
        // Gaussian distribution للتذبذب (أكثر طبيعية من uniform)
        val gaussian = JavaRandom().nextGaussian()
        val jitter = gaussian * (jitterRadius / EARTH_RADIUS) * (180 / PI)
        return value + jitter.toDouble()
    }

    /**
     * حساب المسافة بين نقطتين (Haversine)
     */
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return 2 * EARTH_RADIUS * asin(sqrt(a))
    }

    /**
     * حساب الاتجاه بين نقطتين
     */
    private fun calculateBearing(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val dLng = Math.toRadians(lng2 - lng1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val y = sin(dLng) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(dLng)

        return ((Math.toDegrees(atan2(y, x)) + 360) % 360).toFloat()
    }

    /**
     * Hook المستشعرات لمحاكاة بيانات متوافقة مع الحركة
     * هذا يمنع التطبيقات من كشف أن الجهاز لا يتحرك فعلاً
     */
    private fun hookSensors(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            // Hook SensorEventListener.onSensorChanged
            XposedHelpers.findAndHookMethod(
                "android.hardware.SensorManager", lpparam.classLoader,
                "registerListener",
                SensorEventListener::class.java, Sensor::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val sensor = param.args[1] as? Sensor ?: return
                        val listener = param.args[0] as? SensorEventListener ?: return

                        when (sensor.type) {
                            Sensor.TYPE_ACCELEROMETER -> {
                                // محاكاة تسارع متوافق مع الحركة
                                spoofAccelerometer(listener, sensor)
                            }
                            Sensor.TYPE_GYROSCOPE -> {
                                // محاكاة دوران متوافق مع تغيير الاتجاه
                                spoofGyroscope(listener, sensor)
                            }
                            Sensor.TYPE_STEP_COUNTER -> {
                                // محاكاة عداد الخطوات
                                spoofStepCounter(listener, sensor)
                            }
                        }
                    }
                }
            )
        } catch (_: Exception) {}
    }

    private fun spoofAccelerometer(listener: SensorEventListener, sensor: Sensor) {
        Thread {
            while (isMoving) {
                try {
                    val event = createSensorEvent(sensor)
                    val values = floatArrayOf(
                        // X: تسارع جانبي صغير
                        (Random.nextFloat() - 0.5f) * 0.5f,
                        // Y: تسارع أمامي يتناسب مع السرعة
                        currentSpeed * 0.1f + (Random.nextFloat() - 0.5f) * 0.3f,
                        // Z: الجاذبية + اهتزاز المشي
                        9.81f + sin(System.currentTimeMillis() / 300.0).toFloat() * 0.5f
                    )
                    XposedHelpers.setObjectField(event, "values", values)
                    listener.onSensorChanged(event)
                    Thread.sleep(50) // 20 Hz
                } catch (_: Exception) { break }
            }
        }.apply { isDaemon = true; start() }
    }

    private fun spoofGyroscope(listener: SensorEventListener, sensor: Sensor) {
        Thread {
            while (isMoving) {
                try {
                    val event = createSensorEvent(sensor)
                    val bearingChange = (targetBearing - currentBearing) * 0.01f
                    val values = floatArrayOf(
                        (Random.nextFloat() - 0.5f) * 0.02f,
                        (Random.nextFloat() - 0.5f) * 0.02f,
                        bearingChange + (Random.nextFloat() - 0.5f) * 0.01f
                    )
                    XposedHelpers.setObjectField(event, "values", values)
                    listener.onSensorChanged(event)
                    Thread.sleep(50)
                } catch (_: Exception) { break }
            }
        }.apply { isDaemon = true; start() }
    }

    private fun spoofStepCounter(listener: SensorEventListener, sensor: Sensor) {
        Thread {
            var steps = 0f
            while (isMoving && movementMode != MovementMode.DRIVE) {
                try {
                    val event = createSensorEvent(sensor)
                    steps += if (movementMode == MovementMode.RUN) 2.5f else 1.5f
                    XposedHelpers.setObjectField(event, "values", floatArrayOf(steps))
                    listener.onSensorChanged(event)
                    val interval = if (movementMode == MovementMode.RUN) 400L else 600L
                    Thread.sleep(interval + Random.nextLong(200))
                } catch (_: Exception) { break }
            }
        }.apply { isDaemon = true; start() }
    }

    private fun createSensorEvent(sensor: Sensor): SensorEvent {
        val constructor = SensorEvent::class.java.getDeclaredConstructor(Int::class.javaPrimitiveType)
        constructor.isAccessible = true
        val event = constructor.newInstance(3)
        XposedHelpers.setObjectField(event, "sensor", sensor)
        XposedHelpers.setObjectField(event, "timestamp", SystemClock.elapsedRealtimeNanos())
        XposedHelpers.setObjectField(event, "accuracy", SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
        return event
    }

    // === واجهة التحكم من UI ===

    fun startMoving(mode: MovementMode) {
        movementMode = mode
        isMoving = true
    }

    fun stopMoving() {
        isMoving = false
    }

    fun setRoute(points: List<Pair<Double, Double>>) {
        routePoints = points.toMutableList()
        currentRouteIndex = 0
    }

    fun setPosition(lat: Double, lng: Double) {
        currentLat = lat
        currentLng = lng
    }

    fun setJitterRadius(radius: Double) {
        jitterRadius = radius
    }
}
