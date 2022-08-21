package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.map.MapItem
import org.bukkit.inventory.ItemStack

/**
 * 描くためのツール
 */
interface PaintTool {
    fun paint(itemStack: ItemStack, mapItem: MapItem, interact: CanvasInteraction, session: CanvasSession): Boolean
}