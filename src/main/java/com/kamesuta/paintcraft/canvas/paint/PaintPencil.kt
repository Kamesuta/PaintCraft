package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.map.MapDye
import com.kamesuta.paintcraft.map.MapItem
import com.kamesuta.paintcraft.map.draw.DrawLine
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapPalette
import java.awt.Color

class PaintPencil : PaintTool {
    var lastInteraction: CanvasInteraction? = null

    override fun paint(
        itemStack: ItemStack,
        mapItem: MapItem,
        interact: CanvasInteraction,
        session: CanvasSession
    ): Boolean {
        if (itemStack.type == Material.INK_SAC) {
            val color: MapDye = MapPalette.matchColor(Color.BLACK)

            val lastInteract = lastInteraction
            if (lastInteract != null) {
                mapItem.draw { g ->
                    g(DrawLine(lastInteract.uv.u, lastInteract.uv.v, interact.uv.u, interact.uv.v, color))
                }
            }

            lastInteraction = interact

            return true
        }
        return false
    }
}