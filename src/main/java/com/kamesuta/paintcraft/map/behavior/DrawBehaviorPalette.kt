package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.map.draw.Drawable
import org.bukkit.map.MapPalette

/**
 * パレット
 */
object DrawBehaviorPalette : DrawBehavior {
    override val name = "palette"

    override fun paint(session: CanvasSession, event: PaintEvent) {
        event.mapItem.draw {
            g(DrawLine(0, 0, mapSize - 1, mapSize - 1, MapPalette.matchColor(255, 255, 0)))
        }
    }

    override fun draw(draw: Drawable, f: Drawable.() -> Unit) {
    }
}