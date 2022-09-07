package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.frame.FrameRayTrace.Companion.mapBlockUvToLocation
import com.kamesuta.paintcraft.frame.FrameRayTrace.Companion.mapToBlockUV
import com.kamesuta.paintcraft.frame.FrameRayTrace.Companion.toCanvasPlane
import com.kamesuta.paintcraft.frame.FrameRayTrace.Companion.transformUV
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.vec.*
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.debugLocation
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.util.Vector

/**
 * キャンバスと面の交差判定をします
 */
object FramePlaneTrace {
    /**
     * キャンバスと平面の交差判定をします
     * @param plane 平面
     * @return ヒットした位置情報
     */
    fun FrameRayTrace.planeTraceCanvas(
        plane: FramePlane,
    ): FramePlaneTraceResult {
        // バブル連鎖探索 (仮)
        // 隣接するアイテムフレームを取得し、レイを飛ばしてヒットしたら、そのアイテムフレームに隣接するアイテムフレームを取得して連鎖する

        // バブルのサイズ (キャンバス中心～キャンバス線分までの距離 + α = 0.5√2 + α ≈ 0.7071 + α ≈ 0.8)
        val radius = 0.8

        // 始点のアイテムフレームを検索
        val start = planeTraceCanvasByEntity(plane, plane.rayStart.itemFrame)
            ?: return FramePlaneTraceResult(plane, listOf())

        // ゴール = 終点の座標
        val goal = plane.segment.target

        // バブル連鎖探索用の結果格納クラス
        class SearchResult(
            val parent: SearchResult?,
            val result: FramePlaneTraceResult.FramePlaneTraceEntityResult,
            val prevOrigin: Vector,
        ) {
            val origin = maxOf(result.segment.origin, result.segment.target) { a: Vector, b: Vector ->
                a.distanceSquared(prevOrigin).compareTo(b.distanceSquared(prevOrigin))
            }
        }

        // 終点にたどり着くまで繰り返す
        fun searchAround(currentChain: SearchResult): SearchResult? {
            // 現在の座標を更新 (もとの座標からの距離が遠いものを優先)
            val current = currentChain.result
            player.debugLocation {
                locate(DebugLocationType.SEARCH_SEGMENT, current.segment.toDebug(Line3d.DebugLineType.SEGMENT))
                locate(DebugLocationType.SEARCH_LOCATION, currentChain.origin)
                locate(
                    DebugLocationType.SEARCH_CANVAS_LINE,
                    current.canvasLocation.toDebug(Line3d.DebugLineType.SEGMENT)
                )
            }
            // ゴールからの距離が今の長さ以上なら探索を中止する
            if ((currentChain.parent != null) &&
                (currentChain.origin.distanceSquared(goal) >= currentChain.parent.origin.distanceSquared(goal))
            ) {
                return null
            }

            // 終点までの距離
            val currentDistance = currentChain.origin.distanceSquared(goal)
            if (currentDistance < 0.01) {
                // 終点にたどり着いたら終了
                return currentChain
            }

            // 現在の座標から半径radiusの球体の中にあるアイテムフレームを取得
            currentChain.origin.toLocation(player.world)
                .getNearbyEntitiesByType(ItemFrame::class.java, radius)
                .asSequence()
                // その中からアイテムフレームを取得する
                .filter { it.item.type == Material.FILLED_MAP }
                // レイを飛ばす
                .mapNotNull { planeTraceCanvasByEntity(plane, it) }
                // 現在のアイテムフレームは除外
                .filter { it.itemFrame != current.itemFrame }
                // 裏側のアイテムフレームは除外する
                .filter {
                    // レイ開始時または終了時どちらかの目線の位置から見えているなら除外しない
                    it.canvasLocation.direction.dot(it.canvasLocation.origin - plane.rayStart.eyeLocation.origin) < 0
                            || it.canvasLocation.direction.dot(it.canvasLocation.origin - plane.rayEnd.eyeLocation.origin) < 0
                }
                .forEach {
                    // 再帰的に探索
                    val resultChain = searchAround(SearchResult(currentChain, it, currentChain.origin))
                    if (resultChain != null) {
                        // 終点にたどり着いたら終了
                        return resultChain
                    }
                }

            return null
        }

        // 始点のアイテムフレーム/座標から探索を開始
        var current = searchAround(SearchResult(null, start, plane.segment.origin))

        // チェーンをたどって結果を取得
        val chain = mutableListOf<FramePlaneTraceResult.FramePlaneTraceEntityResult>()
        while (current != null) {
            chain.add(current.result)
            current = current.parent
        }

        // 結果を返す
        return FramePlaneTraceResult(plane, chain)
    }

    /**
     * 指定されたキャンバスと平面の交差判定をします
     * @param plane 平面
     * @param itemFrame アイテムフレーム
     * @return ヒットした位置情報
     */
    private fun FrameRayTrace.planeTraceCanvasByEntity(
        plane: FramePlane,
        itemFrame: ItemFrame,
    ): FramePlaneTraceResult.FramePlaneTraceEntityResult? {
        // マップデータを取得、ただの地図ならばスキップ
        val mapItem = DrawableMapItem.get(itemFrame.item)
            ?: return null
        // キャンバス平面の位置
        val canvasLocation = toCanvasLocation(itemFrame)
        // キャンバスの平面
        val canvasPlane =
            canvasLocation.toCanvasPlane(itemFrame.isVisible || !clientType.isInvisibleFrameSupported)

        // 面の交線を計算
        val canvasIntersectLine = canvasPlane.intersect(plane.plane)
            ?: return null
        // 始点と終点の線分を交線にマッピングする
        val canvasIntersectSegment = canvasIntersectLine.closestSegment(plane.segment)
        player.debugLocation {
            locate(DebugLocationType.INTERSECT_LINE_ORIGIN, canvasIntersectLine.origin)
            locate(DebugLocationType.INTERSECT_LINE_TARGET, canvasIntersectLine.normalized.target)
            locate(DebugLocationType.INTERSECT_LINE, canvasIntersectLine.toDebug(Line3d.DebugLineType.LINE))
            locate(DebugLocationType.INTERSECT_SEGMENT, canvasIntersectSegment.toDebug(Line3d.DebugLineType.SEGMENT))
            locate(DebugLocationType.CANVAS_PLANE, canvasPlane)
            locate(DebugLocationType.INTERSECT_PLANE, plane.plane)
        }

        // キャンバスの回転を計算
        val (canvasYaw, canvasPitch) = getCanvasRotation(itemFrame)

        // 線分を2D座標に変換
        val rawUvOrigin = (canvasIntersectSegment.origin - canvasLocation.origin)
            .mapToBlockUV(canvasYaw, canvasPitch)
        val rawUvTarget = (canvasIntersectSegment.target - canvasLocation.origin)
            .mapToBlockUV(canvasYaw, canvasPitch)
        // 2Dの線分(未クリップ、キャンバス内の範囲に収まっていない)
        val segment = Line2d.fromPoints(rawUvOrigin, rawUvTarget)
        // キャンバス内の座標に変換
        val clip = segment.clipBlockUV() // キャンバスの正方形内の範囲で線分を取る
            ?.intersectSegment(segment) // 始点、終点のどちらかがキャンバス内にある場合は線分を取る
            ?: return null
        // アイテムフレーム内のマップの向き
        val rotation = when (clientType.isLegacyRotation) {
            false -> FrameRotation.fromRotation(itemFrame.rotation)
            true -> FrameRotation.fromLegacyRotation(itemFrame.rotation)
        }
        // キャンバス内UVを計算、キャンバス範囲外ならばスキップ
        val uvStart = clip.origin.transformUV(rotation, false)
            ?: return null
        val uvEnd = clip.target.transformUV(rotation, false)
            ?: return null

        // 3D座標に逆変換
        val segment3d = Line3d.fromPoints(
            clip.origin.mapBlockUvToLocation(canvasYaw, canvasPitch) + canvasLocation.origin,
            clip.target.mapBlockUvToLocation(canvasYaw, canvasPitch) + canvasLocation.origin,
        )
        player.debugLocation {
            locate(DebugLocationType.INTERSECT_SEGMENT_CANVAS, segment3d.toDebug(Line3d.DebugLineType.SEGMENT))
        }

        return FramePlaneTraceResult.FramePlaneTraceEntityResult(
            itemFrame,
            mapItem,
            canvasLocation,
            segment3d,
            uvStart,
            uvEnd
        )
    }

    /**
     * 直線との正方形の線分を計算します
     * @receiver 直線
     * @param range 正方形の半径
     * @return 線分
     */
    @Suppress("LocalVariableName", "FunctionName")
    private fun Line2d.clipBlockUV(range: Double = 0.5): Line2d? {
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