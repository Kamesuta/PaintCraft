package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.map.draw.DrawFill

/**
 * 塗りつぶしツール
 * @param session セッション
 */
class PaintFill(override val session: CanvasSession) : PaintTool {
    override fun paint(event: PaintEvent) {
        // 描く色
        val color = session.mode.mapColor

        // キャンバスに描く
        event.mapItem.draw(event.interact.player) {
            when (event.drawMode) {
                // 描くモードが左クリックの場合
                CanvasActionType.LEFT_CLICK -> {
                    // マップをプレイヤーへ同期するために記憶しておく
                    session.drawing.edited.store(event.interact.player, event.interact.ray.itemFrame, event.mapItem)
                    // 塗りつぶす
                    g(DrawFill(event.interact.uv.x, event.interact.uv.y, color))
                    // クリックを持続させない
                    session.clicking.stopClicking()
                    // 描くのを終了
                    session.drawing.endDrawing()
                }
                // 描くモードが右クリックの場合
                CanvasActionType.RIGHT_CLICK -> {
                    // 後で戻せるよう記憶しておく
                    session.drawing.edited.store(event.interact.player, event.interact.ray.itemFrame, event.mapItem)
                    // 塗りつぶす
                    g(DrawFill(event.interact.uv.x, event.interact.uv.y, color))
                }
                // その他 (想定外)
                else -> {
                    // 何もしない
                }
            }
        }
    }
}