package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.frame.FrameLocation.Companion.transformUv
import com.kamesuta.paintcraft.util.fuzzyEq
import com.kamesuta.paintcraft.util.vec.Line2d
import com.kamesuta.paintcraft.util.vec.Rect3d

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
        val box = Rect3d.of(plane.segment.origin, plane.segment.target).expand(0.05)
        val entities = player.world.getFrameEntities(box.expand(0.5))
            .asSequence()
            // 垂直方向の範囲をチェックする
            .filter {
                // 水平方向以外は無視 (垂直方向の範囲を0にする)
                val blockLocation = it.blockLocation
                if (blockLocation.direction.x fuzzyEq 0.0) {
                    it.location.origin.x in box.min.x..box.max.x
                } else if (blockLocation.direction.y fuzzyEq 0.0) {
                    it.location.origin.y in box.min.y..box.max.y
                } else if (blockLocation.direction.z fuzzyEq 0.0) {
                    it.location.origin.z in box.min.z..box.max.z
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
        itemFrame: FrameEntity,
    ): FramePlaneTraceResult.EntityResult? {
        // マップデータを取得、ただの地図ならばスキップ
        val mapItem = itemFrame.toDrawableMapItem()
            ?: return null
        // フレーム平面の作成
        val frameLocation = itemFrame.toFrameLocation(clientType)

        // 長方形始点と終点
        val rectSegment = plane.segment

        // 線分を2D座標に変換
        val rawUvOrigin = frameLocation.toBlockUv(rectSegment.origin)
        val rawUvTarget = frameLocation.toBlockUv(rectSegment.target)
        // 2Dの線分(未クリップ、キャンバス内の範囲に収まっていない)
        val segment = Line2d.fromPoints(rawUvOrigin, rawUvTarget)
        // アイテムフレーム内のマップの向き
        val rotation = itemFrame.getFrameRotation(clientType)
        // キャンバス内UVを計算、キャンバス範囲外ならば範囲内に納める
        val uvStart = segment.origin.transformUv(rotation)
        val uvEnd = segment.target.transformUv(rotation)

        return FramePlaneTraceResult.EntityResult(
            itemFrame,
            mapItem,
            frameLocation,
            uvStart,
            uvEnd
        )
    }
}