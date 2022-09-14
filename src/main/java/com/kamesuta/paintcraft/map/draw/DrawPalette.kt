package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.canvas.CanvasPalette
import com.kamesuta.paintcraft.map.DrawableMapBuffer
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.util.vec.Vec2d
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapPalette
import java.awt.Color
import kotlin.math.abs
import kotlin.math.atan2

/**
 * カラーピッカーを描画するクラス
 * @param palette パレット
 */
class DrawPalette(
    private val palette: CanvasPalette?,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        val hsvColor = palette?.hsvColor
        val hue = hsvColor?.hue ?: 0.0
        // 色相に合うパレットマップをキャッシュから取得 (見つからないことはないと思う)
        val cache = cachedPalette[Math.floorMod((hue * 255.0).toInt(), 255)] ?: return
        // キャッシュを描画する
        for (iy in 0 until mapSize) {
            for (ix in 0 until mapSize) {
                val color = cache[ix, iy]
                canvas.setPixel(ix, iy, color)
            }
        }
        // カーソルを描画する
        val color = hsvColor?.toMapColor()
            ?: return
        // 反対色
        val oppositeColor = MapPalette.matchColor(Color(hsvColor.toRGB() xor 0xffffff))
        val vec = Vec2d(
            (hsvColor.saturation * 2.0 - 1.0) / 1.414 * radiusSatBri,
            (hsvColor.brightness * 2.0 - 1.0) / 1.414 * radiusSatBri,
        )
        // カーソルの位置
        val x = ((vec.x + 1.0) / 2.0 * mapSize).toInt()
        val y = ((vec.y + 1.0) / 2.0 * mapSize).toInt()
        // カーソルの大きさ
        val radius = 2
        // カーソルを描画する (中央が選択中の色で、周りが反対色)
        for (iy in -radius..radius) {
            for (ix in -radius..radius) {
                if (abs(iy) == radius || abs(ix) == radius) {
                    canvas.setPixel(x + ix, y + iy, oppositeColor)
                } else {
                    canvas.setPixel(x + ix, y + iy, color)
                }
            }
        }
    }

    companion object {
        /** 彩度と明度を選択する円の半径 */
        private const val radiusSatBri = 0.4

        /** 色相を選択する円の半径 */
        private const val radiusHue = 0.6

        /** 色相ごとのパレットを事前に計算しておく */
        private val cachedPalette = (0..255).associateWith { hue ->
            val map = DrawableMapBuffer()
            for (iy in 0 until mapSize) {
                for (ix in 0 until mapSize) {
                    val color = getColor(ix, iy, hue / 255.0, 1.0, 1.0)?.toMapColor()
                        ?: 0
                    map[ix, iy] = color
                }
            }
            map
        }

        /**
         * 座標の色を取得する
         * @param x X座標
         * @param y Y座標
         * @param hue 既に選択されている色相
         * @param saturation 既に選択されている彩度
         * @param brightness 既に選択されている明度
         * @return 色
         */
        fun getColor(
            x: Int,
            y: Int,
            hue: Double,
            saturation: Double,
            brightness: Double,
        ): CanvasPalette.HSVColor? {
            // -1.0 ~ 1.0
            val iVec = Vec2d(x.toDouble(), y.toDouble()) / (mapSize.toDouble() / 2.0) - Vec2d(1.0, 1.0)
            return when (iVec.length) {
                // 中央の彩度と明度を選択する円
                in 0.0..radiusSatBri -> {
                    val s = ((iVec.x / radiusSatBri * 1.414 + 1.0) / 2.0).coerceIn(0.0, 1.0)
                    val b = ((iVec.y / radiusSatBri * 1.414 + 1.0) / 2.0).coerceIn(0.0, 1.0)
                    CanvasPalette.HSVColor(hue, s, b)
                }

                // 周りの色相を選択するドーナツ円
                in radiusSatBri..radiusHue -> {
                    val h = atan2(iVec.y, iVec.x) / (Math.PI * 2.0)
                    CanvasPalette.HSVColor(h, saturation, brightness)
                }

                // その他は無視
                else -> null
            }
        }
    }
}