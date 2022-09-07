package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.frame.FramePlane
import com.kamesuta.paintcraft.frame.FramePlaneTrace.planeTraceCanvas
import com.kamesuta.paintcraft.frame.FrameRayTrace
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.map.draw.DrawRollback
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Plane3d
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.vec.origin
import org.bukkit.entity.ItemFrame
import org.bukkit.map.MapPalette
import java.awt.Color

/**
 * 右クリック2点で線が引けるツール
 * @param session セッション
 */
class PaintLine(override val session: CanvasSession) : PaintTool {
    override fun paint(event: PaintEvent) {
        // 描く色
        @Suppress("DEPRECATION")
        val color = MapPalette.matchColor(Color.BLACK)

        // キャンバスに描く
        when (event.drawMode) {
            // 描くモードが左クリックの場合
            CanvasActionType.LEFT_CLICK -> {
                // 復元 (前回の状態を破棄)
                rollback(rollbackCanvas = true, deleteRollback = true)
            }
            // 描くモードが右クリックの場合
            CanvasActionType.RIGHT_CLICK -> {
                // 復元
                rollback(rollbackCanvas = true, deleteRollback = false)
                // 描画
                drawLine(event, color)
            }
            // その他 (想定外)
            else -> {
                // 何もしない
            }
        }

        // 変更箇所をプレイヤーに送信
        session.drawing.edited.forEach { (itemFrame, drawableMap) ->
            drawableMap.renderer.updatePlayer(itemFrame.location.origin)
        }
    }

    override fun endPainting() {
        // 前回の状態に破棄
        rollback(rollbackCanvas = false, deleteRollback = true)
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
        session.drawing.startEvent?.let { ev ->
            if (event.interact.ray.itemFrame == ev.interact.ray.itemFrame) {
                // 後で戻せるよう記憶しておく
                store(event.interact.ray.itemFrame, event.mapItem)
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
                    // 後で戻せるよう記憶しておく
                    store(entityResult.itemFrame, entityResult.mapItem)
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

    /**
     * 描く前の内容を保存する
     * @param itemFrame アイテムフレーム
     * @param mapItem マップ
     */
    private fun store(itemFrame: ItemFrame, mapItem: DrawableMapItem) {
        session.drawing.edited.computeIfAbsent(itemFrame) {
            // 新たに描いたマップアイテムのみ記憶
            mapItem.renderer.previewBefore = DrawRollback(mapItem.renderer.mapCanvas)
            mapItem
        }
    }

    /**
     * 新たに描いた内容を取り消す
     * @param rollbackCanvas trueならキャンバスを復元する
     * @param deleteRollback 前回の状態を破棄するかどうか
     */
    private fun rollback(rollbackCanvas: Boolean, deleteRollback: Boolean) {
        session.drawing.edited.values.forEach { mapItem ->
            if (rollbackCanvas) {
                // キャンバスを復元
                mapItem.renderer.previewBefore?.let {
                    mapItem.draw {
                        // キャンバスに描く
                        g(it)
                    }
                }
            }
            // 前回の状態を消す
            if (deleteRollback) {
                mapItem.renderer.previewBefore = null
            }
        }
        if (deleteRollback) {
            // 描いた内容の記憶を消す
            session.drawing.edited.clear()
        }
    }
}