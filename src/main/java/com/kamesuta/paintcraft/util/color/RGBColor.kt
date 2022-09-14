package com.kamesuta.paintcraft.util.color

import org.bukkit.map.MapPalette
import java.awt.Color

/**
 * RGBの色情報を保持する
 * (awtのColorのラッパー)
 */
class RGBColor(private val color: Color) {
    /**
     * マップパレットの色に変換
     * @return マップパレットの色 (Byte)
     */
    fun toMapColor(): Byte {
        return MapPalette.matchColor(color)
    }

    /**
     * RGBカラーからHSBカラーに変換
     * @return HSBカラー
     */
    fun toHSB(): HSBColor {
        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        return HSBColor(hsb[0].toDouble(), hsb[1].toDouble(), hsb[2].toDouble())
    }

    /**
     * 反転した色を取得
     * @return 反転した色
     */
    fun toOpposite(): RGBColor {
        return RGBColor(Color(255 - color.red, 255 - color.green, 255 - color.blue))
    }

    companion object {
        /**
         * RGBカラーに変換
         * @return RGBカラー (16進数カラーコード)
         */
        fun HSBColor.toRGB(): RGBColor {
            return RGBColor(Color.getHSBColor(hue.toFloat(), saturation.toFloat(), brightness.toFloat()))
        }

        /**
         * マップパレットの色からRGBカラーに変換
         * @param mapColor マップパレットの色 (Byte)
         * @return RGBカラー
         */
        fun fromMapColor(mapColor: Byte): RGBColor {
            return RGBColor(MapPalette.getColor(mapColor))
        }

        /**
         * 赤、緑、青の色からRGBカラーに変換
         * @param r 赤 (0.0～1.0)
         * @param g 緑 (0.0～1.0)
         * @param b 青 (0.0～1.0)
         * @return RGBカラー
         */
        fun fromRGB(r: Double, g: Double, b: Double): RGBColor {
            return RGBColor(Color(r.toFloat(), g.toFloat(), b.toFloat()))
        }

        /**
         * 16進数カラーコードからRGBカラーに変換
         * @param rgb 16進数カラーコード
         * @return RGBカラー
         */
        fun fromRGB(rgb: Int): RGBColor {
            return RGBColor(Color(rgb))
        }
    }
}