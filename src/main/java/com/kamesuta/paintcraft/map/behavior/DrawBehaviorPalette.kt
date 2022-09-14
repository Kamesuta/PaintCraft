package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.canvas.CanvasDrawingActionType
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.draw.DrawPalette
import com.kamesuta.paintcraft.map.draw.Drawable

/**
 * パレット
 */
object DrawBehaviorPalette : DrawBehavior {
    override val name = "palette"

    override fun paint(session: CanvasSession, event: PaintEvent) {
        // UV座標を取得
        val uv = event.interact.uv
        // 現在の色を取得
        val hsv = session.drawing.palette.hsvColor
        // クリック開始時の場合のみ調整中のモードを設定
        if (session.drawing.drawingAction == CanvasDrawingActionType.BEGIN) {
            // 新しい調整モードを取得して置き換える
            val adjustingType = DrawPalette.getAdjustingType(uv.x, uv.y)
            session.drawing.palette.adjustingType = adjustingType
        }
        // 新しい色を取得
        val color = DrawPalette.getColor(uv.x, uv.y, session.drawing.palette.adjustingType, hsv)
        // 色が変更されている場合のみ色を設定
        if (color != null) {
            session.drawing.palette.hsvColor = color
            session.drawing.palette.color = color.toMapColor()
        }
        // パレットを描画
        event.mapItem.renderer.g(DrawPalette(session.drawing.palette))
    }

    override fun draw(draw: Drawable, f: Drawable.() -> Unit) {
    }

    override fun init(draw: Drawable) {
        // パレットを描画
        draw.g(DrawPalette(null))
    }
}