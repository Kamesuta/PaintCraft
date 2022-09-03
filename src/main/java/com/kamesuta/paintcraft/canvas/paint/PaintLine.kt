package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.map.draw.DrawRollback
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.util.TimeWatcher
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

    /** 前回の状態 */
    private var previewBefore: DrawRollback? = null

    /** 描くのを止める */
    private var stopDraw = false

    /** 描いているか */
    override val isDrawing: Boolean
        get() = session.clientType.drawDuration.isInTime(lastTime)

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
        mapItem.draw { g ->
            when (drawMode) {
                // 描くモードが左クリックの場合
                CanvasActionType.LEFT_CLICK -> {
                    // 復元 (前回の状態を破棄)
                    rollback(g, true)
                    // 右クリックが離されるまで描くのを停止する
                    stopDraw = true
                }
                // 描くモードが右クリックの場合
                CanvasActionType.RIGHT_CLICK -> {
                    // 復元
                    rollback(g, false)
                    // 描画
                    drawLine(g, interact, color)
                }
                // その他 (想定外)
                else -> {
                    // 何もしない
                }
            }
        }

        // クリックを開始した場合
        if (lastEvent == null) {
            // キャンバスが初期化できている場合のみ
            mapItem.renderer.canvas?.let {
                // 復元地点を保存
                previewBefore = DrawRollback(it)
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
                previewBefore = null
                // 最後の位置を初期化
                lastEvent = null
                // 描くのを再開する
                stopDraw = false
            }
        }
    }

    /**
     * 線を描く
     * @param g 描画する対象
     * @param interact インタラクト
     * @param color 描く色
     */
    private fun drawLine(
        g: Drawable,
        interact: CanvasInteraction,
        color: Byte
    ) {
        lastEvent?.let { ev ->
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
    }

    /**
     * 新たに書いた内容を取り消す
     * @param g 描画する対象
     * @param deleteRollback 前回の状態を破棄するかどうか
     */
    private fun rollback(g: Drawable, deleteRollback: Boolean) {
        previewBefore?.let {
            // キャンバスに描く
            g(it)
        }
        // 前回の状態を消す
        if (deleteRollback) {
            previewBefore = null
        }
    }
}