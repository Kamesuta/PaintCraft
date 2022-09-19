package com.kamesuta.paintcraft.palette

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.canvas.paint.PaintTool
import com.kamesuta.paintcraft.map.image.mapSize
import com.kamesuta.paintcraft.palette.DrawPalette.Companion.drawCursor
import com.kamesuta.paintcraft.util.color.RGBColor

/**
 * スポイトツール
 * @param session セッション
 */
class PaintColorPicker(
    override val session: CanvasSession,
) : PaintTool {
    override fun endPainting() {
        // カーソルは終わったら非表示にする
        session.drawing.edited.editing.rollback()
    }

    override fun paint(event: PaintEvent) {
        // UV座標を取得
        val uv = event.interact.uv
        // カーソルを消す
        session.drawing.edited.editing.rollback()
        session.drawing.edited.store(event.interact.ray.itemFrame, event.mapItem)
        // 選択している場所の色を取得
        val color = event.mapItem.renderer.mapCanvas[uv.x, uv.y]
        val oppositeColor = RGBColor.fromMapColor(color).toOpposite().toMapColor()
        // カーソルを描画
        event.mapItem.renderer.mapCanvas.drawCursor(uv.x, uv.y, color, oppositeColor)
        // 固定位置のカーソルを描画
        if (uv.x < CURSOR_THRESHOLD && uv.y < CURSOR_THRESHOLD) {
            // カーソルが左上にある場合は右上にカーソルを描画
            event.mapItem.renderer.mapCanvas.drawCursor(mapSize - CURSOR_OFFSET, CURSOR_OFFSET, color, oppositeColor, 7)
        } else {
            // その他は左上にカーソルを描画
            event.mapItem.renderer.mapCanvas.drawCursor(CURSOR_OFFSET, CURSOR_OFFSET, color, oppositeColor, 7)
        }
        // 現在使用している色を設定
        session.mode.setMapColor(color)
    }

    companion object {
        /** カーソル描画オフセット */
        private const val CURSOR_OFFSET = 32

        /** カーソルを別の位置に動かすしきい値 */
        private const val CURSOR_THRESHOLD = 64
    }
}