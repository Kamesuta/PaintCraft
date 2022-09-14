package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.canvas.paint.PaintLine
import com.kamesuta.paintcraft.canvas.paint.PaintTool
import com.kamesuta.paintcraft.util.color.HSBColor
import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.color.RGBColor.Companion.toRGB

/**
 * 選択中の色情報、太さなどほ保持する
 * @param session キャンバスセッション
 */
class CanvasMode(private val session: CanvasSession) {
    /** 現在の選択中のHSB色 */
    var hsbColor = HSBColor(0.0, 0.0, 0.0)
        private set

    /** 現在の選択中のRGB色 */
    var mapColor: Byte = 0
        private set

    /** 描き込みツール */
    var tool: PaintTool = PaintLine(session)

    /** 色を変更したあとのコールバック */
    var onColorChanged: (() -> Unit)? = null

    /**
     * 色を変更する
     * @param color 変更後のHSB色
     */
    fun setHsbColor(color: HSBColor) {
        hsbColor = color
        mapColor = color.toRGB().toMapColor()
        onColorChanged?.invoke()
    }

    /**
     * 色を変更する
     * @param color 変更後のマップカラー
     */
    fun setMapColor(color: Byte) {
        mapColor = color
        hsbColor = RGBColor.fromMapColor(color).toHSB()
        onColorChanged?.invoke()
    }
}