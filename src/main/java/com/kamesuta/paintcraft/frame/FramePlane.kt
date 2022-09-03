package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Plane3d

/**
 * 線を引くための面を表現するクラス
 */
data class FramePlane(
    val plane: Plane3d,
    val eyeLocation: Line3d,
    val segment: Line3d,
)