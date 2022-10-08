package com.kamesuta.paintcraft.player

import com.kamesuta.paintcraft.frame.FrameEntity
import com.kamesuta.paintcraft.frame.FrameEntityBukkit
import com.kamesuta.paintcraft.util.vec.Rect3d
import com.kamesuta.paintcraft.util.vec.Vec3d
import com.kamesuta.paintcraft.util.vec.toVec3d
import com.kamesuta.paintcraft.util.vec.toVector
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.ItemFrame
import org.bukkit.util.BoundingBox

/**
 * アイテムフレームを含むワールド
 * @param world ワールド
 */
class PaintWorldBukkit(private val world: World) : PaintWorld {
    override fun getFrameEntities(area: Rect3d): List<FrameEntity> {
        // 範囲を変換
        val box = BoundingBox.of(area.min.toVector(), area.max.toVector())
        // ワールドにあるアイテムフレームを取得
        return world.getNearbyEntities(box) { it is ItemFrame }
            .asSequence()
            .map { it as ItemFrame }
            // その中からアイテムフレームを取得する
            .filter { it.item.type == Material.FILLED_MAP }
            // FrameEntityに変換する
            .map { FrameEntityBukkit(it) }
            .toList()
    }

    override fun rayTraceBlockLocation(origin: Vec3d, direction: Vec3d, maxDistance: Double): Vec3d? {
        // レイキャストを行い、ヒットしたブロックがあればそのブロック座標と目線の位置から範囲の中心座標とサイズを計算する
        val blockRay = world.rayTraceBlocks(
            origin.toVector().toLocation(world),
            direction.toVector(),
            maxDistance
        )
        // クリックがヒットした座標
        return blockRay?.hitPosition?.toVec3d()
    }
}