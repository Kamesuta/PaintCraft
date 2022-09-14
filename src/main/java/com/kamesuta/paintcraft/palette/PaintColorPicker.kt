package com.kamesuta.paintcraft.palette

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.canvas.paint.PaintTool

/**
 * スポイトツール
 * @param session セッション
 */
class PaintColorPicker(
    override val session: CanvasSession,
) : PaintTool {
    override fun paint(event: PaintEvent) {
        // UV座標を取得
        val uv = event.interact.uv
        // 選択している場所の色を設定
        session.mode.setMapColor(event.mapItem.renderer.mapCanvas.getPixel(uv.x, uv.y))
    }
}