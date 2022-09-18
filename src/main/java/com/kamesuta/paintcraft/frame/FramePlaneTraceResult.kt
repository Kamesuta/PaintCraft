package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.vec.Vec2i
import org.bukkit.entity.ItemFrame

/**
 * 平面上のヒットした位置情報
 * @param plane 平面
 * @param entities ヒットしたアイテムフレームリスト
 */
data class FramePlaneTraceResult(
    val plane: FramePlane,
    val entities: Collection<FramePlaneTraceEntityResult>,
) {
    /**
     * アイテムフレームごとの平面上のヒットした位置情報
     * @param itemFrame アイテムフレーム
     * @param mapItem 地図アイテム
     * @param frameLocation キャンバスの位置
     * @param uvStart キャンバス上の始点のUV
     * @param uvEnd キャンバス上の終点のUV
     */
    data class FramePlaneTraceEntityResult(
        val itemFrame: ItemFrame,
        val mapItem: DrawableMapItem,
        val frameLocation: FrameLocation,
        val uvStart: Vec2i,
        val uvEnd: Vec2i,
    )
}
