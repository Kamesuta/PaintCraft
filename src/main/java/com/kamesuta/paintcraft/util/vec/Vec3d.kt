package com.kamesuta.paintcraft.util.vec

import org.bukkit.Location
import org.bukkit.util.Vector

/** ベクトルの加算 */
operator fun Vector.plus(other: Vector): Vector = clone().add(other)

/** ベクトルの減算 */
operator fun Vector.minus(other: Vector): Vector = clone().subtract(other)

/** ベクトルのスカラー倍 */
operator fun Vector.times(other: Double): Vector = clone().multiply(other)

/** ベクトルの1/スカラー倍 */
operator fun Vector.div(other: Double): Vector = clone().multiply(1.0 / other)

/** ベクトルの正規化 */
val Vector.normalized: Vector get() = clone().normalize()

/** ロケーションの加算 */
operator fun Location.plus(other: Location): Location = clone().add(other)

/** ロケーションの加算 */
operator fun Location.plus(other: Vector): Location = clone().add(other)

/** ロケーションの減算 */
operator fun Location.minus(other: Location): Location = clone().subtract(other)

/** ロケーションの減算 */
operator fun Location.minus(other: Vector): Location = clone().subtract(other)

/** ロケーションのスカラー倍 */
operator fun Location.times(other: Double): Location = clone().multiply(other)

/** 線の始点の位置 */
val Location.origin: Vector get() = toVector()

/** 線の先の位置 */
val Location.target: Vector get() = toVector().add(direction)
