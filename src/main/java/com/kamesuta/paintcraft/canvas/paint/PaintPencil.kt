package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.map.draw.DrawRect
import com.kamesuta.paintcraft.map.draw.Drawable
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

        // プレイヤーに描画を通知する
        event.mapItem.renderer.updatePlayer(event.interact.ray.itemFrame.location.origin)
    }

    /**
     * 線を描く
     * @param event 描きこむイベント
     * @param color 描く色
     */
    private fun Drawable.drawLine(
        event: PaintEvent,
        color: Byte
    ) {
        lastEvent?.let { ev ->
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
    }
}