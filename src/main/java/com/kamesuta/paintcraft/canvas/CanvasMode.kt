package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.canvas.paint.PaintLine
import com.kamesuta.paintcraft.canvas.paint.PaintTool
import com.kamesuta.paintcraft.util.color.HSBColor

/**
 * 選択中の色情報、太さなどほ保持する
 * @param session キャンバスセッション
 */
class CanvasMode(private val session: CanvasSession) {
    /** 現在の選択中のHSB色 */
    var hsbColor = HSBColor(0.0, 0.0, 0.0)

    /** 現在の選択中のRGB色 */
    var color: Byte = 0

    /** 塗りつぶしツール */
    var tool: PaintTool = PaintLine(session)
}