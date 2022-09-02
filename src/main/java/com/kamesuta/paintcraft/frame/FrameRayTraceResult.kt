package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Vec2i
import org.bukkit.entity.ItemFrame
import org.bukkit.util.Vector

/**
 * キャンバス上のヒットした位置情報
 * @param itemFrame アイテムフレーム
 * @param mapItem 地図アイテム
 * @param canvasLocation キャンバスの位置
 * @param canvasIntersectLocation キャンバスとレイの交点
 * @param uv UV
 */
data class FrameRayTraceResult(
    val itemFrame: ItemFrame,
    val mapItem: DrawableMapItem,
    val canvasLocation: Line3d,
    val canvasIntersectLocation: Vector,
    val uv: Vec2i,
)
