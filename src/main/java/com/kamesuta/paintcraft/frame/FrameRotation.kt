package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.util.vec.Vec2d
import org.bukkit.Rotation

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
     * 回転後のU成分を計算
     * @param uv 回転前のUV
     * @return 回転後のU成分
     */
    fun u(uv: Vec2d): Double {
        return x1 * uv.x + y1 * uv.y
    }

    /**
     * 回転後のV成分を計算
     * @param uv 回転前のUV
     * @return 回転後のV成分
     */
    fun v(uv: Vec2d): Double {
        return x2 * uv.x + y2 * uv.y
    }

    companion object {
        /**
         * BukkitのRotationから対応するFrameRotationを取得
         * @param rotation BukkitのRotation
         * @return 対応するFrameRotation
         */
        fun fromRotation(rotation: Rotation): FrameRotation {
            return when (rotation) {
                Rotation.NONE -> NONE
                Rotation.CLOCKWISE_45 -> CLOCKWISE_45
                Rotation.CLOCKWISE -> CLOCKWISE
                Rotation.CLOCKWISE_135 -> CLOCKWISE_135
                Rotation.FLIPPED -> FLIPPED
                Rotation.FLIPPED_45 -> FLIPPED_45
                Rotation.COUNTER_CLOCKWISE -> COUNTER_CLOCKWISE
                Rotation.COUNTER_CLOCKWISE_45 -> COUNTER_CLOCKWISE_45
                else -> NONE
            }
        }
    }
}