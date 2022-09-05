package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Plane3d

/**
 * 線を引くための面を表現するクラス
 * @param plane 平面
 * @param eyeLocation 視点
 * @param segment 線分
 * @param rayStart レイの始点
 * @param rayEnd レイの終点
 */
data class FramePlane(
    val plane: Plane3d,
    val eyeLocation: Line3d,
    val segment: Line3d,
    val rayStart: FrameRayTraceResult,
    val rayEnd: FrameRayTraceResult,
)