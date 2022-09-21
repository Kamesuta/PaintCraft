package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.map.image.PixelImage
import com.kamesuta.paintcraft.map.image.mapSize
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
 * @param thickness 線の太さ
 */
class DrawRect(
    private val x1: Int,
    private val y1: Int,
    private val x2: Int,
    private val y2: Int,
    private val color: Byte,
    private val fill: Boolean,
    private val thickness: Int,
) : Draw {
    override fun draw(canvas: PixelImage) {
        // 塗りつぶすかどうか
        if (fill) {
            // (x1,y1)が(x2,y2)より左上になるようにする
            val xMin = min(x1, x2).coerceIn(0, mapSize)
            val yMin = min(y1, y2).coerceIn(0, mapSize)
            val xMax = max(x1, x2).coerceIn(-1, mapSize - 1)
            val yMax = max(y1, y2).coerceIn(-1, mapSize - 1)
            // 範囲内のピクセルを塗りつぶし
            for (x in xMin..xMax) {
                for (y in yMin..yMax) {
                    canvas[x, y] = color
                }
            }
        } else {
            // 範囲外の縁を描画しないためにマップのサイズより1大きいサイズにする
            val th = thickness / 2
            val th2 = (thickness + 1) / 2
            // (x1,y1)が(x2,y2)より左上になるようにする
            val xMin = min(x1, x2).coerceIn(-th - 1, th + mapSize + 1)
            val yMin = min(y1, y2).coerceIn(-th - 1, th + mapSize + 1)
            val xMax = max(x1, x2).coerceIn(-th - 2, th + mapSize)
            val yMax = max(y1, y2).coerceIn(-th - 2, th + mapSize)
            // 4辺を描画
            for (x in xMin - th2..xMax + th2) {
                for (y in -th2..th) {
                    canvas[x, yMin + y] = color
                    canvas[x, yMax - y] = color
                }
            }
            for (y in yMin - th2..yMax + th2) {
                for (x in -th2..th) {
                    canvas[xMin + x, y] = color
                    canvas[xMax - x, y] = color
                }
            }
        }
    }
}