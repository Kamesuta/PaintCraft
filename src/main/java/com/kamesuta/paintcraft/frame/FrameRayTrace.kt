package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.frame.FrameLocation.Companion.isUvInMap
import com.kamesuta.paintcraft.frame.FrameLocation.Companion.transformUv
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.clienttype.ClientType
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.debugLocation
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox

/**
 * キャンバスと目線の交差判定をし、UVを計算します
 * @param player プレイヤー
 * @param getClientType クライアントの種類を取得
 */
class FrameRayTrace(
    val player: Player,
    val getClientType: () -> ClientType
) {
    /** クライアントの種類 */
    val clientType: ClientType
        get() = getClientType()

    /**
     * レイを飛ばしてアイテムフレームを取得
     * @param eyeLocation プレイヤーの目線の位置
     * @return 交差した位置情報
     */
    fun rayTraceCanvas(
        eyeLocation: Line3d,
    ): FrameRayTraceResult? {
        // 目線と向きからエンティティを取得し、アイテムフレームかどうかを確認する
        player.debugLocation {
            locate(DebugLocationType.EYE_LOCATION, eyeLocation.origin)
            locate(DebugLocationType.EYE_DIRECTION, eyeLocation.target)
            locate(DebugLocationType.EYE_LINE, eyeLocation.toDebug(Line3d.DebugLineType.DIRECTION))
        }

        // 距離は前方8m(半径4)を範囲にする
        val distance = 8.0
        // 範囲を全方向にmarginずつ拡張
        val margin = 1.0
        // エンティティを取得する範囲のバウンディングボックス
        val box = BoundingBox.of(eyeLocation.origin, 0.0, 0.0, 0.0).expand(eyeLocation.direction, distance)
        // レイキャストを行い、ヒットしたブロックがあればそのブロック座標と目線の位置から範囲の中心座標とサイズを計算する
        val blockRay = player.world.rayTraceBlocks(
            eyeLocation.origin.toLocation(player.world),
            eyeLocation.direction,
            distance + margin
        )
        // クリックがヒットした座標
        val blockHitLocation = blockRay?.hitPosition
        player.debugLocation {
            locate(DebugLocationType.BLOCK_HIT_LOCATION, blockHitLocation)
        }

        // キャンバスよりも手前にブロックがあるならば探索終了
        val maxDistance = (blockHitLocation?.distance(eyeLocation.origin) ?: distance)

        // 範囲内にあるすべてのアイテムフレームを取得する
        val result = player.world.getNearbyEntities(box.clone().expand(margin)) { it is ItemFrame }
            .asSequence()
            .map { it as ItemFrame }
            // その中からアイテムフレームを取得する
            .filter { it.item.type == Material.FILLED_MAP }
            // レイを飛ばす
            .mapNotNull { rayTraceCanvasByEntity(eyeLocation, it, false) }
            .filter { it.canvasIntersectLocation.distanceSquared(eyeLocation.origin) <= maxDistance * maxDistance }
            // 一番近いヒットしたキャンバス
            .minByOrNull {
                // 距離の2条で比較する
                it.canvasIntersectLocation.distanceSquared(eyeLocation.origin)
            }
            ?: return null

        // 最大距離より遠い場合は除外 (ブロックより後ろのアイテムフレームは除外)
        if (result.canvasIntersectLocation.distanceSquared(eyeLocation.origin) > maxDistance * maxDistance) {
            return null
        }

        return result
    }

    /**
     * 指定されたアイテムフレームにレイを飛ばして一致する場合は取得
     * @param eyeLocation プレイヤーの目線の位置
     * @param itemFrame アイテムフレーム
     * @param missHit レイがヒットしなかった場合を含めるかどうか
     * @return 交差した位置情報
     */
    fun rayTraceCanvasByEntity(
        eyeLocation: Line3d,
        itemFrame: ItemFrame,
        missHit: Boolean,
    ): FrameRayTraceResult? {
        // マップデータを取得、ただの地図ならばスキップ
        val mapItem = DrawableMapItem.get(itemFrame.item)
            ?: return null
        // フレーム平面の作成
        val frameLocation = FrameLocation.fromItemFrame(itemFrame, clientType)
        player.debugLocation {
            // アイテムフレームの位置
            locate(DebugLocationType.CANVAS_LOCATION, frameLocation.location.origin)
            // アイテムフレームの正面ベクトル
            locate(DebugLocationType.CANVAS_DIRECTION, frameLocation.location.target)
        }

        // キャンバスのオフセットを計算
        val intersectLocation = frameLocation.plane
            .intersect(eyeLocation)
            ?: return null
        // アイテムフレーム内のマップの向き
        val rotation = when (clientType.isLegacyRotation) {
            false -> FrameRotation.fromRotation(itemFrame.rotation)
            true -> FrameRotation.fromLegacyRotation(itemFrame.rotation)
        }
        // UVに変換 → キャンバス内UVを計算、キャンバス範囲外ならばスキップ
        val uv = frameLocation.toBlockUv(intersectLocation)
            // キャンバス内UV(0～127)を計算、キャンバス範囲外ならばスキップ
            .transformUv(rotation)
            .run { if (missHit || isUvInMap()) this else return null }
        // レイの結果を返す
        return FrameRayTraceResult(itemFrame, mapItem, eyeLocation, frameLocation.location, intersectLocation, uv)
    }
}