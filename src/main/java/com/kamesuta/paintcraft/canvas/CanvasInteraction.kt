package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.frame.FrameRayTraceResult
import com.kamesuta.paintcraft.util.UVInt
import org.bukkit.entity.Player

/**
 * プレイヤーがキャンバスに描くときのインタラクション詳細。
 * @param uv クリック場所のUV座標
 * @param player インタラクションを行ったプレイヤー
 * @param ray アイテムフレームへのインタラクションのレイ
 * @param actionType プレイヤーのアクションタイプ
 */
data class CanvasInteraction(
    val uv: UVInt,
    val ray: FrameRayTraceResult,
    val player: Player,
    val actionType: CanvasActionType
)