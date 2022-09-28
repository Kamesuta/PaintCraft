package com.kamesuta.paintcraft.map.image

import com.kamesuta.paintcraft.map.DrawableMapReflection
import org.bukkit.map.MapCanvas

/**
 * BukkitのMapCanvasのラッパー
 * @param canvas BukkitのMapCanvas
 */
class PixelImageMapCanvas private constructor(val canvas: MapCanvas, pixels: ByteArray) : PixelImageMapBuffer(pixels) {
    override fun set(x: Int, y: Int, color: Byte) = canvas.setPixel(x, y, color)
    override fun get(x: Int, y: Int): Byte = canvas.getPixel(x, y)

    companion object {
        /**
         * MapCanvasをラップする
         * @param canvas BukkitのMapCanvas
         * @return ラップしたPixelImageMapCanvas
         */
        fun wrap(canvas: MapCanvas): PixelImageMapCanvas? {
            val buffer = DrawableMapReflection.getCanvasBuffer(canvas)
                ?: return null
            return PixelImageMapCanvas(canvas, buffer)
        }
    }
}