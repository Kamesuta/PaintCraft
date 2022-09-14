package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.tool.PaintDrawTool
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.map.draw.DrawRect

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
        val color = session.mode.mapColor

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
                    // 線を描く
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
        // 最後の点+現在の点を結ぶ線を描く
        lastEvent?.let {
            // 描画
            PaintDrawTool.drawLine(session, event, it) {
                // マップをプレイヤーへ同期するために記憶しておく
                session.drawing.edited.store(itemFrame, mapItem)
                // 線を描く
                mapItem.draw {
                    g(
                        DrawLine(
                            uvStart.x,
                            uvStart.y,
                            uvEnd.x,
                            uvEnd.y,
                            color
                        )
                    )
                }
            }
        }
    }
}