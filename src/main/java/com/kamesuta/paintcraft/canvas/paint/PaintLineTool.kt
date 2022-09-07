package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.frame.FramePlane
import com.kamesuta.paintcraft.frame.FramePlaneTrace.planeTraceCanvas
import com.kamesuta.paintcraft.frame.FrameRayTrace
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Plane3d
import com.kamesuta.paintcraft.util.vec.Vec2i
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.debugLocation
import org.bukkit.entity.ItemFrame

/**
 * 複数キャンバスをまたいで線を描画するツール
 */
object PaintLineTool {
    /**
     * 線を描く情報
     * @param itemFrame アイテムフレーム
     * @param mapItem マップアイテム
     * @param uvStart 開始座標
     * @param uvEnd 終了座標
     */
    class DrawData(
        val itemFrame: ItemFrame,
        val mapItem: DrawableMapItem,
        val uvStart: Vec2i,
        val uvEnd: Vec2i,
    )

    /**
     * 線を描く
     * @param session セッション
     * @param event 描きこむイベント
     * @param prevEvent 前回の描きこむイベント
     * @param draw 実際に描く処理
     */
    fun drawLine(
        session: CanvasSession,
        event: PaintEvent,
        prevEvent: PaintEvent,
        draw: DrawData.() -> Unit
    ) {
        if (event.interact.ray.itemFrame == prevEvent.interact.ray.itemFrame) {
            // 絵を描く
            draw(DrawData(event.interact.ray.itemFrame, event.mapItem, prevEvent.interact.uv, event.interact.uv))
        } else {
            // アイテムフレームが違うなら平面を作成しレイキャストする

            // 平面を作成 (プレイヤーの視線と始点、終点を通る平面)
            val eyeLocation = event.interact.ray.eyeLocation
            val segment = Line3d.fromPoints(
                prevEvent.interact.ray.canvasIntersectLocation,
                event.interact.ray.canvasIntersectLocation
            )
            val plane = Plane3d.fromPoints(eyeLocation.origin, segment.origin, segment.target)
            event.interact.player.debugLocation {
                locate(DebugLocationType.SEGMENT_ORIGIN, segment.origin)
                locate(DebugLocationType.SEGMENT_TARGET, segment.target)
            }
            val framePlane = FramePlane(plane, eyeLocation, segment, prevEvent.interact.ray, event.interact.ray)

            // 当たり判定
            val rayTrace = FrameRayTrace(event.interact.player, session.clientType)
            val result = rayTrace.planeTraceCanvas(framePlane)

            // 線を描く
            for (entityResult in result.entities) {
                // 線を描く
                draw(DrawData(entityResult.itemFrame, entityResult.mapItem, entityResult.uvStart, entityResult.uvEnd))
            }
        }
    }
}