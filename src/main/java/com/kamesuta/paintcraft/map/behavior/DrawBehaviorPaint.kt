package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.draw.Drawable

/**
 * 描きこみを行うツール
 */
object DrawBehaviorPaint : DrawBehavior {
    override val name = "paint"

    override fun paint(session: CanvasSession, event: PaintEvent) {
        session.tool.paint(event)
    }

    override fun draw(draw: Drawable, f: Drawable.() -> Unit) {
        f(draw)
    }

    override fun init(draw: Drawable) {
    }
}