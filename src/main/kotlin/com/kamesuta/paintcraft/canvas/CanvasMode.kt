package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.canvas.paint.PaintLine
import com.kamesuta.paintcraft.canvas.paint.PaintTool
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.black

/**
 * 選択中の色情報、太さなどほ保持する
 * @param session キャンバスセッション
 */
class CanvasMode(private val session: CanvasSession) {
    /** 現在の選択中のRGB色 */
    var mapColor: Byte = black
        set(value) {
            field = value
            onColorChanged?.invoke()
        }

    /** ペンの太さ */
    var thickness: Double = 0.0

    /** 描き込みツール */
    var tool: PaintTool = PaintLine(session)
        set(value) {
            // 一つ前のツールを記憶
            prevTool = field
            field = value
            // ツールを変更したらコールバックをリセット
            onColorChanged = null
        }

    /** 一つ前の描き込みツール */
    var prevTool: PaintTool = PaintLine(session)

    /** 色を変更したあとのコールバック */
    var onColorChanged: (() -> Unit)? = null
}