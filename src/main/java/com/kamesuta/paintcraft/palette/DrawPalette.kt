package com.kamesuta.paintcraft.palette

import com.kamesuta.paintcraft.canvas.CanvasMode
import com.kamesuta.paintcraft.map.DrawableMapBuffer
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.map.draw.Draw
import com.kamesuta.paintcraft.util.color.HSBColor
import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.color.RGBColor.Companion.toRGB
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.black
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.transparent
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.white
import com.kamesuta.paintcraft.util.vec.Vec2d
import com.kamesuta.paintcraft.util.vec.Vec2i
import org.bukkit.map.MapCanvas
import org.bukkit.map.MinecraftFont
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * カラーピッカーを描画するクラス
 * @param palette パレットデータ
 * @param playerMode プレイヤーの選択モード
 */
class DrawPalette(
    private val palette: PaletteData,
    private val playerMode: CanvasMode?,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        val mode = playerMode

        val hue = mode?.hsbColor?.hue ?: 0.0
        // 色相に合うパレットマップをキャッシュから取得 (見つからないことはないと思う)
        val cache = cachedPalette[Math.floorMod((hue * 255.0).toInt(), 255)] ?: return
        // キャッシュを描画する
        for (iy in 0 until mapSize) {
            for (ix in 0 until mapSize) {
                val color = cache[ix, iy]
                canvas.setPixel(ix, iy, color)
            }
        }

        // パレットを描画する
        run {
            // 一番左のスロットの位置
            val start = mapSize / 2 - (storedPaletteSize * (PaletteData.MAP_PALETTE_SIZE - 1)) / 2
            // 全スロットを描画する
            palette.storedPalettes.forEachIndexed { index, color ->
                val y = start + index * storedPaletteSize
                canvas.drawCursor(storedPaletteOffsetX, y, color, color, storedPaletteSize / 2 - 1)
            }
            // 透明が選択されていないときのみカーソルを描画
            if (mode?.mapColor != transparent) {
                // 選択中のスロットを描画する
                val y = start + palette.selectedPaletteIndex * storedPaletteSize
                val color = palette.storedPalettes.getOrNull(palette.selectedPaletteIndex)
                    ?: return@run
                if (mode?.mapColor == color) {
                    val oppositeColor = RGBColor.fromMapColor(color).toOpposite().toMapColor()
                    canvas.drawCursor(storedPaletteOffsetX, y, color, oppositeColor, storedPaletteSize / 2)
                }
            }
        }

        // ボタンカラー (黒色: -49)
        val buttonColor: Byte = black

        // 透明ボタンを描画する
        run {
            // 反対色
            val oppositeColor: Byte = if (mode?.mapColor == transparent) white else transparent

            // 四角と塗りつぶしを描画
            canvas.drawCursor(
                transparentButtonPosition.x,
                transparentButtonPosition.y,
                oppositeColor,
                buttonColor,
                buttonSize / 2
            )
            // 斜線を描画
            for (i in -buttonSize / 2 until buttonSize / 2) {
                canvas.setPixel(transparentButtonPosition.x - i, transparentButtonPosition.y + i, buttonColor)
            }
        }

        // カラーピッカーボタンを描画する
        run {
            // 反対色
            val oppositeColor: Byte = if (mode?.tool is PaintColorPicker) white else transparent

            // 四角と塗りつぶしを描画
            canvas.drawCursor(
                colorPickerButtonPosition.x,
                colorPickerButtonPosition.y,
                oppositeColor,
                buttonColor,
                buttonSize / 2
            )
            // 内側の四角
            canvas.drawCursor(
                colorPickerButtonPosition.x,
                colorPickerButtonPosition.y,
                oppositeColor,
                buttonColor,
                buttonSize / 2 - 2
            )
            // 十字を描画
            for (i in -buttonSize / 2 until buttonSize / 2) {
                canvas.setPixel(colorPickerButtonPosition.x + i, colorPickerButtonPosition.y, buttonColor)
                canvas.setPixel(colorPickerButtonPosition.x, colorPickerButtonPosition.y + i, buttonColor)
            }
        }

        if (mode != null) {
            val hsbColor = mode.hsbColor
            val rgbColor = hsbColor.toRGB()
            val color = rgbColor.toMapColor()

            // 透明が選択されていないときのみカーソルを描画
            if (mode.mapColor != transparent) {
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
                        color,
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

            // カラーコードを描画する
            val mapColorHex = String.format("%02X", color)
            canvas.drawText(
                colorCodePosition.x - colorCodeSize.x / 2,
                colorCodePosition.y - colorCodeSize.y / 2,
                MinecraftFont.Font,
                "§$color;${rgbColor.toHexCode()} ($mapColorHex)",
            )
        }
    }

    companion object {
        /** 彩度と明度を選択する円の半径 */
        private const val radiusSatBri = 0.4

        /** 色相を選択する円の内側の円の半径 */
        private const val radiusSpace = 0.36

        /** 色相を選択する円の外側の円の半径 */
        private const val radiusHue = 0.6

        /** パレットのX座標 */
        private const val storedPaletteOffsetX = 16

        /** 色相を選択する円の半径 */
        private const val storedPaletteSize = mapSize / 16

        /** ボタンのサイズ */
        private const val buttonSize = 9

        /** 透明ボタンの位置 */
        private val transparentButtonPosition = Vec2i(30, 30)

        /** カラーピッカーボタンの位置 */
        private val colorPickerButtonPosition = Vec2i(mapSize - 30, 30)

        /** カラーコードの位置 */
        private val colorCodePosition = Vec2i(mapSize / 2, 21)

        /** カラーコードのサイズ */
        private val colorCodeSize = Vec2i(66, 9)

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
        ): PaletteAdjustingType {
            // パレットの位置
            val start = mapSize / 2 - (storedPaletteSize * (PaletteData.MAP_PALETTE_SIZE)) / 2
            val end = mapSize / 2 + (storedPaletteSize * (PaletteData.MAP_PALETTE_SIZE)) / 2
            if (y in start..end && x - storedPaletteOffsetX in -storedPaletteSize / 2..storedPaletteSize / 2) {
                return PaletteAdjustingType.STORED_PALETTE
            }

            // 透明ボタンの位置
            if (x - transparentButtonPosition.x in -buttonSize / 2..buttonSize / 2
                && y - transparentButtonPosition.y in -buttonSize / 2..buttonSize / 2
            ) {
                return PaletteAdjustingType.TRANSPARENT_COLOR
            }

            // 透明ボタンの位置
            if (x - colorPickerButtonPosition.x in -buttonSize / 2..buttonSize / 2
                && y - colorPickerButtonPosition.y in -buttonSize / 2..buttonSize / 2
            ) {
                return PaletteAdjustingType.COLOR_PICKER_COLOR
            }

            // カラーコードの位置
            if (x - colorCodePosition.x in -colorCodeSize.x / 2..colorCodeSize.x / 2
                && y - colorCodePosition.y in -colorCodeSize.y / 2..colorCodeSize.y / 2
            ) {
                return PaletteAdjustingType.COLOR_CODE
            }

            // -1.0 ~ 1.0
            val iVec = Vec2d(x.toDouble(), y.toDouble()) / (mapSize.toDouble() / 2.0) - Vec2d(1.0, 1.0)
            return when (iVec.length) {
                // 中央の彩度と明度を選択する円
                in 0.0..radiusSpace -> PaletteAdjustingType.SATURATION_BRIGHTNESS
                // 周りの色相を選択するドーナツ円
                in radiusSatBri..radiusHue -> PaletteAdjustingType.HUE
                // その他は無視
                else -> PaletteAdjustingType.NONE
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
            adjustingType: PaletteAdjustingType,
            HSBColor: HSBColor,
        ): HSBColor? {
            // -1.0 ~ 1.0
            val iVec = Vec2d(x.toDouble(), y.toDouble()) / (mapSize.toDouble() / 2.0) - Vec2d(1.0, 1.0)
            return when (adjustingType) {
                // 中央の彩度と明度を選択する円
                PaletteAdjustingType.SATURATION_BRIGHTNESS -> {
                    val s = ((iVec.x / radiusSatBri * 1.414 + 1.0) / 2.0).coerceIn(0.0, 1.0)
                    val b = ((iVec.y / radiusSatBri * 1.414 + 1.0) / 2.0).coerceIn(0.0, 1.0)
                    HSBColor(HSBColor.hue, s, b)
                }

                // 周りの色相を選択するドーナツ円
                PaletteAdjustingType.HUE -> {
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
         * @param y Y座標
         * @return パレットの番号
         */
        fun getStoredPaletteIndex(y: Int): Int {
            // パレットの位置
            val start = mapSize / 2 - (storedPaletteSize * (PaletteData.MAP_PALETTE_SIZE)) / 2
            return (y - start) / storedPaletteSize
        }

        /**
         * パレットをピクセルデータから読み込む
         * @receiver canvas 読み込む元のマップ
         * @param paletteData 読み込んだデータを保存するパレットデータ
         */
        fun MapCanvas.loadPalette(paletteData: PaletteData) {
            // パレットの位置
            val start = mapSize / 2 - (storedPaletteSize * (PaletteData.MAP_PALETTE_SIZE - 1)) / 2
            val left = storedPaletteOffsetX + storedPaletteSize / 2
            for (index in 0 until PaletteData.MAP_PALETTE_SIZE) {
                // 横のピクセルを取得
                val y = start + index * storedPaletteSize

                // パレットの色を取得
                val color = getPixel(storedPaletteOffsetX, y)
                if (color != transparent) {
                    paletteData.storedPalettes[index] = color
                }

                // 縁が描画されていたら選択されている
                val frameColor = getPixel(left, y)
                if (frameColor != transparent)
                    paletteData.selectedPaletteIndex = index
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
    }
}