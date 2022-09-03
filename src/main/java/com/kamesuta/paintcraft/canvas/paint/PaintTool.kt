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
    /**
     * キャンバスに描く
     * @param itemStack 手に持っているアイテム
     * @param mapItem　描くマップアイテム
     * @param interact キャンバスのインタラクション
     */
    fun paint(itemStack: ItemStack, mapItem: DrawableMapItem, interact: CanvasInteraction)

    /**
     * 描く以外のタイミングで呼ぶ
     */
    fun tick()

    /** キャンバスセッション */
    val session: CanvasSession

    /** 現在描画中か */
    val isDrawing: Boolean
}