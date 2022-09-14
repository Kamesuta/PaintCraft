package com.kamesuta.paintcraft.palette

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.canvas.paint.PaintTool

/**
 * スポイトツール
 * @param session セッション
 * @param callback スポイトが完了した時のコールバック
 */
class PaintColorPicker(
    override val session: CanvasSession,
    private val callback: (Byte) -> Unit
) : PaintTool {
    override fun paint(event: PaintEvent) {
        // UV座標を取得
        val uv = event.interact.uv
        // 選択している場所の色を取得
        val color = event.mapItem.renderer.mapCanvas.getPixel(uv.x, uv.y)

        // コールバックを呼び出す
        callback(color)
    }
}