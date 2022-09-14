package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.canvas.CanvasPalette
import com.kamesuta.paintcraft.map.DrawableMapBuffer
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.util.color.HSBColor
import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.color.RGBColor.Companion.toRGB
import com.kamesuta.paintcraft.util.vec.Vec2d
import org.bukkit.map.MapCanvas
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * カラーピッカーを描画するクラス
 * @param palette パレット
 */
class DrawPalette(
    private val palette: CanvasPalette?,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        val palette = palette

        val hue = palette?.hsbColor?.hue ?: 0.0
        // 色相に合うパレットマップをキャッシュから取得 (見つからないことはないと思う)
        val cache = cachedPalette[Math.floorMod((hue * 255.0).toInt(), 255)] ?: return
        // キャッシュを描画する
        for (iy in 0 until mapSize) {
            for (ix in 0 until mapSize) {
                val color = cache[ix, iy]
                canvas.setPixel(ix, iy, color)
            }
        }

        if (palette != null) {
            val hsbColor = palette.hsbColor
            val rgbColor = hsbColor.toRGB()

            // パレットを描画する
            run {
                // 一番左のスロットの位置
                val start = mapSize / 2 - (storedPaletteSize * (CanvasPalette.MAP_PALETTE_SIZE - 1)) / 2
                // 全スロットを描画する
                palette.storedPalettes.forEachIndexed { index, color ->
                    val x = start + index * storedPaletteSize
                    canvas.drawCursor(x, storedPaletteOffsetY, color, color, storedPaletteSize / 2 - 1)
                }
                // 選択中のスロットを描画する
                val x = start + palette.selectedPaletteIndex * storedPaletteSize
                val color = palette.storedPalettes.getOrNull(palette.selectedPaletteIndex)
                    ?: return@run
                val oppositeColor = RGBColor.fromMapColor(color).toOpposite().toMapColor()
                canvas.drawCursor(x, storedPaletteOffsetY, color, oppositeColor, storedPaletteSize / 2)
            }

            // 明度と彩度のカーソルを描画する
            run {
                // 反対色
                val oppositeColor = rgbColor.toOpposite().toMapColor()

                // カーソルの位置
                val vec = Vec2d(
                    (hsbColor.saturation * 2.0 - 1.0) / 1.414 * radiusSatBri,
                    (hsbColor.brightness * 2.0 - 1.0) / 1.414 * radiusSatBri,
                )
                // カーソルを描画する
                canvas.drawCursor(
                    ((vec.x + 1.0) / 2.0 * mapSize).toInt(),
                    ((vec.y + 1.0) / 2.0 * mapSize).toInt(),
                    rgbColor.toMapColor(),
                    oppositeColor
                )
            }

            // 色相カーソルを描画する
            run {
                // 彩度と明度が1.0のときの色
                val primaryHsbColor = HSBColor(hsbColor.hue, 1.0, 1.0)
                val primaryRgbColor = primaryHsbColor.toRGB()
                // 反対色
                val oppositeColor = primaryRgbColor.toOpposite().toMapColor()
                // カーソルの半径位置
                val radius = (radiusHue + radiusSatBri) / 2.0

                // カーソルの位置
                val hueVec = Vec2d(
                    cos(hsbColor.hue * 2.0 * Math.PI) * radius,
                    sin(hsbColor.hue * 2.0 * Math.PI) * radius,
                )
                // カーソルを描画する
                canvas.drawCursor(
                    ((hueVec.x + 1.0) / 2.0 * mapSize).toInt(),
                    ((hueVec.y + 1.0) / 2.0 * mapSize).toInt(),
                    primaryRgbColor.toMapColor(),
                    oppositeColor
                )
            }
        }
    }

    /**
     * カーソルを描画する
     * (中央が選択中の色で、周りが反対色)
     * @param x カーソルのX座標
     * @param y カーソルのY座標
     * @param color カーソルの色
     * @param oppositeColor カーソルの反対色
     * @param radius カーソルの大きさ
     */
    private fun MapCanvas.drawCursor(
        x: Int,
        y: Int,
        color: Byte,
        oppositeColor: Byte,
        radius: Int = 2,
    ) {
        for (iy in -radius..radius) {
            for (ix in -radius..radius) {
                if (abs(iy) == radius || abs(ix) == radius) {
                    setPixel(x + ix, y + iy, oppositeColor)
                } else {
                    setPixel(x + ix, y + iy, color)
                }
            }
        }
    }

    companion object {
        /** 彩度と明度を選択する円の半径 */
        private const val radiusSatBri = 0.4

        /** 色相を選択する円の内側の円の半径 */
        private const val radiusSpace = 0.36

        /** 色相を選択する円の外側の円の半径 */
        private const val radiusHue = 0.6

        /** 彩度と明度を選択する円の半径 */
        private const val storedPaletteOffsetY = 16

        /** 色相を選択する円の半径 */
        private const val storedPaletteSize = mapSize / 16

        /** 色相ごとのパレットを事前に計算しておく */
        private val cachedPalette = (0..255).associateWith { hue ->
            val map = DrawableMapBuffer()
            for (iy in 0 until mapSize) {
                for (ix in 0 until mapSize) {
                    val adjustingType = getAdjustingType(ix, iy)
                    val color = getColor(ix, iy, adjustingType, HSBColor(hue / 255.0, 1.0, 1.0))
                    map[ix, iy] = color?.toRGB()?.toMapColor() ?: 0
                }
            }
            map
        }

        /**
         * 調整中のモードを取得する
         * @param x X座標
         * @param y Y座標
         * @return 調整中のモード
         */
        fun getAdjustingType(
            x: Int,
            y: Int,
        ): CanvasPalette.AdjustingType {
            // パレットの位置
            val start = mapSize / 2 - (storedPaletteSize * (CanvasPalette.MAP_PALETTE_SIZE)) / 2
            val end = mapSize / 2 + (storedPaletteSize * (CanvasPalette.MAP_PALETTE_SIZE)) / 2
            if (x in start..end && y - storedPaletteOffsetY in -storedPaletteSize / 2..storedPaletteSize / 2) {
                return CanvasPalette.AdjustingType.STORED_PALETTE
            }

            // -1.0 ~ 1.0
            val iVec = Vec2d(x.toDouble(), y.toDouble()) / (mapSize.toDouble() / 2.0) - Vec2d(1.0, 1.0)
            return when (iVec.length) {
                // 中央の彩度と明度を選択する円
                in 0.0..radiusSpace -> CanvasPalette.AdjustingType.SATURATION_BRIGHTNESS
                // 周りの色相を選択するドーナツ円
                in radiusSatBri..radiusHue -> CanvasPalette.AdjustingType.HUE
                // その他は無視
                else -> CanvasPalette.AdjustingType.NONE
            }
        }

        /**
         * 座標の色を取得する
         * @param x X座標
         * @param y Y座標
         * @param adjustingType 調整中のモード
         * @param HSBColor 既に選択されている色
         * @return 色
         */
        fun getColor(
            x: Int,
            y: Int,
            adjustingType: CanvasPalette.AdjustingType,
            HSBColor: HSBColor,
        ): HSBColor? {
            // -1.0 ~ 1.0
            val iVec = Vec2d(x.toDouble(), y.toDouble()) / (mapSize.toDouble() / 2.0) - Vec2d(1.0, 1.0)
            return when (adjustingType) {
                // 中央の彩度と明度を選択する円
                CanvasPalette.AdjustingType.SATURATION_BRIGHTNESS -> {
                    val s = ((iVec.x / radiusSatBri * 1.414 + 1.0) / 2.0).coerceIn(0.0, 1.0)
                    val b = ((iVec.y / radiusSatBri * 1.414 + 1.0) / 2.0).coerceIn(0.0, 1.0)
                    HSBColor(HSBColor.hue, s, b)
                }

                // 周りの色相を選択するドーナツ円
                CanvasPalette.AdjustingType.HUE -> {
                    val h = atan2(iVec.y, iVec.x) / (Math.PI * 2.0)
                    HSBColor(
                        h,
                        HSBColor.saturation,
                        HSBColor.brightness
                    )
                }

                // その他は無視
                else -> null
            }
        }

        /**
         * 座標のパレットの番号を取得する
         * 注意: 範囲外の番号を返す可能性があります
         * @param x X座標
         * @return パレットの番号
         */
        fun getStoredPaletteIndex(x: Int): Int {
            // パレットの位置
            val start = mapSize / 2 - (storedPaletteSize * (CanvasPalette.MAP_PALETTE_SIZE)) / 2
            return (x - start) / storedPaletteSize
        }
    }
}