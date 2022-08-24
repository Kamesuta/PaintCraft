package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.map.MapItem
import org.bukkit.inventory.ItemStack

/**
 * 描くためのツール
 */
interface PaintTool {
    fun paint(itemStack: ItemStack, mapItem: MapItem, interact: CanvasInteraction)
    fun tick()

    val session: CanvasSession
    val isDrawing: Boolean

    companion object {
        const val TIME_DRAW: Long = 300
        val now: Long
            get() = System.currentTimeMillis()

        fun isDrawTime(time: Long): Boolean {
            return now < time + TIME_DRAW
        }
    }
}