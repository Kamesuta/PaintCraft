package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.frame.FrameRayTrace.Companion.mapToBlockUV
import com.kamesuta.paintcraft.frame.FrameRayTrace.Companion.toCanvasPlane
import com.kamesuta.paintcraft.frame.FrameRayTrace.Companion.transformUV
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.vec.Line2d
import com.kamesuta.paintcraft.util.vec.Vec2d
import com.kamesuta.paintcraft.util.vec.Vec2i
import com.kamesuta.paintcraft.util.vec.minus
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.util.BoundingBox

/**
 * キャンバスと面の交差判定をします
 * @param rayTrace レイトレースツール
 */
class FramePlaneTrace(private val rayTrace: FrameRayTrace) {
    val player = rayTrace.player

    data class FramePlaneTraceEntityResult(
        val itemFrame: ItemFrame,
        val mapItem: DrawableMapItem,
        val segment: Line2d,
        val uvStart: Vec2i,
        val uvEnd: Vec2i,
    )

    data class FramePlaneTraceResult(
        val plane: FramePlane,
        val entities: List<FramePlaneTraceEntityResult>,
    )

    fun planeTraceCanvas(
        plane: FramePlane,
    ): FramePlaneTraceResult {
        // 範囲を全方向にmarginずつ拡張
        val margin = 1.0
        // エンティティを取得する範囲のバウンディングボックス
        val box = BoundingBox.of(plane.segment.origin, plane.segment.target)

        // 範囲内にあるすべてのアイテムフレームを取得する
        val result = player.world.getNearbyEntities(box.clone().expand(margin)) { it is ItemFrame }
            .asSequence()
            .map { it as ItemFrame }
            // その中からアイテムフレームを取得する
            .filter { it.item.type == Material.FILLED_MAP }
            // レイを飛ばす
            .mapNotNull { planeTraceCanvasByEntity(plane, it) }
            .toList()

        return FramePlaneTraceResult(plane, result)
    }

    private fun planeTraceCanvasByEntity(
        plane: FramePlane,
        itemFrame: ItemFrame,
    ): FramePlaneTraceEntityResult? {
        // マップデータを取得、ただの地図ならばスキップ
        val mapItem = DrawableMapItem.get(itemFrame.item)
            ?: return null
        // キャンバス平面の位置
        val canvasLocation = rayTrace.toCanvasLocation(itemFrame)
        // キャンバスの平面
        val canvasPlane =
            canvasLocation.toCanvasPlane(itemFrame.isVisible || !rayTrace.clientType.isInvisibleFrameSupported)

        // 面の交線を計算
        val canvasIntersectLine = canvasPlane.intersect(plane.plane)
            ?: return null

        // キャンバスの回転を計算
        val (canvasYaw, canvasPitch) = rayTrace.getCanvasRotation(itemFrame)

        // 2D座標に変換
        val rawUvOrigin = (canvasIntersectLine.origin - canvasLocation.origin)
            .mapToBlockUV(canvasYaw, canvasPitch)
        val rawUvTarget = (canvasIntersectLine.target - canvasLocation.origin)
            .mapToBlockUV(canvasYaw, canvasPitch)
        val segment = Line2d.fromPoints(rawUvOrigin, rawUvTarget)
            .clipBlockUV()
            ?: return null
        // キャンバス内UVを計算、キャンバス範囲外ならばスキップ
        val uvStart = segment.origin.transformUV(itemFrame.rotation)
            ?: return null
        val uvEnd = segment.target.transformUV(itemFrame.rotation)
            ?: return null

        return FramePlaneTraceEntityResult(itemFrame, mapItem, segment, uvStart, uvEnd)
    }

    companion object {
        /**
         * 直線との正方形の線分を計算します
         * @receiver 直線
         * @param range 正方形の半径
         * @return 線分
         */
        @Suppress("LocalVariableName", "FunctionName")
        fun Line2d.clipBlockUV(range: Double = 0.5): Line2d? {
            // 以下の式を使用して交点を計算する
            // https://www.desmos.com/calculator/rqjlphqe2b

            // 座標の計算式
            val A = origin
            val B = target
            fun Y(x: Double) = (B.y - A.y) / (B.x - A.x) * (x - A.x) + A.y
            fun X(y: Double) = (B.x - A.x) / (B.y - A.y) * (y - A.y) + A.x

            // 辺の交点の座標を計算する
            val x0 = X(-range)  // y = -0.5 上辺の交点のx座標
            val x1 = X(range)   // y = 0.5  下辺の交点のx座標
            val y0 = Y(-range)  // x = -0.5 左辺の交点のy座標
            val y1 = Y(range)   // x = 0.5  右辺の交点のy座標

            // 交点をリストにする
            val crossPoints = mutableListOf<Vec2d>()
            if (x0 in -range..range) crossPoints.add(Vec2d(x0, -range))
            if (x1 in -range..range) crossPoints.add(Vec2d(x1, range))
            if (y0 in -range..range) crossPoints.add(Vec2d(-range, y0))
            if (y1 in -range..range) crossPoints.add(Vec2d(range, y1))

            // 交点リストから線分を計算する
            return when (crossPoints.size) {
                1 -> Line2d.fromPoints(crossPoints[0], crossPoints[0])
                2 -> Line2d.fromPoints(crossPoints[0], crossPoints[1])
                else -> null
            }
        }
    }
}