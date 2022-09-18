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
     * 回転後のUV座標を計算
     * @param uv 回転前のUV
     * @return 回転後のUV座標
     */
    fun uv(uv: Vec2d) = Vec2d(x1 * uv.x + y1 * uv.y, x2 * uv.x + y2 * uv.y)

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

        /**
         * BukkitのRotationから対応するFrameRotationを取得
         * 1.7.10以下のバージョン用、4方向しかない
         * @param rotation BukkitのRotation
         * @return 対応するFrameRotation
         */
        fun fromLegacyRotation(rotation: Rotation): FrameRotation {
            return when (rotation) {
                Rotation.NONE -> NONE
                Rotation.CLOCKWISE_45 -> NONE
                Rotation.CLOCKWISE -> CLOCKWISE_45
                Rotation.CLOCKWISE_135 -> CLOCKWISE_45
                Rotation.FLIPPED -> CLOCKWISE
                Rotation.FLIPPED_45 -> CLOCKWISE
                Rotation.COUNTER_CLOCKWISE -> CLOCKWISE_135
                Rotation.COUNTER_CLOCKWISE_45 -> CLOCKWISE_135
                else -> NONE
            }
        }
    }
}