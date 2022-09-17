package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.frame.FrameLocation.Companion.clipBlockUV
import com.kamesuta.paintcraft.frame.FrameLocation.Companion.isUvInMap
import com.kamesuta.paintcraft.frame.FrameLocation.Companion.transformUv
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.fuzzyEq
import com.kamesuta.paintcraft.util.vec.Line2d
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.debug.DebugLocatables.DebugLineType.LINE
import com.kamesuta.paintcraft.util.vec.debug.DebugLocatables.DebugLineType.SEGMENT
import com.kamesuta.paintcraft.util.vec.debug.DebugLocatables.toDebug
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.vec.minus
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.util.Vector
import kotlin.math.abs

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

        /** バブル連鎖探索用の結果格納クラス */
        class SearchResult(
            val parent: SearchResult?,
            val result: FramePlaneTraceResult.FramePlaneTraceEntityResult,
            val prevOrigin: Vector,
        ) {
            /** 中心座標 */
            val origin = maxOf(result.segment.origin, result.segment.target) { a: Vector, b: Vector ->
                a.distanceSquared(prevOrigin).compareTo(b.distanceSquared(prevOrigin))
            }

            /** ゴールに到着済み */
            val isGoal = origin.distanceSquared(goal) fuzzyEq 0.0
        }

        // 終点にたどり着くまで繰り返す
        fun searchAround(currentChain: SearchResult): SearchResult {
            // 現在の座標を更新 (もとの座標からの距離が遠いものを優先)
            val current = currentChain.result
            player.debugLocation {
                locate(DebugLocationType.SEARCH_SEGMENT, current.segment.toDebug(SEGMENT))
                locate(DebugLocationType.SEARCH_LOCATION, currentChain.origin.toDebug())
                locate(
                    DebugLocationType.SEARCH_CANVAS_LINE,
                    current.frameLocation.normal.toDebug(SEGMENT)
                )
            }
            // ゴールからの距離が今の長さ以上なら探索を中止する
            if ((currentChain.parent != null) &&
                (currentChain.origin.distanceSquared(goal) >= currentChain.parent.origin.distanceSquared(goal))
            ) {
                return currentChain
            }

            // 終点までの距離
            if (currentChain.isGoal) {
                // 終点にたどり着いたら終了
                return currentChain
            }

            // 現在の座標から半径radiusの球体の中にあるアイテムフレームを取得
            val results = currentChain.origin.toLocation(player.world)
                .getNearbyEntitiesByType(ItemFrame::class.java, radius)
                .asSequence()
                // その中からアイテムフレームを取得する
                .filter { it.item.type == Material.FILLED_MAP }
                // レイを飛ばす
                .mapNotNull { planeTraceCanvasByEntity(plane, it) }
                // 現在のアイテムフレームは除外
                .filter { it.itemFrame != current.itemFrame }
                // 線がつながっているかチェック
                .filter {
                    // 角度からアイテムフレーム同士が最低限つながる距離を計算 (0°=0.0, 45°=0.7071, 90°=1.0)+α
                    val thresholdDistance =
                        0.01 + 0.1 * (1.0 - abs(currentChain.result.frameLocation.forward.dot(it.frameLocation.forward)))
                    // 終点と次のアイテムフレーム上の始点の距離がthresholdDistance以下ならつながっている
                    currentChain.origin.distanceSquared(it.segment.origin) < thresholdDistance
                            || currentChain.origin.distanceSquared(it.segment.target) < thresholdDistance
                }
                // 裏側のアイテムフレームは除外する
                .filter {
                    // レイ開始時または終了時どちらかの目線の位置から見えているなら除外しない
                    it.frameLocation.forward.dot(it.frameLocation.origin - plane.rayStart.eyeLocation.origin) < 0
                            || it.frameLocation.forward.dot(it.frameLocation.origin - plane.rayEnd.eyeLocation.origin) < 0
                }
                // チェーンをつなぐ
                .map { SearchResult(currentChain, it, currentChain.origin) }
                // 再帰的に探索
                .map { searchAround(it) }
                .toList()

            // ゴールとの距離が近いものを優先
            return results.minByOrNull { it.origin.distanceSquared(goal) } ?: currentChain
        }

        // 始点のアイテムフレーム/座標から探索を開始
        var current: SearchResult? = searchAround(SearchResult(null, start, plane.segment.origin))

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
        // フレーム平面の作成
        val frameLocation = FrameLocation.fromItemFrame(itemFrame, clientType)

        // 面の交線を計算
        val intersectLine = frameLocation.plane.intersect(plane.plane)
            ?: return null
        // 始点と終点の線分を交線にマッピングする
        val intersectSegment = intersectLine.closestSegment(plane.segment)
        player.debugLocation {
            locate(DebugLocationType.INTERSECT_LINE, intersectLine.toDebug(LINE))
            locate(DebugLocationType.INTERSECT_SEGMENT, intersectSegment.toDebug(SEGMENT))
            locate(DebugLocationType.CANVAS_PLANE, frameLocation.toDebug())
            locate(DebugLocationType.INTERSECT_PLANE, plane.plane.toDebug())
        }

        // 線分を2D座標に変換
        val rawUvOrigin = frameLocation.toBlockUv(intersectSegment.origin)
        val rawUvTarget = frameLocation.toBlockUv(intersectSegment.target)
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
        val uvStart = clip.origin.transformUv(rotation)
            .run { if (isUvInMap()) this else return null }
        val uvEnd = clip.target.transformUv(rotation)
            .run { if (isUvInMap()) this else return null }

        // 3D座標に逆変換
        val segment3d = Line3d.fromPoints(
            frameLocation.fromBlockUv(clip.origin),
            frameLocation.fromBlockUv(clip.target),
        )
        player.debugLocation {
            locate(DebugLocationType.INTERSECT_SEGMENT_CANVAS, segment3d.toDebug(SEGMENT))
        }

        return FramePlaneTraceResult.FramePlaneTraceEntityResult(
            itemFrame,
            mapItem,
            frameLocation,
            segment3d,
            uvStart,
            uvEnd
        )
    }
}