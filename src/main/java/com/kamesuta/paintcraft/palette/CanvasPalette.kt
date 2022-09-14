package com.kamesuta.paintcraft.palette

import com.kamesuta.paintcraft.util.color.HSBColor

/**
 * 選択中の色情報、太さなどほ保持する
 */
class CanvasPalette {
    /** 現在の選択中のHSB色 */
    var hsbColor = HSBColor(0.0, 0.0, 0.0)

    /** 現在の選択中のRGB色 */
    var color: Byte = 0
}