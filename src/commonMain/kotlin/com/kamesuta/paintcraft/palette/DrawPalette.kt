package com.kamesuta.paintcraft.palette

import com.kamesuta.paintcraft.map.draw.Draw
import com.kamesuta.paintcraft.map.image.*
import com.kamesuta.paintcraft.util.color.HSBColor
import com.kamesuta.paintcraft.util.color.MapColor
import com.kamesuta.paintcraft.util.color.RGBColor.Companion.toRGB
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.black
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.transparent
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.white
import com.kamesuta.paintcraft.util.color.toMapColor
import com.kamesuta.paintcraft.util.vec.Vec2d
import kotlin.math.*

/**
 * カラーピッカーを描画するクラス
 * @param palette パレットデータ
 */
class DrawPalette(
    private val palette: PaletteData,
) : Draw {
    override fun draw(canvas: PixelImage) {
        val hue = palette.hsbColor.hue
        // 色相に合うパレットマップをキャッシュから取得 (見つからないことはないと思う)
        val cache = cachedPalette[Math.floorMod((hue * 255.0).toInt(), 255)] ?: return
        // キャッシュを描画する
        for (iy in 0 until mapSize) {
            for (ix in 0 until mapSize) {
                val color = cache[ix, iy]
                canvas[ix, iy] = color
            }
        }

        // パレットを描画する
        run {
            // 一番左のスロットの位置
            val start = mapSize / 2 - (storedPaletteSize * (PaletteData.MAP_PALETTE_SIZE - 1)) / 2
            // 全スロットを描画する
            palette.storedPalettes.forEachIndexed { index, color ->
                val y = start + index * storedPaletteSize
                canvas.drawCursor(storedPalettePositionX, y, color, color, storedPaletteSize / 2 - 1)
            }
            // 透明が選択されていないときのみカーソルを描画
            if (palette.mapColor != transparent) {
                // 選択中のスロットを描画する
                val y = start + palette.selectedPaletteIndex * storedPaletteSize
                val color = palette.storedPalettes.getOrNull(palette.selectedPaletteIndex)
                    ?: return@run
                if (palette.mapColor == color) {
                    val oppositeColor = MapColor.toRGBColor(color).toOpposite().toMapColor()
                    canvas.drawCursor(storedPalettePositionX, y, color, oppositeColor, storedPaletteSize / 2)
                }
            }
        }

        // ボタンカラー (黒色: -49)
        val buttonColor: Byte = black

        // 透明ボタンを描画する
        run {
            // 反対色
            val oppositeColor: Byte = if (palette.mapColor == transparent) white else transparent

            // 四角と塗りつぶしを描画
            canvas.drawCursor(
                transparentButtonPosition.x,
                transparentButtonPosition.y,
                oppositeColor,
                buttonColor,
                buttonSize / 2
            )
            // 斜線を描画
            val x0 = transparentButtonPosition.x.roundToInt()
            val y0 = transparentButtonPosition.y.roundToInt()
            val size0 = (buttonSize / 2.0).toInt()
            for (i in -size0 until size0) {
                canvas[x0 - i, y0 + i] = buttonColor
            }
        }

        // カラーピッカーボタンを描画する
        run {
            // 反対色
            val oppositeColor: Byte = if (palette.isPickerTool) white else transparent

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
                buttonSize / 2.0 - 2.0
            )
            // 十字を描画
            val x0 = colorPickerButtonPosition.x.roundToInt()
            val y0 = colorPickerButtonPosition.y.roundToInt()
            val size0 = (buttonSize / 2.0).toInt()
            for (i in -size0 until size0) {
                canvas[x0 + i, y0] = buttonColor
                canvas[x0, y0 + i] = buttonColor
            }
        }

        // 太さスライダーを描画する
        run {
            // 太さスライダー描画
            val sliderSizeY = thicknessSliderSize.y.toInt()
            val sliderPosX = thicknessSliderPosition.x.roundToInt()
            val sliderPosY = thicknessSliderPosition.y.roundToInt()
            for (iy in -sliderSizeY / 2..sliderSizeY / 2) {
                val width = ((0.5 - iy / thicknessSliderSize.y) * thicknessSliderSize.x).roundToInt()
                // スライダーの背景
                for (ix in 0..width) {
                    // だんだん細くなるように描画
                    canvas[sliderPosX - ix, sliderPosY + iy] = buttonColor
                }
            }

            // カーソルを描画
            val radius = (palette.thickness / thicknessMax * thicknessSliderSize.x / 2) + 3.0
            canvas.drawCursor(
                sliderPosX - radius / 2.0,
                getYFromThickness(palette.thickness),
                white,
                buttonColor,
                radius
            )
        }

        // 選択中の色を描画する
        run {
            val hsbColor = palette.hsbColor
            val rgbColor = hsbColor.toRGB()
            val color = rgbColor.toMapColor()

            // 透明が選択されていないときのみカーソルを描画
            if (palette.mapColor != transparent) {
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
                        (vec.x + 1.0) / 2.0 * mapSize,
                        (vec.y + 1.0) / 2.0 * mapSize,
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
                        (hueVec.x + 1.0) / 2.0 * mapSize,
                        (hueVec.y + 1.0) / 2.0 * mapSize,
                        primaryRgbColor.toMapColor(),
                        oppositeColor
                    )
                }
            }

            // カラーコードを描画する
            val hexCode = rgbColor.toHexCode()
            val textWidth = canvas.getWidth(hexCode)
            canvas.drawText(
                colorCodePosition.x - textWidth / 2,
                colorCodePosition.y - colorCodeSize.y / 2,
                color,
                hexCode,
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
        private const val storedPalettePositionX = 16.0

        /** 色相を選択する円の半径 */
        private const val storedPaletteSize = mapSize / 16.0

        /** ボタンのサイズ */
        private const val buttonSize = 9.0

        /** 透明ボタンの位置 */
        private val transparentButtonPosition = Vec2d(30.0, 30.0)

        /** カラーピッカーボタンの位置 */
        private val colorPickerButtonPosition = Vec2d(mapSize - 30.0, 30.0)

        /** カラーコードの位置 */
        private val colorCodePosition = Vec2d(mapSize / 2.0, 21.0)

        /** カラーコードのサイズ */
        private val colorCodeSize = Vec2d(50.0, 9.0)

        /** 最大の太さ */
        private const val thicknessMax = 32.0

        /** 太さスライダーの位置 */
        private val thicknessSliderPosition = Vec2d(mapSize - 14.0, mapSize / 2.0)

        /** 太さスライダーのサイズ */
        private val thicknessSliderSize = Vec2d(6.0, mapSize - 30.0)

        /** 色相ごとのパレットを事前に計算しておく */
        val cachedPalette = (0..255).associateWith { hue ->
            val map = PixelImageMapBuffer()
            for (iy in 0 until mapSize) {
                for (ix in 0 until mapSize) {
                    val adjustingType = getAdjustingType(ix.toDouble(), iy.toDouble())
                    val color = getColor(ix.toDouble(), iy.toDouble(), adjustingType, HSBColor(hue / 255.0, 1.0, 1.0))
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
            x: Double,
            y: Double,
        ): PaletteAdjustingType {
            // パレットの位置
            val start = mapSize / 2.0 - (storedPaletteSize * (PaletteData.MAP_PALETTE_SIZE)) / 2.0
            val end = mapSize / 2.0 + (storedPaletteSize * (PaletteData.MAP_PALETTE_SIZE)) / 2.0
            if (y in start..end && x - storedPalettePositionX in -storedPaletteSize / 2..storedPaletteSize / 2) {
                return PaletteAdjustingType.STORED_PALETTE
            }

            // 透明ボタンの位置
            if (x - transparentButtonPosition.x in -buttonSize / 2..buttonSize / 2
                && y - transparentButtonPosition.y in -buttonSize / 2..buttonSize / 2
            ) {
                return PaletteAdjustingType.TRANSPARENT_COLOR
            }

            // カラーピッカーボタンの位置
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

            // 太さスライダーの位置
            val sliderRadius = thicknessSliderSize.x.toInt()
            val sliderWidth = max(thicknessSliderSize.x, sliderRadius * 2.0)
            if (x - thicknessSliderPosition.x + thicknessSliderSize.x / 2.0 in -sliderWidth / 2.0..sliderWidth / 2.0
                && y - thicknessSliderPosition.y in -thicknessSliderSize.y / 2.0..thicknessSliderSize.y / 2.0
            ) {
                return PaletteAdjustingType.THICKNESS
            }

            // -1.0 ~ 1.0
            val iVec = Vec2d(x, y) / (mapSize.toDouble() / 2.0) - Vec2d(1.0, 1.0)
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
            x: Double,
            y: Double,
            adjustingType: PaletteAdjustingType,
            HSBColor: HSBColor,
        ): HSBColor? {
            // -1.0 ~ 1.0
            val iVec = Vec2d(x, y) / (mapSize.toDouble() / 2.0) - Vec2d(1.0, 1.0)
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
        fun getStoredPaletteIndex(y: Double): Int {
            // パレットの位置
            val start = mapSize / 2.0 - (storedPaletteSize * (PaletteData.MAP_PALETTE_SIZE)) / 2.0
            return ((y - start) / storedPaletteSize).toInt()
        }

        /**
         * 座標のペンの太さを取得する
         * @param y Y座標
         * @return ペンの太さ
         */
        fun getThickness(y: Double): Double {
            // パレットの位置
            val height = thicknessSliderSize.y - 15.0
            val start = thicknessSliderPosition.y + height / 2.0 + 2.0
            return (-(y - start) / height * thicknessMax).coerceIn(
                0.0,
                thicknessMax
            )
        }

        /**
         * 太さからY座標を取得する
         * @param thickness 太さ
         * @return Y座標
         */
        fun getYFromThickness(thickness: Double): Double {
            // パレットの位置
            val height = thicknessSliderSize.y - 15.0
            val start = thicknessSliderPosition.y + height / 2.0 + 2.0
            return thickness / thicknessMax * -height + start
        }

        /**
         * パレットをピクセルデータから読み込む
         * @receiver canvas 読み込む元のマップ
         * @param paletteData 読み込んだデータを保存するパレットデータ
         */
        fun PixelImage.loadPalette(paletteData: PaletteData) {
            // 透明かどうか
            val isTransparent =
                this[transparentButtonPosition.x.toInt() - 1, transparentButtonPosition.y.toInt() - 1] != transparent

            if (isTransparent) {
                // 透明色を設定
                paletteData.selectedPaletteIndex = -1
                paletteData.mapColor = transparent
                paletteData.hsbColor = HSBColor(0.0, 0.0, 0.0)
            } else {
                // パレット、選択中の色を読み込む
                // パレットの位置
                val start = mapSize / 2.0 - (storedPaletteSize * (PaletteData.MAP_PALETTE_SIZE - 1)) / 2.0
                val left = storedPalettePositionX + storedPaletteSize / 2.0
                for (index in 0 until PaletteData.MAP_PALETTE_SIZE) {
                    // 横のピクセルを取得
                    val y = start + index * storedPaletteSize

                    // パレットの色を取得
                    val color = this[storedPalettePositionX.toInt(), y.toInt()]
                    if (color != transparent) {
                        paletteData.storedPalettes[index] = color
                    }

                    // 縁が描画されていたら選択されている
                    val frameColor = this[left.toInt(), y.toInt()]
                    if (frameColor != transparent) {
                        paletteData.selectedPaletteIndex = index
                        paletteData.mapColor = color
                        paletteData.hsbColor = MapColor.toRGBColor(color).toHSB()
                    }
                }
            }

            // 太さを読み込む
            run {
                // スライダーの位置
                val sliderSizeY = thicknessSliderSize.y.toInt()
                val sliderPosX = thicknessSliderPosition.x.roundToInt()
                val sliderPosY = thicknessSliderPosition.y.roundToInt()
                // 線の太さを取得
                var cursor = false
                var cursorStart = 0
                var cursorEnd = 0
                // 一列を走査し開始位置と終了位置を取得
                for (iy in -sliderSizeY / 2..sliderSizeY / 2) {
                    if (this[sliderPosX + 1, sliderPosY + iy] != transparent) {
                        if (!cursor) {
                            cursorStart = sliderPosY + iy
                            cursorEnd = sliderPosY + iy
                            cursor = true
                        } else {
                            cursorStart = min(cursorStart, sliderPosY + iy)
                            cursorEnd = max(cursorEnd, sliderPosY + iy)
                        }
                    }
                }
                if (cursor) {
                    // カーソルがあれば開始位置と終了位置の中間がカーソルの位置
                    // その位置からスライダーの位置を計算
                    paletteData.thickness = getThickness((cursorStart + cursorEnd) / 2.0)
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
        fun PixelImage.drawCursor(
            x: Double,
            y: Double,
            color: Byte,
            oppositeColor: Byte,
            radius: Double = 2.0,
        ) {
            val x0 = x.roundToInt()
            val y0 = y.roundToInt()
            val radius0 = radius.roundToInt()
            for (iy in -radius0..radius0) {
                for (ix in -radius0..radius0) {
                    if (abs(iy) == radius0 || abs(ix) == radius0) {
                        this[x0 + ix, y0 + iy] = oppositeColor
                    } else {
                        this[x0 + ix, y0 + iy] = color
                    }
                }
            }
        }
    }
}