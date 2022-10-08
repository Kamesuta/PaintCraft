package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.frame.FrameRayTraceResult
import com.kamesuta.paintcraft.player.PaintPlayer
import com.kamesuta.paintcraft.util.vec.Vec2d

/**
 * プレイヤーがキャンバスに描くときのインタラクション詳細。
 * @param uv クリック場所のUV座標
 * @param ray アイテムフレームへのインタラクションのレイ
 * @param player インタラクションを行ったプレイヤー
 */
data class CanvasInteraction(
    val uv: Vec2d,
    val ray: FrameRayTraceResult,
    val player: PaintPlayer,
)