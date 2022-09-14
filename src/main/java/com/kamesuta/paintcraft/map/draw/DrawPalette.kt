package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.util.vec.Vec2d
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapPalette
import java.awt.Color
import kotlin.math.atan2

/**
 * カラーピッカーを描画するクラス
 * @param x クリックしたX座標
 * @param y クリックしたY座標
 */
class DrawPalette(
    private val x: Int,
    private val y: Int,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        val radiusSatBri = 0.4
        val radiusHue = 0.6
        val vec = Vec2d(x.toDouble(), y.toDouble()) / (mapSize.toDouble() / 2.0) - Vec2d(1.0, 1.0)
        for (iy in 0 until mapSize) {
            for (ix in 0 until mapSize) {
                // -1.0 ~ 1.0
                val iVec = Vec2d(ix.toDouble(), iy.toDouble()) / (mapSize.toDouble() / 2.0) - Vec2d(1.0, 1.0)
                when (iVec.length) {
                    in 0.0..radiusSatBri -> {
                        val hue = atan2(vec.y, vec.x) / (Math.PI * 2.0)
                        val sat = ((iVec.x / radiusSatBri * 1.414 + 1.0) / 2.0).coerceIn(0.0, 1.0)
                        val bri = ((iVec.y / radiusSatBri * 1.414 + 1.0) / 2.0).coerceIn(0.0, 1.0)
                        val color = Color.getHSBColor(hue.toFloat(), sat.toFloat(), bri.toFloat())
                        canvas.setPixel(ix, iy, MapPalette.matchColor(color))
                    }
                    in radiusSatBri..radiusHue -> {
                        val hue = atan2(iVec.y, iVec.x) / (Math.PI * 2.0)
                        val color = Color.getHSBColor(hue.toFloat(), 1.0f, 1.0f)
                        canvas.setPixel(ix, iy, MapPalette.matchColor(color))
                    }
                    else -> {
                        canvas.setPixel(ix, iy, MapPalette.matchColor(255, 255, 0))
                    }
                }
            }
        }
    }
}