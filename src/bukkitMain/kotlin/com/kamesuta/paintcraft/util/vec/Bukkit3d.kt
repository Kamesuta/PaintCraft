package com.kamesuta.paintcraft.util.vec

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector

/** BukkitのVectorからVec3dへの変換 */
fun Vector.toVec3d() = Vec3d(x, y, z)

/** BukkitのVectorへの変換 */
fun Vec3d.toVector() = Vector(x, y, z)

/** 線の始点の位置 */
val Location.origin: Vec3d get() = Vec3d(x, y, z)

/** 線の先の位置 */
val Location.target: Vec3d get() = toVector().add(direction).toVec3d()

/**
 * BukkitのLocationに変換する
 * @param world ワールド
 * @return Location座標
 */
@Deprecated("Locationに変換するのはコストが高いため、Line3dのまま使用することを推奨します")
fun Line3d.toLocation(world: World): Location {
    // 始点を取得
    val location = Location(world, origin.x, origin.y, origin.z)
    // 方向ベクトルからyawとpitchを求める
    location.yaw = yaw
    location.pitch = pitch
    return location
}

/**
 * 線に変換する
 * @return 線
 */
fun Location.toLine(): Line3d {
    return Line3d(origin, direction.toVec3d())
}
