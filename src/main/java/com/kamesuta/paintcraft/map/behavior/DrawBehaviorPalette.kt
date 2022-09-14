package com.kamesuta.paintcraft.map.behavior

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
        val uv = event.interact.uv
        event.mapItem.renderer.g(DrawPalette(uv.x, uv.y))
    }

    override fun draw(draw: Drawable, f: Drawable.() -> Unit) {
    }

    override fun init(draw: Drawable) {
        draw.g(DrawPalette(0, 0))
    }
}