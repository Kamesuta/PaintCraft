package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.frame.FramePlane
import com.kamesuta.paintcraft.frame.FramePlaneTrace
import com.kamesuta.paintcraft.frame.FrameRayTrace
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.map.draw.DrawRollback
import com.kamesuta.paintcraft.util.DebugLocationType
import com.kamesuta.paintcraft.util.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.TimeWatcher
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Line3d.Companion.toLine
import com.kamesuta.paintcraft.util.vec.Plane3d
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapPalette
import java.awt.Color

/**
 * 右クリック2点で線が引けるツール
 * @param session セッション
 */
class PaintLine(override val session: CanvasSession) : PaintTool {
    /** 最後の操作 */
    private var lastEvent: PaintEvent? = null

    /** 最後の操作時刻 */
    private var lastTime = 0L

    /** 操作モード */
    private var drawMode: CanvasActionType? = null

    /** 描くのを止める */
    private var stopDraw = false

    /** 描いているか */
    override val isDrawing: Boolean
        get() = session.clientType.threshold.drawDuration.isInTime(lastTime)

    override fun paint(
        itemStack: ItemStack,
        mapItem: DrawableMapItem,
        interact: CanvasInteraction,
    ) {
        // Tickイベント
        tick()

        // 左右クリックした場合は状態を更新する
        if (interact.actionType != CanvasActionType.MOUSE_MOVE) {
            lastTime = TimeWatcher.now
            // 現在描いているモードを保存
            drawMode = interact.actionType
        }

        // 描くのを止めている場合は何もしない
        if (stopDraw) {
            return
        }

        // 描く色
        @Suppress("DEPRECATION")
        val color = MapPalette.matchColor(Color.BLACK)

        // キャンバスに描く
        when (drawMode) {
            // 描くモードが左クリックの場合
            CanvasActionType.LEFT_CLICK -> {
                // 復元 (前回の状態を破棄)
                rollback(mapItem, true)
                // 右クリックが離されるまで描くのを停止する
                stopDraw = true
            }
            // 描くモードが右クリックの場合
            CanvasActionType.RIGHT_CLICK -> {
                // 復元
                rollback(mapItem, false)
                // 描画
                drawLine(mapItem, interact, color)
            }
            // その他 (想定外)
            else -> {
                // 何もしない
            }
        }

        // クリックを開始した場合
        if (lastEvent == null) {
            // キャンバスが初期化できている場合のみ
            mapItem.renderer.canvas?.let {
                // 復元地点を保存
                mapItem.renderer.previewBefore = DrawRollback(it)
                // イベントを保存
                lastEvent = PaintEvent(mapItem, interact)
            }
        }
    }

    override fun tick() {
        // 描いている途中に右クリックが離されたら
        if (!isDrawing) {
            lastEvent?.let {
                // 前回の状態に破棄
                it.mapItem.renderer.previewBefore = null
                // 最後の位置を初期化
                lastEvent = null
                // 描くのを再開する
                stopDraw = false
            }
        }
    }

    /**
     * 線を描く
     * @param interact インタラクト
     * @param color 描く色
     */
    private fun drawLine(
        mapItem: DrawableMapItem,
        interact: CanvasInteraction,
        color: Byte
    ) {
        lastEvent?.let { ev ->
            if (interact.ray.itemFrame == ev.interact.ray.itemFrame) {
                // アイテムフレームが同じならそのまま書き込む
                mapItem.draw { g ->
                    g(
                        DrawLine(
                            ev.interact.uv.x,
                            ev.interact.uv.y,
                            interact.uv.x,
                            interact.uv.y,
                            color
                        )
                    )
                }
            } else {
                // アイテムフレームが違うなら平面を作成しレイキャストする

                // 平面を作成 (プレイヤーの視線と始点、終点を通る平面)
                val eyeLocation = interact.player.eyeLocation.toLine()
                val segment = Line3d.fromPoints(
                    ev.interact.ray.canvasIntersectLocation,
                    interact.ray.canvasIntersectLocation
                )
                val plane = Plane3d.fromPoints(eyeLocation.origin, segment.origin, segment.target)
                interact.player.debugLocation { locate ->
                    locate(DebugLocationType.SEGMENT_ORIGIN, segment.origin)
                    locate(DebugLocationType.SEGMENT_TARGET, segment.target)
                }
                val framePlane = FramePlane(plane, eyeLocation, segment)

                // 当たり判定
                val rayTrace = FrameRayTrace(interact.player, session.clientType)
                val planeTrace = FramePlaneTrace(rayTrace)
                val result = planeTrace.planeTraceCanvas(framePlane)

                // 線を描く
                for (entityResult in result.entities) {
                    entityResult.mapItem.draw { g ->
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
     * 新たに書いた内容を取り消す
     * @param mapItem マップアイテム
     * @param deleteRollback 前回の状態を破棄するかどうか
     */
    private fun rollback(mapItem: DrawableMapItem, deleteRollback: Boolean) {
        mapItem.renderer.previewBefore?.let {
            mapItem.draw { g ->
                // キャンバスに描く
                g(it)
            }
        }
        // 前回の状態を消す
        if (deleteRollback) {
            mapItem.renderer.previewBefore = null
        }
    }
}