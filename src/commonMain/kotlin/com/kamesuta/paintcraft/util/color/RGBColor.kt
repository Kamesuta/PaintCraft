package com.kamesuta.paintcraft.util.color

import java.awt.Color

/**
 * RGBの色情報を保持する
 * (awtのColorのラッパー)
 */
class RGBColor(val color: Color) {
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
        /** 変更なし */
        const val unchanged: Byte = -1

        /** 透明 */
        const val transparent: Byte = 0

        /** 白 */
        const val white: Byte = 34

        /** 黒 */
        const val black: Byte = -49

        /** 赤のマップカラー */
        const val red: Byte = 18

        /** 緑のマップカラー */
        const val green: Byte = -122

        /** 青のマップカラー */
        const val blue: Byte = 49
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