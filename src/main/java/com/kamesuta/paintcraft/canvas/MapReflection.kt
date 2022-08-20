package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.ReflectionAccessor
import org.bukkit.map.MapView

object MapReflection {
    fun getMap(mapView: MapView): ByteArray {
        val colors: ByteArray? = try {
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
        return colors ?: ByteArray(128 * 128)
    }

    fun getMapCache(mapView: MapView): ByteArray {
        val colors: ByteArray? = try {
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
        return colors ?: ByteArray(128 * 128)
    }
}