package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.frame.FrameLocation.Companion.transformUv
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.vec.Line2d
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.vec.minus
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.util.BoundingBox

/**
 * キャンバスと長方形の交差判定をします
 */
object FrameRectTrace {
    /**
     * キャンバスと長方形の交差判定をします
     * @param plane 長方形
     * @return ヒットした位置情報
     */
    fun FrameRayTrace.rectTraceCanvas(
        plane: FramePlane,
    ): FramePlaneTraceResult {
        /*
        // 斜めのキャンバスで矩形を描画するとたしかにおかしな結果になるが、これはこれで面白いのでとりあえずはこれでよしとする
        // → この判定は行わないためコメントアウト
        val facing = plane.rayStart.itemFrame.facing
        if (!facing.isCartesian || facing.direction != plane.rayStart.itemFrame.location.direction) {
            // 斜めを向いている、またはブロックに対して斜めを向いている場合は無視
            return FramePlaneTraceResult(plane, listOf())
        }
        */

        // 範囲 (平面+αの範囲、αの厚み)
        val box = BoundingBox.of(plane.segment.origin, plane.segment.target).expand(0.05)
        val entities = player.world.getNearbyEntities(box.clone().expand(0.3)) { it is ItemFrame }
            .asSequence()
            .mapNotNull { it as? ItemFrame }
            // その中からアイテムフレームを取得する
            .filter { it.item.type == Material.FILLED_MAP }
            // 垂直方向の範囲をチェックする
            .filter {
                // 水平方向以外は無視 (垂直方向の範囲を0にする)
                if (it.facing.modX != 0) {
                    it.location.x in box.minX..box.maxX
                } else if (it.facing.modY != 0) {
                    it.location.y in box.minY..box.maxY
                } else if (it.facing.modZ != 0) {
                    it.location.z in box.minZ..box.maxZ
                } else {
                    true
                }
            }
            // レイを飛ばす
            .mapNotNull { rectTraceCanvasByEntity(plane, it) }
            // 裏側のアイテムフレームは除外する
            .filter {
                // レイ開始時または終了時どちらかの目線の位置から見えているなら除外しない
                it.frameLocation.forward.dot(it.frameLocation.origin - plane.rayStart.eyeLocation.origin) < 0
                        || it.frameLocation.forward.dot(it.frameLocation.origin - plane.rayEnd.eyeLocation.origin) < 0
            }
            .toList()

        // 結果を返す
        return FramePlaneTraceResult(plane, entities)
    }

    /**
     * 指定されたキャンバスと長方形の交差判定をします
     * @param plane 長方形
     * @param itemFrame アイテムフレーム
     * @return ヒットした位置情報
     */
    private fun FrameRayTrace.rectTraceCanvasByEntity(
        plane: FramePlane,
        itemFrame: ItemFrame,
    ): FramePlaneTraceResult.FramePlaneTraceEntityResult? {
        // マップデータを取得、ただの地図ならばスキップ
        val mapItem = DrawableMapItem.get(itemFrame.item)
            ?: return null
        // フレーム平面の作成
        val frameLocation = FrameLocation.fromItemFrame(itemFrame, clientType)

        // 長方形始点と終点
        val rectSegment = plane.segment

        // 線分を2D座標に変換
        val rawUvOrigin = frameLocation.toBlockUv(rectSegment.origin)
        val rawUvTarget = frameLocation.toBlockUv(rectSegment.target)
        // 2Dの線分(未クリップ、キャンバス内の範囲に収まっていない)
        val segment = Line2d.fromPoints(rawUvOrigin, rawUvTarget)
        // アイテムフレーム内のマップの向き
        val rotation = when (clientType.isLegacyRotation) {
            false -> FrameRotation.fromRotation(itemFrame.rotation)
            true -> FrameRotation.fromLegacyRotation(itemFrame.rotation)
        }
        // キャンバス内UVを計算、キャンバス範囲外ならば範囲内に納める
        val uvStart = segment.origin.transformUv(rotation)
        //.clampUvInMap() // これは必要ない: DrawRectは範囲外に対応している
        val uvEnd = segment.target.transformUv(rotation)
        //.clampUvInMap() // これは必要ない: DrawRectは範囲外に対応している

        // 3D座標に逆変換
        val segment3d = Line3d.fromPoints(
            frameLocation.fromBlockUv(segment.origin),
            frameLocation.fromBlockUv(segment.target),
        )
        player.debugLocation {
            locate(DebugLocationType.INTERSECT_SEGMENT_CANVAS, segment3d.toDebug(Line3d.DebugLineType.SEGMENT))
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