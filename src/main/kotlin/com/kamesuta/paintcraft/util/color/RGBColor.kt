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
        @Suppress("DEPRECATION")
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

    /**
     * 16進数カラーコードを取得
     * @return 16進数カラーコード
     */
    fun toCode(): Int {
        return color.rgb
    }

    /**
     * 16進数カラーコード文字列を取得
     * @return 16進数カラーコード文字列
     */
    fun toHexCode(): String {
        return String.format("#%02X%02X%02X", color.red, color.green, color.blue)
    }

    /** よく使うマップカラー */
    object MapColors {
        /** 透明 */
        const val transparent: Byte = 0

        /** 白 */
        val white = fromRGB(1.0, 1.0, 1.0).toMapColor()

        /** 黒 */
        val black = fromRGB(0.0, 0.0, 0.0).toMapColor()
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
            @Suppress("DEPRECATION")
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
        fun fromCode(rgb: Int): RGBColor {
            return RGBColor(Color(rgb))
        }

        /**
         * 16進数カラーコードからRGBAカラーに変換
         * @param rgba 16進数カラーコード
         * @return RGBAカラー
         */
        fun fromCodeWithAlpha(rgba: Int): RGBColor {
            return RGBColor(Color(rgba, true))
        }

        /**
         * カラーコード文字列からRGBカラーに変換
         * @param rgb 16進数カラーコード文字列
         * @return RGBカラー
         */
        fun fromHexCode(rgb: String): RGBColor? {
            var rgbHexCode = rgb
            if (rgbHexCode.startsWith("#")) {
                // 先頭の#は除外
                rgbHexCode = rgbHexCode.substring(1)
            }
            if (rgbHexCode.startsWith("0x")) {
                // 先頭の0xは除外
                rgbHexCode = rgbHexCode.substring(2)
            }
            // 数値に変換し、できなかったらnullを返す
            val rgbHex = rgbHexCode.toIntOrNull(16)
                ?: return null
            return when (rgbHexCode.length) {
                // 3桁の場合
                3 -> fromCode(
                    (rgbHex and 0xf00 shl 8 * 0x11)
                            or (rgbHex and 0xf0 shl 4 * 0x11)
                            or (rgbHex and 0xf * 0x11)
                )
                // 6桁の場合
                6 -> fromCode(rgbHex)
                // それ以外の場合は非対応
                else -> null
            }
        }
    }
}