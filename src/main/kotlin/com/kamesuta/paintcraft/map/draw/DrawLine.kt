package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.map.image.PixelImage
import com.kamesuta.paintcraft.util.fuzzyEq
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
    override fun draw(canvas: PixelImage) {
        // Bresenham's Algorithm
        // http://members.chello.at/~easyfilter/bresenham.html
        // http://members.chello.at/~easyfilter/canvas.html

        // 処理の解像度
        val resolution = 255

        // 伸ばしていく方向
        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1

        // 始点から終点までの幅と高さを取得
        val dx0 = abs(x1 - x0)
        val dy0 = abs(y1 - y0)

        // 線の長さ
        val length = sqrt((dx0 * dx0 + dy0 * dy0).toDouble())

        // 線が細い、または線の長さが0の場合、線を描画するだけ
        if (thickness <= 0 || length fuzzyEq 0.0) {
            plotLine(canvas)
            return
        }
        // 線の太さ
        val th = resolution * (thickness - 1)

        // 縦と横の差
        val dx = dx0 * resolution / length
        val dy = dy0 * resolution / length

        /** 太さを考慮して点を描画する */
        fun plot(x: Int, y: Int) {
            if (sx * (x - x0) * dx + sy * (y - y0) * dy < 0) return
            if (sx * (x - x1) * dx + sy * (y - y1) * dy > 0) return
            canvas[x, y] = color
        }

        // 斜めの場合、線の太さを考慮して長めに描画する必要がある
        // そのため、線の太さを考慮した長さの増加分を計算する
        val th2 = th / 2.0 / resolution / resolution
        if (dx < dy) {
            // 急な線の場合
            // 開始オフセット
            val x1 = (length + th / 2.0) / dy
            // 誤差をオフセット幅までずらす
            var err = x1 * dy - th / 2.0
            // 線の太さを考慮した長さの増加分を加算する
            var x0 = x0 - ((x1 + th2 * dx) * sx).roundToInt()
            var y0 = y0 - (th2 * dy * sy).roundToInt()
            val y1 = y1 + (th2 * dy * sy).roundToInt()
            while (true) {
                // 開始点寄りの縁のピクセル (今回はアンチエイリアスしないため塗りつぶす)
                var x2 = x0
                plot(x2, y0)
                var e2 = dy - err - th
                while (e2 + dy < resolution) {
                    // 線上のピクセル
                    x2 += sx
                    plot(x2, y0)
                    e2 += dy
                }
                // 終了点寄りの縁のピクセル (今回はアンチエイリアスしないため塗りつぶす)
                plot(x2 + sx, y0)
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
            val y1 = (length + th / 2.0) / dx
            // 誤差をオフセット幅までずらす
            var err = y1 * dx - th / 2.0
            // 線の太さを考慮した長さの増加分を加算する
            var y0 = y0 - ((y1 + th2 * dy) * sy).roundToInt()
            var x0 = x0 - (th2 * dx * sx).roundToInt()
            val x1 = x1 + (th2 * dx * sx).roundToInt()
            while (true) {
                // 開始点寄りの縁のピクセル (今回はアンチエイリアスしないため塗りつぶす)
                var y2 = y0
                plot(x0, y2)
                var e2 = dx - err - th
                while (e2 + dx < resolution) {
                    // 線上のピクセル
                    y2 += sy
                    plot(x0, y2)
                    e2 += dx
                }
                // 終了点寄りの縁のピクセル (今回はアンチエイリアスしないため塗りつぶす)
                plot(x0, y2 + sy)
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
    private fun plotLine(canvas: PixelImage) {
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
            canvas[x0, y0] = color
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