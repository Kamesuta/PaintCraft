package com.kamesuta.paintcraft.map.draw

import org.bukkit.map.MapCanvas
import kotlin.math.max
import kotlin.math.min

/**
 * 矩形を描画するクラス
 * @param x1 始点のX座標
 * @param y1 始点のY座標
 * @param x2 終点のX座標
 * @param y2 終点のY座標
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
        val x1 = min(x1, x2)
        val y1 = min(y1, y2)
        val x2 = max(x1, x2)
        val y2 = max(y1, y2)

        // 塗りつぶすかどうか
        if (fill) {
            // 範囲内のピクセルを塗りつぶし
            for (x in x1..x2) {
                for (y in y1..y2) {
                    canvas.setPixel(x, y, color)
                }
            }
        } else {
            // 4辺を描画
            for (x in x1..x2) {
                canvas.setPixel(x, y1, color)
                canvas.setPixel(x, y2, color)
            }
            for (y in y1..y2) {
                canvas.setPixel(x1, y, color)
                canvas.setPixel(x2, y, color)
            }
        }
    }
}