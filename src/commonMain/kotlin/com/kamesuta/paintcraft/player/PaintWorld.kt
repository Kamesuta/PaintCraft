package com.kamesuta.paintcraft.player

import com.kamesuta.paintcraft.frame.FrameEntity
import com.kamesuta.paintcraft.util.vec.Rect3d
import com.kamesuta.paintcraft.util.vec.Vec3d

/**
 * アイテムフレームを含むワールド
 */
interface PaintWorld {
    /**
     * ワールド内のアイテムフレームを取得する
     * @return アイテムフレームのリスト
     */
    fun getFrameEntities(area: Rect3d): List<FrameEntity>

    /**
     * レイを飛ばしてヒットしたブロックの座標を取得
     * @param origin 始点
     * @param direction 方向
     * @param maxDistance 最大距離
     * @return ヒットした座標
     */
    fun rayTraceBlockLocation(origin: Vec3d, direction: Vec3d, maxDistance: Double): Vec3d?
}