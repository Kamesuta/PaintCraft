package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.UVInt
import org.bukkit.Location
import org.bukkit.entity.ItemFrame
import org.bukkit.util.Vector

/**
 * キャンバス上のヒットした位置情報
 * @param itemFrame アイテムフレーム
 * @param mapItem 地図アイテム
 * @param canvasLocation キャンバス上の位置
 * @param canvasIntersectOffset キャンバス上の位置とアイテムフレーム上の位置の差分
 * @param uv UV
 */
data class CanvasRayTraceResult(
    val itemFrame: ItemFrame,
    val mapItem: DrawableMapItem,
    val canvasLocation: Location,
    val canvasIntersectOffset: Vector,
    val uv: UVInt,
) {
    val canvasIntersectLocation: Location by lazy { canvasLocation.clone().add(canvasIntersectOffset) }
}

