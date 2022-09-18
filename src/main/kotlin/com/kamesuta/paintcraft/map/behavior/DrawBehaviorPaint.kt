package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.DrawableMapRenderer
import com.kamesuta.paintcraft.map.draw.Drawable

/**
 * 描きこみを行うツール
 * @param renderer 描画クラス
 */
class DrawBehaviorPaint(private val renderer: DrawableMapRenderer) : DrawBehavior {
    override fun paint(session: CanvasSession, event: PaintEvent) {
        session.mode.tool.paint(event)
    }

    override fun draw(f: Drawable.() -> Unit) {
        f(renderer)
    }

    override fun init() {
    }
}