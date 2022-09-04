package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.frame.FramePlane
import com.kamesuta.paintcraft.frame.FramePlaneTrace.planeTraceCanvas
import com.kamesuta.paintcraft.frame.FrameRayTrace
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.map.draw.DrawRollback
import com.kamesuta.paintcraft.util.TimeWatcher
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.Plane3d
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.debugLocation
import org.bukkit.entity.ItemFrame
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

    /** 編集したマップアイテム */
    private val edited = mutableMapOf<ItemFrame, DrawableMapItem>()

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
                rollback(rollbackCanvas = true, deleteRollback = true)
                // 右クリックが離されるまで描くのを停止する
                stopDraw = true
            }
            // 描くモードが右クリックの場合
            CanvasActionType.RIGHT_CLICK -> {
                // 復元
                rollback(rollbackCanvas = true, deleteRollback = false)
                // 描画
                drawLine(mapItem, interact, color)
            }
            // その他 (想定外)
            else -> {
                // 何もしない
            }
        }

        // 変更箇所をプレイヤーに送信
        edited.values.forEach {
            it.renderer.updatePlayer(interact.player)
        }

        // クリックを開始した場合
        if (lastEvent == null) {
            // イベントを保存
            lastEvent = PaintEvent(mapItem, interact)
        }
    }

    override fun tick() {
        // 描いている途中に右クリックが離されたら
        if (!isDrawing) {
            lastEvent?.let {
                // 前回の状態に破棄
                rollback(rollbackCanvas = false, deleteRollback = true)
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
                // 後で戻せるよう記憶しておく
                store(interact.ray.itemFrame, mapItem)
                // アイテムフレームが同じならそのまま書き込む
                mapItem.draw {
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
                val eyeLocation = interact.ray.eyeLocation
                val segment = Line3d.fromPoints(
                    ev.interact.ray.canvasIntersectLocation,
                    interact.ray.canvasIntersectLocation
                )
                val plane = Plane3d.fromPoints(eyeLocation.origin, segment.origin, segment.target)
                interact.player.debugLocation {
                    locate(DebugLocationType.SEGMENT_ORIGIN, segment.origin)
                    locate(DebugLocationType.SEGMENT_TARGET, segment.target)
                }
                val framePlane = FramePlane(plane, eyeLocation, segment)

                // 当たり判定
                val rayTrace = FrameRayTrace(interact.player, session.clientType)
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
        edited.computeIfAbsent(itemFrame) {
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
        edited.values.forEach { mapItem ->
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
            edited.clear()
        }
    }
}