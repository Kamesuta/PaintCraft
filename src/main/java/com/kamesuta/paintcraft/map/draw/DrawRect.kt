package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import org.bukkit.map.MapCanvas
import kotlin.math.max
import kotlin.math.min

/**
 * 矩形を描画するクラス
 * @param x1 始点のX座標 (clamp不要)
 * @param y1 始点のY座標 (clamp不要)
 * @param x2 終点のX座標 (clamp不要)
 * @param y2 終点のY座標 (clamp不要)
 * @param color 描画する色
 * @param fill 矩形の内側を塗りつぶすかどうか
 */
class DrawRect(
    private val x1: Int,
    private val y1: Int,
    private val x2: Int,
    private val y2: Int,
    private val color: Byte,
    private val fill: Boolean,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        // (x1,y1)が(x2,y2)より左上になるようにする
        // 範囲外の縁を描画しないためにマップのサイズより1大きいサイズにする
        val xMin = max(-1, min(x1, x2))
        val yMin = max(-1, min(y1, y2))
        val xMax = min(mapSize, max(x1, x2))
        val yMax = min(mapSize, max(y1, y2))

        // 塗りつぶすかどうか
        if (fill) {
            // 範囲内のピクセルを塗りつぶし
            for (x in xMin..xMax) {
                for (y in yMin..yMax) {
                    canvas.setPixel(x, y, color)
                }
            }
        } else {
            // 4辺を描画
            for (x in xMin..xMax) {
                canvas.setPixel(x, yMin, color)
                canvas.setPixel(x, yMax, color)
            }
            for (y in yMin..yMax) {
                canvas.setPixel(xMin, y, color)
                canvas.setPixel(xMax, y, color)
            }
        }
    }
}