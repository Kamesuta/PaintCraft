package com.kamesuta.paintcraft.palette

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.canvas.paint.PaintTool
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
        val color = event.mapItem.renderer.mapCanvas.getPixel(uv.x, uv.y)
        val oppositeColor = RGBColor.fromMapColor(color).toOpposite().toMapColor()
        // カーソルを描画
        event.mapItem.renderer.mapCanvas.drawCursor(uv.x, uv.y, color, oppositeColor, 3)
        // 現在使用している色を設定
        session.mode.setMapColor(color)
    }
}