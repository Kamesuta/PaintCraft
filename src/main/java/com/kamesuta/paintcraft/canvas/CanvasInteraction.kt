package com.kamesuta.paintcraft.canvas

import org.bukkit.entity.Player

/**
 * プレイヤーがキャンバスに描くときのインタラクション詳細。
 * @param player インタラクションを行ったプレイヤー
 * @param ray インタラクションのレイ
 * @param actionType プレイヤーのアクションタイプ
 */
class CanvasInteraction(
    val ray: CanvasRayTraceResult,
    val player: Player,
    val actionType: CanvasActionType
)