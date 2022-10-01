package com.kamesuta.paintcraft.util.vec.debug

import com.kamesuta.paintcraft.frame.FrameLocation
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Plane3d
import com.kamesuta.paintcraft.util.vec.Vec3d

/**
 * デバッグの座標表示関数郡
 */
object DebugLocatables {
    /** デバッグ用の線のタイプ */
    enum class DebugLineType {
        /** 直線 */
        LINE,

        /** 半直線 */
        DIRECTION,

        /** 線分 */
        SEGMENT,
    }

    /**
     * デバッグ用に点を作成する
     */
    fun Vec3d.toDebug() = DebugLocatable { _, locate ->
        locate(this)
    }

    /**
     * デバッグ用に線を作成する
     * @param lineType 線のタイプ
     */
    fun Line3d.toDebug(lineType: DebugLineType = DebugLineType.LINE) = DebugLocatable { eyeLocation, locate ->
        // 単位ベクトル
        val dir = direction.normalized
        // 最短距離の点を求めるためのdを求める (詳しくはclosestPoint()を参照)
        // closestPoint = origin + dir * d
        val v = eyeLocation.origin - origin
        val d = v.dot(dir)
        // 線分の長さ
        val length = direction.length

        // 線の種類に応じて、範囲内にあれば表示する
        fun locateLinePoint(t: Double) {
            if (d + t < 0 && lineType != DebugLineType.LINE) return
            if (d + t > length && lineType == DebugLineType.SEGMENT) return
            locate(origin + dir * (d + t))
        }
        // 始点と終点を表示する
        locate(origin)
        locate(target)
        // 目線に近い場所を表示する
        for (i in -10..10) locateLinePoint(i.toDouble() * 0.5)
        for (i in -10..10) locateLinePoint(i.toDouble() * 4.0)
    }

    /**
     * デバッグ用に平面を作成する
     */
    fun Plane3d.toDebug() = DebugLocatable { eyeLocation, locate ->
        val right = right
        val up = up
        val closestPoint = closestPoint(eyeLocation.origin)
        locatePlane(locate, closestPoint, right, up)
    }

    /**
     * デバッグ用に平面を作成する
     */
    fun FrameLocation.toDebug() = DebugLocatable { eyeLocation, locate ->
        val right = right
        val up = up
        val closestPoint = plane.closestPoint(eyeLocation.origin)
        locatePlane(locate, closestPoint, right, up)
    }

    /**
     * デバッグ用に平面を描画する
     * @param locate
     */
    private fun locatePlane(locate: (Vec3d) -> Unit, closestPoint: Vec3d, right: Vec3d, up: Vec3d) {
        for (y in -10..10) {
            for (x in -10..10) {
                val pos = closestPoint + (right * (x.toDouble() * 0.5)) + (up * (y.toDouble() * 0.5))
                locate(pos)
            }
        }
        for (y in -10..10) {
            for (x in -10..10) {
                val pos = closestPoint + (right * (x.toDouble() * 2.0)) + (up * (y.toDouble() * 2.0))
                locate(pos)
            }
        }
    }
}