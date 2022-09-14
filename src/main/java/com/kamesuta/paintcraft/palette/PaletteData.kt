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
            RGBColor.fromCode(0xFFFFFF).toMapColor(),
            RGBColor.fromCode(0x000000).toMapColor(),
            RGBColor.fromCode(0x7F7F7F).toMapColor(),
            RGBColor.fromCode(0xC3C3C3).toMapColor(),
            RGBColor.fromCode(0xED1C24).toMapColor(),
            RGBColor.fromCode(0xFF7F27).toMapColor(),
            RGBColor.fromCode(0xFFAEC9).toMapColor(),
            RGBColor.fromCode(0xFFF200).toMapColor(),
            RGBColor.fromCode(0x22B14C).toMapColor(),
            RGBColor.fromCode(0x00A2E8).toMapColor(),
            RGBColor.fromCode(0x3F48CC).toMapColor(),
            RGBColor.fromCode(0xA349A4).toMapColor(),
        )
    }
}