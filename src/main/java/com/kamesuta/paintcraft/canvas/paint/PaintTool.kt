package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.TimeWatcher
import org.bukkit.inventory.ItemStack

/**
 * 描くためのツール
 */
interface PaintTool {
    /** 描き込み開始 */
    fun beginPainting(event: PaintEvent) {
    }

    /** 描き込み終了 */
    fun endPainting() {
    }

    /** キャンバスに描く */
    fun paint(event: PaintEvent)

    /** キャンバスセッション */
    val session: CanvasSession
}