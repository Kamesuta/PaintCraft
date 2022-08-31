package com.kamesuta.paintcraft.map.draw

import org.bukkit.map.MapCanvas
import kotlin.math.abs

/**
 * 線を描画するクラス
 * @param x1 始点のX座標
 * @param y1 始点のY座標
 * @param x2 終点のX座標
 * @param y2 終点のY座標
 * @param color 描画する色
 */
class DrawLine(
    private val x1: Int,
    private val y1: Int,
    private val x2: Int,
    private val y2: Int,
    private val color: Byte,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        // 始点から終点までの幅と高さを取得
        val w: Int = x2 - x1
        val h: Int = y2 - y1

        // 基準点
        var dx1 = 0
        var dy1 = 0
        var dx2 = 0
        var dy2 = 0

        // 伸ばしていく方向
        if (w != 0) {
            dx1 = if (w > 0) 1 else -1
            dx2 = if (w > 0) 1 else -1
        }
        if (h != 0) {
            dy1 = if (h > 0) 1 else -1
        }

        // 最大、最小距離
        var longest = abs(w)
        var shortest = abs(h)

        // 最大、最小距離が w>h になるように、最大距離と最小距離を入れ替える
        if (longest <= shortest) {
            longest = abs(h)
            shortest = abs(w)
            if (h < 0) {
                dy2 = -1
            } else if (h > 0) {
                dy2 = 1
            }
            dx2 = 0
        }
        var numerator = longest shr 1

        // 1ピクセルずつずらしながら描画
        var x = x1
        var y = y1
        for (i in 0..longest) {
            canvas.setPixel(x, y, color)
            numerator += shortest
            if (numerator >= longest) {
                numerator -= longest
                x += dx1
                y += dy1
            } else {
                x += dx2
                y += dy2
            }
        }
    }
}