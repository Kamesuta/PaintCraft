package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.util.vec.Vec2d

/**
 * アイテムフレームのアイテムの角度からUVを計算するクラス
 */
enum class FrameRotation(
    private val x1: Double,
    private val y1: Double,
    private val x2: Double,
    private val y2: Double
) {
    // @formatter:off
    NONE					(+1.0,  0.0,  0.0, +1.0),
    CLOCKWISE_45            ( 0.0, +1.0, -1.0,  0.0),
    CLOCKWISE               (-1.0,  0.0,  0.0, -1.0),
    CLOCKWISE_135           ( 0.0, -1.0, +1.0,  0.0),
    FLIPPED                 (+1.0,  0.0,  0.0, +1.0),
    FLIPPED_45              ( 0.0, +1.0, -1.0,  0.0),
    COUNTER_CLOCKWISE       (-1.0,  0.0,  0.0, -1.0),
    COUNTER_CLOCKWISE_45    ( 0.0, -1.0, +1.0,  0.0);
    // @formatter:on

    /**
     * 回転後のUV座標を計算
     * @param uv 回転前のUV
     * @return 回転後のUV座標
     */
    fun uv(uv: Vec2d) = Vec2d(x1 * uv.x + y1 * uv.y, x2 * uv.x + y2 * uv.y)
}