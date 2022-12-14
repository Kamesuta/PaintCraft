package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.frame.FrameLocation.Companion.clipBlockUV
import com.kamesuta.paintcraft.frame.FrameLocation.Companion.transformUv
import com.kamesuta.paintcraft.map.image.mapSize
import com.kamesuta.paintcraft.util.vec.Line2d
import com.kamesuta.paintcraft.util.vec.debug.DebugLocatables.DebugLineType.LINE
import com.kamesuta.paintcraft.util.vec.debug.DebugLocatables.DebugLineType.SEGMENT
import com.kamesuta.paintcraft.util.vec.debug.DebugLocatables.toDebug
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType

/**
 * キャンバスと面の交差判定をします
 */
object FramePlaneTrace {
    /**
     * キャンバスと平面の交差判定をします
     * @param plane 平面
     * @param entities 交差判定をするエンティティ
     * @return ヒットした位置情報
     */
    fun FrameRayTrace.planeTraceCanvas(
        plane: FramePlane,
        entities: Collection<FrameEntity>,
    ): FramePlaneTraceResult {
        // 既になぞったキャンバスからヒットした位置を取得
        val results = entities
            .mapNotNull { planeTraceCanvasByEntity(plane, it) }

        // 結果を返す
        return FramePlaneTraceResult(plane, results)
    }

    /**
     * 指定されたキャンバスと平面の交差判定をします
     * @param plane 平面
     * @param itemFrame アイテムフレーム
     * @return ヒットした位置情報
     */
    private fun FrameRayTrace.planeTraceCanvasByEntity(
        plane: FramePlane,
        itemFrame: FrameEntity,
    ): FramePlaneTraceResult.EntityResult? {
        // マップデータを取得、ただの地図ならばスキップ
        val mapItem = itemFrame.toDrawableMapItem()
            ?: return null
        // フレーム平面の作成
        val frameLocation = itemFrame.toFrameLocation(clientType)

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
        val clip = segment.clipBlockUV(0.5 + mode.thickness / mapSize) // キャンバスの正方形内の範囲で線分を取る
            ?.intersectSegment(segment) // 始点、終点のどちらかがキャンバス内にある場合は線分を取る
            ?: return null
        // アイテムフレーム内のマップの向き
        val rotation = itemFrame.getFrameRotation(clientType)
        // キャンバス内UVを計算、キャンバス範囲外ならばスキップ
        val uvStart = clip.origin.transformUv(rotation)
        val uvEnd = clip.target.transformUv(rotation)

        return FramePlaneTraceResult.EntityResult(
            itemFrame,
            mapItem,
            frameLocation,
            uvStart,
            uvEnd
        )
    }
}