package com.kamesuta.paintcraft.canvas

import org.bukkit.map.MapPalette
import java.awt.Color

/**
 * 選択中の色情報、太さなどほ保持する
 */
class CanvasPalette {
    /**
     * 色相、彩度、明度の色情報を保持する
     * @param hue 色相
     * @param saturation 彩度
     * @param brightness 明度
     */
    class HSVColor(
        val hue: Double,
        val saturation: Double,
        val brightness: Double,
    ) {
        /**
         * RGBカラーに変換
         * @return RGBカラー (16進数カラーコード)
         */
        fun toRGB(): Int {
            return Color.HSBtoRGB(hue.toFloat(), saturation.toFloat(), brightness.toFloat())
        }

        /**
         * マップパレットの色に変換
         * @return マップパレットの色 (Byte)
         */
        fun toMapColor(): Byte {
            return MapPalette.matchColor(Color(toRGB()))
        }
    }

    /** 調整中の項目 */
    enum class AdjustingType {
        NONE,
        HUE,
        SATURATION_BRIGHTNESS,
    }

    /** 現在の選択中のHSV色 */
    var hsvColor = HSVColor(0.0, 0.0, 0.0)

    /** 現在の選択中のRGB色 */
    var color: Byte = 0

    /** 現在の操作中の色相 or 明度/彩度 */
    var adjustingType = AdjustingType.NONE
}