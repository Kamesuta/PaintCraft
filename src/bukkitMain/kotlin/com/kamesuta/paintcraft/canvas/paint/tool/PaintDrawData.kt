package com.kamesuta.paintcraft.canvas.paint.tool

import com.kamesuta.paintcraft.frame.FrameEntity
import com.kamesuta.paintcraft.frame.FramePlaneTraceResult
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.vec.Vec2d

/**
 * 線を描く情報
 * @param itemFrame アイテムフレーム
 * @param mapItem マップアイテム
 * @param uvStart 開始座標
 * @param uvEnd 終了座標
 * @param result レイキャスト結果 (同じアイテムフレーム同士の場合は線分のレイキャストしないのでnull)
 */
class PaintDrawData(
    val itemFrame: FrameEntity,
    val mapItem: DrawableMapItem,
    val uvStart: Vec2d,
    val uvEnd: Vec2d,
    val result: FramePlaneTraceResult?
)
