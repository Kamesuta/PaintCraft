package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.map.DrawableMapItem

/**
 * 操作の情報
 * @param mapItem 対象のマップアイテム
 * @param interact 操作
 */
data class PaintEvent(
    val mapItem: DrawableMapItem,
    val interact: CanvasInteraction,
)
