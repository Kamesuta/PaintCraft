package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.ReflectionAccessor
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapView

object MapReflection {
    fun getCanvasBuffer(mapCanvas: MapCanvas): MapBuffer? {
        return try {
            ReflectionAccessor.getField(mapCanvas, "buffer") as ByteArray?
        } catch (e: ReflectiveOperationException) {
            null
        }
    }

    fun getMapBuffer(mapView: MapView): MapBuffer? {
        return try {
            val worldMap: Any = ReflectionAccessor.getField(mapView, "worldMap")
            try {
                ReflectionAccessor.getField(worldMap, "colors") as ByteArray?
            } catch (e: NoSuchFieldException) {
                //Then we must be on 1.17
                ReflectionAccessor.getField(worldMap, "g") as ByteArray?
            }
        } catch (e: ReflectiveOperationException) {
            null
        }
    }

    fun getMapCacheBuffer(mapView: MapView): MapBuffer? {
        return try {
            val worldMap = ReflectionAccessor.getField(mapView, "renderCache") as HashMap<*, *>
            val renderCache = worldMap[null]
                ?: worldMap.values.firstOrNull()
            if (renderCache != null) {
                ReflectionAccessor.getField(renderCache, "buffer") as ByteArray
            } else {
                null
            }
        } catch (e: ReflectiveOperationException) {
            null
        }
    }
}
