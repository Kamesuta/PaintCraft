package com.kamesuta.paintcraft.map.draw

import org.bukkit.map.MapCanvas
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * 線を描画するクラス
 * @param x0 始点のX座標
 * @param y0 始点のY座標
 * @param x1 終点のX座標
 * @param y1 終点のY座標
 * @param color 描画する色
 * @param thickness 線の太さ
 */
class DrawLine(
    private val x0: Int,
    private val y0: Int,
    private val x1: Int,
    private val y1: Int,
    private val color: Byte,
    private val thickness: Int,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        // 処理の解像度
        val resolution = 255
        // 線の太さ
        val th = resolution * (thickness - 1)

        // 伸ばしていく方向
        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1
        // 始点から終点までの幅と高さを取得
        val dx0 = abs(x1 - x0)
        val dy0 = abs(y1 - y0)

        // 線の長さ
        var e2 = sqrt((dx0 * dx0 + dy0 * dy0).toDouble())

        // 太さが1の場合は、線を描画するだけ
        if (th <= 1 || e2 == 0.0) {
            plotLine(canvas)
            return
        }

        // 縦と横の差
        val dx = dx0 * resolution / e2
        val dy = dy0 * resolution / e2

        if (dx < dy) {
            // 急な線の場合
            // 開始オフセット
            var x1 = ((e2 + th / 2) / dy).roundToInt()
            // 誤差をオフセット幅までずらす
            var err = x1 * dy - th / 2
            var x0 = x0 - x1 * sx
            var y0 = y0
            while (true) {
                // 開始点寄りの縁のピクセル (今回はアンチエイリアスしないため塗りつぶす)
                canvas.setPixel(x0.also { x1 = it }, y0, color)
                e2 = dy - err - th
                while (e2 + dy < resolution) {
                    // 線上のピクセル
                    canvas.setPixel(sx.let { x1 += it; x1 }, y0, color)
                    e2 += dy
                }
                // 終了点寄りの縁のピクセル (今回はアンチエイリアスしないため塗りつぶす)
                canvas.setPixel(x1 + sx, y0, color)
                if (y0 == y1) break
                // Yを1つ進める
                err += dx
                if (err > resolution) {
                    // Xを1つ進める
                    err -= dy
                    x0 += sx
                }
                y0 += sy
            }
        } else {
            // 平らな線の場合
            // 開始オフセット
            var y1 = ((e2 + th / 2) / dx).roundToInt()
            // 誤差をオフセット幅までずらす
            var err = y1 * dx - th / 2
            var x0 = x0
            var y0 = y0 - y1 * sy
            while (true) {
                // 開始点寄りの縁のピクセル (今回はアンチエイリアスしないため塗りつぶす)
                canvas.setPixel(x0, y0.also { y1 = it }, color)
                e2 = dx - err - th
                while (e2 + dx < resolution) {
                    // 線上のピクセル
                    canvas.setPixel(x0, sy.let { y1 += it; y1 }, color)
                    e2 += dx
                }
                // 終了点寄りの縁のピクセル (今回はアンチエイリアスしないため塗りつぶす)
                canvas.setPixel(x0, y1 + sy, color)
                if (x0 == x1) break
                // Xを1つ進める
                err += dy
                if (err > resolution) {
                    // Yを1つ進める
                    err -= dx
                    y0 += sy
                }
                x0 += sx
            }
        }
    }

    /**
     * 線を描画する (太さが1のとき)
     * @param canvas 描画先
     */
    private fun plotLine(canvas: MapCanvas) {
        var x0 = x0
        var y0 = y0

        // 伸ばしていく方向
        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1

        // 始点から終点までの幅と高さを取得
        val dx = abs(x1 - x0)
        val dy = -abs(y1 - y0)

        // 誤差
        var err = dx + dy

        while (true) {
            canvas.setPixel(x0, y0, color)
            if (x0 == x1 && y0 == y1) break
            val e2 = 2 * err
            if (e2 >= dy) {
                // Xを1つ進める
                err += dy
                x0 += sx
            }
            if (e2 <= dx) {
                // Yを1つ進める
                err += dx
                y0 += sy
            }
        }
    }
}