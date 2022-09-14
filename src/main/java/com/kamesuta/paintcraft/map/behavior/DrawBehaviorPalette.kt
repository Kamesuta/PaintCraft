package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.map.draw.DrawRect
import com.kamesuta.paintcraft.map.draw.Drawable
import org.bukkit.map.MapPalette

/**
 * パレット
 */
object DrawBehaviorPalette : DrawBehavior {
    override val name = "palette"

    override fun paint(session: CanvasSession, event: PaintEvent) {
        val uv = event.interact.uv
        event.mapItem.renderer.g(
            DrawRect(
                uv.x - 1,
                uv.y - 1,
                uv.x + 1,
                uv.y + 1,
                MapPalette.matchColor(255, 0, 0),
                true
            )
        )
    }

    override fun draw(draw: Drawable, f: Drawable.() -> Unit) {
    }

    override fun init(draw: Drawable) {
        draw.g(DrawRect(0, 0, mapSize - 1, mapSize - 1, MapPalette.matchColor(255, 255, 0), true))
    }
}