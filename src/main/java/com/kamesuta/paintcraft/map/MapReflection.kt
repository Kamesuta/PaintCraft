package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.util.ReflectionAccessor
import com.kamesuta.paintcraft.util.UVInt
import com.kamesuta.paintcraft.util.UVIntArea
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapView

object MapReflection {
    fun getCanvasBuffer(mapCanvas: MapCanvas): MapBuffer? {
        return try {
            ReflectionAccessor.getField(mapCanvas, "buffer") as ByteArray?
        } catch (e: ReflectiveOperationException) {
            PaintCraft.instance.logger.warning("Failed to get MapCanvas buffer")
            null
        }
    }

    fun getMapBuffer(mapView: MapView): MapBuffer? {
        return try {
            val worldMap: Any = ReflectionAccessor.getField(mapView, "worldMap")
                ?: return null
            try {
                ReflectionAccessor.getField(worldMap, "colors") as ByteArray?
            } catch (e: NoSuchFieldException) {
                //Then we must be on 1.17
                ReflectionAccessor.getField(worldMap, "g") as ByteArray?
            }
        } catch (e: ReflectiveOperationException) {
            PaintCraft.instance.logger.warning("Failed to get map buffer")
            null
        }
    }

    fun getMapCacheBuffer(mapView: MapView): MapBuffer? {
        return try {
            val worldMap = ReflectionAccessor.getField(mapView, "renderCache") as HashMap<*, *>?
                ?: return null
            val renderCache = worldMap[null]
                ?: worldMap.values.firstOrNull()
            if (renderCache != null) {
                ReflectionAccessor.getField(renderCache, "buffer") as ByteArray
            } else {
                null
            }
        } catch (e: ReflectiveOperationException) {
            PaintCraft.instance.logger.warning("Failed to get map cache buffer")
            null
        }
    }

    fun sendMap(player: Player, map: MapView, buffer: MapBuffer, area: UVIntArea) {
        try {
            val handle = ReflectionAccessor.invokeMethod(player, "getHandle")
                ?: return
            val connection = ReflectionAccessor.getField(handle, "playerConnection")
                ?: return
            val icons: Collection<Any?> = listOf()
            val packet = ReflectionAccessor.newInstance(
                "PacketPlayOutMap",
                map.id,
                map.scale.value,
                true,
                map.isLocked,
                java.util.Collection::class.java,
                icons,
                buffer,
                area.p1.u,
                area.p1.v,
                area.width,
                area.height,
            ) ?: return
            val packetClass = ReflectionAccessor.forName("Packet")
            ReflectionAccessor.invokeMethod(connection, "sendPacket", packetClass, packet)
        } catch (e: ReflectiveOperationException) {
            PaintCraft.instance.logger.warning("Failed to send map to player ${player.name}")
        }
    }

    fun getMapDirtyArea(player: Player, map: MapView): UVIntArea? {
        return try {
            val entity = ReflectionAccessor.invokeMethod(player, "getHandle")
                ?: return null
            val worldMap = ReflectionAccessor.getField(map, "worldMap")
                ?: return null
            val humanTrackerMap = ReflectionAccessor.getField(worldMap, "humans") as HashMap<*, *>?
                ?: return null
            val humanTracker = humanTrackerMap[entity]
                ?: return null
            val x1 = ReflectionAccessor.getField(humanTracker, "e") as Int
            val y1 = ReflectionAccessor.getField(humanTracker, "f") as Int
            val x2 = ReflectionAccessor.getField(humanTracker, "g") as Int
            val y2 = ReflectionAccessor.getField(humanTracker, "h") as Int
            UVIntArea(
                UVInt(x1, y1),
                UVInt(x2, y2),
            )
        } catch (e: ReflectiveOperationException) {
            PaintCraft.instance.logger.warning("Failed to get map dirty area for player ${player.name}")
            null
        }
    }
}
