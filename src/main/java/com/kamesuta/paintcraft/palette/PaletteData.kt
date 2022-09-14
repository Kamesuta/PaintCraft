package com.kamesuta.paintcraft.palette

import com.kamesuta.paintcraft.util.color.RGBColor

class PaletteData {
    /** 現在の操作中の色相 or 明度/彩度 */
    var adjustingType = PaletteAdjustingType.NONE

    /** 保存したパレット */
    val storedPalettes: MutableList<Byte> = presetColors.toMutableList()

    /** 選択中のパレットスロット */
    var selectedPaletteIndex = 0

    companion object {
        /** 保存できるパレットの色の数 */
        const val MAP_PALETTE_SIZE = 12

        /** 初期色 */
        val presetColors: List<Byte> = listOf(
            RGBColor.fromRGB(0xFFFFFF).toMapColor(),
            RGBColor.fromRGB(0x000000).toMapColor(),
            RGBColor.fromRGB(0x7F7F7F).toMapColor(),
            RGBColor.fromRGB(0xC3C3C3).toMapColor(),
            RGBColor.fromRGB(0xED1C24).toMapColor(),
            RGBColor.fromRGB(0xFF7F27).toMapColor(),
            RGBColor.fromRGB(0xFFAEC9).toMapColor(),
            RGBColor.fromRGB(0xFFF200).toMapColor(),
            RGBColor.fromRGB(0x22B14C).toMapColor(),
            RGBColor.fromRGB(0x00A2E8).toMapColor(),
            RGBColor.fromRGB(0x3F48CC).toMapColor(),
            RGBColor.fromRGB(0xA349A4).toMapColor(),
        )
    }
}