package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.map.draw.DrawRect
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.util.TimeWatcher
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapPalette
import java.awt.Color

/**
 * フリーハンドのペンツール
 * @param session セッション
 */
class PaintPencil(override val session: CanvasSession) : PaintTool {
    /** 最後の操作 */
    private var lastEvent: PaintEvent? = null

    /** 最後の操作時刻 */
    private var lastTime = 0L

    /** 操作モード */
    private var drawMode: CanvasActionType? = null

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

        // 描く色
        @Suppress("DEPRECATION")
        val color = MapPalette.matchColor(Color.BLACK)

        // キャンバスに描く
        mapItem.draw {
            when (drawMode) {
                // 描くモードが左クリックの場合
                CanvasActionType.LEFT_CLICK -> {
                    // 全消し
                    g(DrawRect(0, 0, mapSize - 1, mapSize - 1, 0, true))
                }
                // 描くモードが右クリックの場合
                CanvasActionType.RIGHT_CLICK -> {
                    // 描画
                    drawLine(interact, color)
                }
                // その他 (想定外)
                else -> {
                    // 何もしない
                }
            }
        }

        // プレイヤーに描画を通知する
        mapItem.renderer.updatePlayer(interact.player)

        // イベントを保存
        lastEvent = PaintEvent(mapItem, interact)
    }

    override fun tick() {
        // 描いている途中に右クリックが離されたら
        if (lastEvent != null && !isDrawing) {
            // 最後の位置を初期化
            lastEvent = null
        }
    }

    /**
     * 線を描く
     * @param interact インタラクト
     * @param color 描く色
     */
    private fun Drawable.drawLine(
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
}