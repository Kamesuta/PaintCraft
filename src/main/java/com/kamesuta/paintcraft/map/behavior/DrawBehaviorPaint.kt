package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent

/**
 * 描きこみを行うツール
 */
object DrawBehaviorPaint : DrawBehavior {
    override val name = "paint"

    override fun draw(session: CanvasSession, event: PaintEvent) {
        session.tool.paint(event)
    }
}