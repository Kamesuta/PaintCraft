package com.kamesuta.paintcraft.canvas

import org.bukkit.Rotation

enum class CanvasRotation(val x1: Double, val y1: Double, val x2: Double, val y2: Double) {
    // @formatter:off
    NONE					(+1.0,  0.0,  0.0, +1.0),
    CLOCKWISE_45            ( 0.0, -1.0, +1.0,  0.0),
    CLOCKWISE               (-1.0,  0.0,  0.0, -1.0),
    CLOCKWISE_135           ( 0.0, +1.0, -1.0,  0.0),
    FLIPPED                 (+1.0,  0.0,  0.0, +1.0),
    FLIPPED_45              ( 0.0, -1.0, +1.0,  0.0),
    COUNTER_CLOCKWISE       (-1.0,  0.0,  0.0, -1.0),
    COUNTER_CLOCKWISE_45    ( 0.0, +1.0, -1.0,  0.0);
    // @formatter:on

    fun u(u: Double, v: Double): Double {
        return x1 * u + y1 * v
    }

    fun v(u: Double, v: Double): Double {
        return x2 * u + y2 * v
    }

    companion object {
        fun fromRotation(rotation: Rotation) : CanvasRotation {
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