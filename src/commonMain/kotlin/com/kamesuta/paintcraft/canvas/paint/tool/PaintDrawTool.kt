package com.kamesuta.paintcraft.canvas.paint.tool

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.frame.FrameEntity
import com.kamesuta.paintcraft.frame.FrameLocation.Companion.isUvInMap
import com.kamesuta.paintcraft.frame.FramePlane
import com.kamesuta.paintcraft.frame.FramePlaneTrace.planeTraceCanvas
import com.kamesuta.paintcraft.frame.FramePlaneTraceResult
import com.kamesuta.paintcraft.frame.FrameRayTrace
import com.kamesuta.paintcraft.frame.FrameRectTrace.rectTraceCanvas
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Plane3d
import com.kamesuta.paintcraft.util.vec.debug.DebugLocatables.DebugLineType.SEGMENT
import com.kamesuta.paintcraft.util.vec.debug.DebugLocatables.toDebug
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType

/**
 * 複数キャンバスをまたいで線を描画するツール
 */
object PaintDrawTool {
    /**
     * 線を描く
     * @param session セッション
     * @param event 描きこむイベント
     * @param prevEvent 前回の描きこむイベント
     * @param entities 交差判定をするエンティティ
     * @param draw 実際に描く処理
     */
    fun drawLine(
        session: CanvasSession,
        event: PaintEvent,
        prevEvent: PaintEvent,
        entities: Collection<FrameEntity>,
        draw: PaintDrawData.() -> Unit,
    ) {
        drawRaycast(session, event, prevEvent, draw) { planeTraceCanvas(it, entities) }
    }

    /**
     * 矩形を描く
     * @param session セッション
     * @param event 描きこむイベント
     * @param prevEvent 前回の描きこむイベント
     * @param draw 実際に描く処理
     */
    fun drawRect(
        session: CanvasSession,
        event: PaintEvent,
        prevEvent: PaintEvent,
        draw: PaintDrawData.() -> Unit,
    ) {
        drawRaycast(session, event, prevEvent, draw) { rectTraceCanvas(it) }
    }

    /**
     * レイキャストして描く
     * @param session セッション
     * @param event 描きこむイベント
     * @param prevEvent 前回の描きこむイベント
     * @param draw 実際に描く処理
     * @param raycast レイキャストする処理
     */
    private fun drawRaycast(
        session: CanvasSession,
        event: PaintEvent,
        prevEvent: PaintEvent,
        draw: PaintDrawData.() -> Unit,
        raycast: FrameRayTrace.(FramePlane) -> FramePlaneTraceResult,
    ) {
        if (event.interact.ray.itemFrame == prevEvent.interact.ray.itemFrame
            // 現在キャンバス範囲外をクリックしている状態 (範囲内→範囲外へのD&D)
            && event.interact.ray.isHit
            // 縁に近い場所をクリックしている状態
            && (event.interact.ray.uv.isUvInMap(-session.mode.thickness * 2.0)
                    || prevEvent.interact.ray.uv.isUvInMap(-session.mode.thickness * 2.0))
        ) {
            // アイテムフレームが同じならそのまま描く
            draw(
                PaintDrawData(
                    event.interact.ray.itemFrame,
                    event.mapItem,
                    prevEvent.interact.uv,
                    event.interact.uv,
                    null
                )
            )
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
                locate(DebugLocationType.SEGMENT_LINE, segment.toDebug(SEGMENT))
            }
            val framePlane = FramePlane(plane, eyeLocation, segment, prevEvent.interact.ray, event.interact.ray)

            // 当たり判定
            val result = session.rayTrace.raycast(framePlane)

            // 線を描く
            for (entityResult in result.entities) {
                // 線を描く
                draw(
                    PaintDrawData(
                        entityResult.itemFrame,
                        entityResult.mapItem,
                        entityResult.uvStart,
                        entityResult.uvEnd,
                        result,
                    )
                )
            }
        }
    }
}