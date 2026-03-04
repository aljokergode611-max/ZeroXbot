package com.ox.stealth.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.view.MotionEvent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.ScaleBarOverlay

/**
 * Zero X - مكون الخريطة الاحترافية
 *
 * خريطة متعددة الطبقات مع:
 * - عرض عادي (OpenStreetMap)
 * - أقمار صناعية (Google/ESRI Satellite)
 * - تضاريس (OpenTopoMap)
 * - مسارات (CyclOSM / Hiking)
 * - هجين (أقمار صناعية + أسماء الشوارع)
 * - وضع مظلم (CartoDB Dark Matter)
 */

// ===================== مصادر الخرائط =====================

/**
 * Google Satellite - أقمار صناعية عالية الجودة
 */
val GOOGLE_SATELLITE = object : OnlineTileSourceBase(
    "GoogleSat", 0, 20, 256, ".png",
    arrayOf("https://mt0.google.com", "https://mt1.google.com", "https://mt2.google.com", "https://mt3.google.com")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${baseUrl}/vt/lyrs=s&x=$x&y=$y&z=$zoom"
    }
}

/**
 * Google Hybrid - أقمار صناعية + أسماء الشوارع
 */
val GOOGLE_HYBRID = object : OnlineTileSourceBase(
    "GoogleHybrid", 0, 20, 256, ".png",
    arrayOf("https://mt0.google.com", "https://mt1.google.com", "https://mt2.google.com", "https://mt3.google.com")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${baseUrl}/vt/lyrs=y&x=$x&y=$y&z=$zoom"
    }
}

/**
 * Google Terrain - تضاريس
 */
val GOOGLE_TERRAIN = object : OnlineTileSourceBase(
    "GoogleTerrain", 0, 20, 256, ".png",
    arrayOf("https://mt0.google.com", "https://mt1.google.com", "https://mt2.google.com", "https://mt3.google.com")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${baseUrl}/vt/lyrs=p&x=$x&y=$y&z=$zoom"
    }
}

/**
 * Google Roads - خريطة الطرق العادية
 */
val GOOGLE_ROADS = object : OnlineTileSourceBase(
    "GoogleRoads", 0, 20, 256, ".png",
    arrayOf("https://mt0.google.com", "https://mt1.google.com", "https://mt2.google.com", "https://mt3.google.com")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${baseUrl}/vt/lyrs=m&x=$x&y=$y&z=$zoom"
    }
}

/**
 * ESRI World Imagery - أقمار صناعية عالية الدقة (بديل)
 */
val ESRI_SATELLITE = object : OnlineTileSourceBase(
    "ESRISat", 0, 19, 256, ".jpg",
    arrayOf("https://server.arcgisonline.com")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${baseUrl}/ArcGIS/rest/services/World_Imagery/MapServer/tile/$zoom/$y/$x"
    }
}

/**
 * OpenTopoMap - خريطة تضاريس مفصلة
 */
val OPEN_TOPO = object : OnlineTileSourceBase(
    "OpenTopo", 0, 17, 256, ".png",
    arrayOf("https://a.tile.opentopomap.org", "https://b.tile.opentopomap.org", "https://c.tile.opentopomap.org")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${baseUrl}/$zoom/$x/$y.png"
    }
}

/**
 * CyclOSM - خريطة مسارات الدراجات والمشي
 */
val CYCLOSM = object : OnlineTileSourceBase(
    "CyclOSM", 0, 19, 256, ".png",
    arrayOf("https://a.tile-cyclosm.openstreetmap.fr", "https://b.tile-cyclosm.openstreetmap.fr", "https://c.tile-cyclosm.openstreetmap.fr")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${baseUrl}/cyclosm/$zoom/$x/$y.png"
    }
}

/**
 * CartoDB Dark Matter - وضع مظلم (يناسب تصميم Zero X)
 */
val CARTO_DARK = object : OnlineTileSourceBase(
    "CartoDark", 0, 20, 256, ".png",
    arrayOf("https://a.basemaps.cartocdn.com", "https://b.basemaps.cartocdn.com", "https://c.basemaps.cartocdn.com")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${baseUrl}/dark_all/$zoom/$x/$y@2x.png"
    }
}

// ===================== أنواع الخرائط =====================

enum class MapLayer(val title: String, val icon: String) {
    ROADS("عادي", "🗺️"),
    SATELLITE("أقمار صناعية", "🛰️"),
    HYBRID("هجين", "🌍"),
}

fun getMapTileSource(layer: MapLayer): OnlineTileSourceBase {
    return when (layer) {
        MapLayer.ROADS -> GOOGLE_ROADS
        MapLayer.SATELLITE -> GOOGLE_SATELLITE
        MapLayer.HYBRID -> GOOGLE_HYBRID
    }
}

// ===================== مكون الخريطة =====================

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Double = 16.0,
    mapLayer: MapLayer = MapLayer.ROADS,
    routePoints: List<Pair<Double, Double>> = emptyList(),
    accuracyMeters: Float = 10f,
    onLocationSelected: (Double, Double) -> Unit
) {
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var markerRef by remember { mutableStateOf<Marker?>(null) }
    var routeOverlay by remember { mutableStateOf<Polyline?>(null) }

    // تحديث طبقة الخريطة
    LaunchedEffect(mapLayer) {
        mapViewRef?.let { map ->
            map.setTileSource(getMapTileSource(mapLayer))
            map.invalidate()
        }
    }

    // تحديث الموقع
    LaunchedEffect(latitude, longitude) {
        mapViewRef?.let { map ->
            val point = GeoPoint(latitude, longitude)
            map.controller.animateTo(point)

            markerRef?.let { marker ->
                marker.position = point
                marker.snippet = "${"%.6f".format(latitude)}, ${"%.6f".format(longitude)}"
            }
            map.invalidate()
        }
    }

    // تحديث المسار
    LaunchedEffect(routePoints) {
        mapViewRef?.let { map ->
            // حذف المسار القديم
            routeOverlay?.let { map.overlays.remove(it) }

            if (routePoints.size >= 2) {
                val polyline = Polyline().apply {
                    outlinePaint.color = Color.argb(200, 0, 255, 65) // أخضر Zero X
                    outlinePaint.strokeWidth = 6f
                    outlinePaint.isAntiAlias = true
                    outlinePaint.strokeCap = Paint.Cap.ROUND
                    outlinePaint.strokeJoin = Paint.Join.ROUND

                    val geoPoints = routePoints.map { GeoPoint(it.first, it.second) }
                    setPoints(geoPoints)
                }
                map.overlays.add(polyline)
                routeOverlay = polyline
            }
            map.invalidate()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            // إعداد OSMDroid
            Configuration.getInstance().apply {
                userAgentValue = "ZeroX/1.0"
                osmdroidTileCache = context.cacheDir
                // تحسين الأداء
                tileDownloadThreads = 4
                tileFileSystemThreads = 4
                tileDownloadMaxQueueSize = 40
                tileFileSystemMaxQueueSize = 40
            }

            MapView(context).apply {
                // إعداد مصدر الخريطة
                setTileSource(getMapTileSource(mapLayer))
                setMultiTouchControls(true)
                setBuiltInZoomControls(false)

                // إعدادات الأداء
                isTilesScaledToDpi = true
                isHorizontalMapRepetitionEnabled = false
                isVerticalMapRepetitionEnabled = false

                // إعداد الموقع الأولي
                controller.setZoom(zoom)
                controller.setCenter(GeoPoint(latitude, longitude))

                // === إضافة شريط المقياس ===
                val scaleBar = ScaleBarOverlay(this).apply {
                    setCentred(true)
                    setScaleBarOffset(
                        context.resources.displayMetrics.widthPixels / 2,
                        10
                    )
                    // تخصيص ألوان شريط المقياس
                    val textPaint = Paint().apply {
                        color = Color.argb(200, 0, 255, 65)
                        textSize = 28f
                        isAntiAlias = true
                    }
                    val barPaint = Paint().apply {
                        color = Color.argb(200, 0, 255, 65)
                        strokeWidth = 4f
                        isAntiAlias = true
                    }
                    setTextPaint(textPaint)
                    setBarPaint(barPaint)
                    setBackgroundPaint(Paint().apply {
                        color = Color.argb(100, 0, 0, 0)
                    })
                }
                overlays.add(scaleBar)

                // === إضافة البوصلة ===
                val compassOverlay = CompassOverlay(context, this).apply {
                    enableCompass()
                }
                overlays.add(compassOverlay)

                // === إضافة دعم الدوران باللمس ===
                val rotationOverlay = RotationGestureOverlay(this)
                overlays.add(rotationOverlay)

                // === إضافة Marker الموقع المزيّف ===
                val spoofMarker = Marker(this).apply {
                    position = GeoPoint(latitude, longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "📍 الموقع المزيّف"
                    snippet = "${"%.6f".format(latitude)}, ${"%.6f".format(longitude)}"
                    subDescription = "Zero X GPS Spoof"
                }
                overlays.add(spoofMarker)
                markerRef = spoofMarker

                // === دائرة الدقة حول الموقع ===
                val accuracyOverlay = object : Overlay() {
                    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
                        if (shadow) return
                        val markerPos = spoofMarker.position ?: return
                        val projection = mapView.projection
                        val screenPoint = Point()
                        projection.toPixels(markerPos, screenPoint)

                        // حساب نصف القطر بالبكسل
                        val metersPerPixel = 156543.03392 * Math.cos(
                            Math.toRadians(markerPos.latitude)
                        ) / Math.pow(2.0, mapView.zoomLevelDouble)
                        val radiusPixels = (accuracyMeters / metersPerPixel).toFloat()

                        // رسم دائرة الدقة
                        val fillPaint = Paint().apply {
                            color = Color.argb(30, 0, 255, 65)
                            style = Paint.Style.FILL
                            isAntiAlias = true
                        }
                        canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), radiusPixels, fillPaint)

                        val strokePaint = Paint().apply {
                            color = Color.argb(100, 0, 255, 65)
                            style = Paint.Style.STROKE
                            strokeWidth = 2f
                            isAntiAlias = true
                        }
                        canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), radiusPixels, strokePaint)

                        // نقطة مركزية متوهجة
                        val centerPaint = Paint().apply {
                            color = Color.argb(255, 0, 255, 65)
                            style = Paint.Style.FILL
                            isAntiAlias = true
                        }
                        canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), 6f, centerPaint)

                        val glowPaint = Paint().apply {
                            color = Color.argb(80, 0, 255, 65)
                            style = Paint.Style.FILL
                            isAntiAlias = true
                        }
                        canvas.drawCircle(screenPoint.x.toFloat(), screenPoint.y.toFloat(), 12f, glowPaint)
                    }
                }
                overlays.add(accuracyOverlay)

                // === النقر على الخريطة لاختيار الموقع ===
                val tapOverlay = object : Overlay() {
                    override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                        val projection = mapView.projection
                        val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint

                        // تحديث Marker
                        spoofMarker.position = geoPoint
                        spoofMarker.snippet = "${"%.6f".format(geoPoint.latitude)}, ${"%.6f".format(geoPoint.longitude)}"
                        mapView.invalidate()

                        // إرسال الموقع الجديد
                        onLocationSelected(geoPoint.latitude, geoPoint.longitude)
                        return true
                    }

                    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {}
                }
                overlays.add(0, tapOverlay)

                mapViewRef = this
            }
        },
        update = { map ->
            mapViewRef = map
        }
    )
}
