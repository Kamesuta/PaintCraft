package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.frame.FrameRayTraceResult
import com.kamesuta.paintcraft.util.vec.Vec2i
import org.bukkit.entity.Player

/**
 * プレイヤーがキャンバスに描くときのインタラクション詳細。
 * @param uv クリック場所のUV座標
 * @param player インタラクションを行ったプレイヤー
 * @param ray アイテムフレームへのインタラクションのレイ
 * @param actionType プレイヤーのアクションタイプ
 */
data class CanvasInteraction(
    val uv: Vec2i,
    val ray: FrameRayTraceResult,
    val player: Player,
    val actionType: CanvasActionType
)