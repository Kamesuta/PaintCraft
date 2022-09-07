package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.frame.FramePlane
import com.kamesuta.paintcraft.frame.FramePlaneTrace.planeTraceCanvas
import com.kamesuta.paintcraft.frame.FrameRayTrace
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.map.draw.DrawRect
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Plane3d
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.vec.origin
import org.bukkit.map.MapPalette
import java.awt.Color

/**
 * フリーハンドのペンツール
 * @param session セッション
 */
class PaintPencil(override val session: CanvasSession) : PaintTool {
    /** 最後の座標 */
    private var lastEvent: PaintEvent? = null

    /** 開始時に最後の座標更新 */
    override fun beginPainting(event: PaintEvent) {
        lastEvent = event
    }

    /** 終了時に最後の座標をクリア */
    override fun endPainting() {
        lastEvent = null
    }

    override fun paint(event: PaintEvent) {
        // 描く色
        @Suppress("DEPRECATION")
        val color = MapPalette.matchColor(Color.BLACK)

        // キャンバスに描く
        event.mapItem.draw {
            when (event.drawMode) {
                // 描くモードが左クリックの場合
                CanvasActionType.LEFT_CLICK -> {
                    // 全消し
                    g(DrawRect(0, 0, mapSize - 1, mapSize - 1, 0, true))
                    // クリックを持続させない
                    session.clicking.stopClicking()
                    // 描くのを終了
                    session.drawing.endDrawing()
                }
                // 描くモードが右クリックの場合
                CanvasActionType.RIGHT_CLICK -> {
                    // 描画
                    drawLine(event, color)
                }
                // その他 (想定外)
                else -> {
                    // 何もしない
                }
            }
        }

        // 最後の座標を更新
        lastEvent = event

        // 変更箇所をプレイヤーに送信
        session.drawing.edited.forEach { (itemFrame, drawableMap) ->
            drawableMap.renderer.updatePlayer(itemFrame.location.origin)
        }
    }

    /**
     * 線を描く
     * @param event 描きこむイベント
     * @param color 描く色
     */
    private fun drawLine(
        event: PaintEvent,
        color: Byte
    ) {
        lastEvent?.let { ev ->
            if (event.interact.ray.itemFrame == ev.interact.ray.itemFrame) {
                // マップをプレイヤーへ同期するために記憶しておく
                session.drawing.edited.computeIfAbsent(event.interact.ray.itemFrame) { event.mapItem }
                // アイテムフレームが同じならそのまま書き込む
                event.mapItem.draw {
                    g(
                        DrawLine(
                            ev.interact.uv.x,
                            ev.interact.uv.y,
                            event.interact.uv.x,
                            event.interact.uv.y,
                            color
                        )
                    )
                }
            } else {
                // アイテムフレームが違うなら平面を作成しレイキャストする

                // 平面を作成 (プレイヤーの視線と始点、終点を通る平面)
                val eyeLocation = event.interact.ray.eyeLocation
                val segment = Line3d.fromPoints(
                    ev.interact.ray.canvasIntersectLocation,
                    event.interact.ray.canvasIntersectLocation
                )
                val plane = Plane3d.fromPoints(eyeLocation.origin, segment.origin, segment.target)
                event.interact.player.debugLocation {
                    locate(DebugLocationType.SEGMENT_ORIGIN, segment.origin)
                    locate(DebugLocationType.SEGMENT_TARGET, segment.target)
                }
                val framePlane = FramePlane(plane, eyeLocation, segment, ev.interact.ray, event.interact.ray)

                // 当たり判定
                val rayTrace = FrameRayTrace(event.interact.player, session.clientType)
                val result = rayTrace.planeTraceCanvas(framePlane)

                // 線を描く
                for (entityResult in result.entities) {
                    // マップをプレイヤーへ同期するために記憶しておく
                    session.drawing.edited.computeIfAbsent(entityResult.itemFrame) { entityResult.mapItem }
                    // 線を描く
                    entityResult.mapItem.draw {
                        g(
                            DrawLine(
                                entityResult.uvStart.x,
                                entityResult.uvStart.y,
                                entityResult.uvEnd.x,
                                entityResult.uvEnd.y,
                                color
                            )
                        )
                    }
                }
            }
        }
    }
}