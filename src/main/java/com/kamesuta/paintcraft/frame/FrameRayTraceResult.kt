package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Vec2i
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
data class FrameRayTraceResult(
    val itemFrame: ItemFrame,
    val mapItem: DrawableMapItem,
    val canvasLocation: Line3d,
    val canvasIntersectOffset: Vector,
    val uv: Vec2i,
) {
    val canvasIntersectLocation: Line3d by lazy { canvasLocation + canvasIntersectOffset }
}

