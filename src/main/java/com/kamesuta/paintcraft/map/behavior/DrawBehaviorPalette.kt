package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.canvas.CanvasDrawingActionType
import com.kamesuta.paintcraft.canvas.CanvasPalette
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.DrawableMapRenderer
import com.kamesuta.paintcraft.map.draw.DrawPalette
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.color.RGBColor.Companion.toRGB

/**
 * パレット
 * @param renderer 描画クラス
 */
class DrawBehaviorPalette(private val renderer: DrawableMapRenderer) : DrawBehavior {
    override fun paint(session: CanvasSession, event: PaintEvent) {
        // UV座標を取得
        val uv = event.interact.uv
        // 現在の色を取得
        val hsb = session.drawing.palette.hsbColor
        // クリック開始時の場合のみ調整中のモードを設定
        if (session.drawing.drawingAction == CanvasDrawingActionType.BEGIN) {
            // 新しい調整モードを取得して置き換える
            val adjustingType = DrawPalette.getAdjustingType(uv.x, uv.y)
            session.drawing.palette.adjustingType = adjustingType

            // 保存パレットの場合は選択中の色を変更
            if (adjustingType == CanvasPalette.AdjustingType.STORED_PALETTE) {
                // 選択した保存パレットのスロット番号を取得
                val paletteIndex = DrawPalette.getStoredPaletteIndex(uv.x)
                // 色を読み込む
                val color = session.drawing.palette.storedPalettes.getOrNull(paletteIndex)
                if (color != null) {
                    // 選択中のスロット番号を変更
                    session.drawing.palette.selectedPaletteIndex = paletteIndex
                    // 色を変更
                    val rgbColor = RGBColor.fromMapColor(color)
                    session.drawing.palette.hsbColor = rgbColor.toHSB()
                    session.drawing.palette.color = color
                }
            }
        }
        // 新しい色を取得
        val color = DrawPalette.getColor(uv.x, uv.y, session.drawing.palette.adjustingType, hsb)
        // 色が変更されている場合のみ色を設定
        if (color != null) {
            session.drawing.palette.hsbColor = color
            session.drawing.palette.color = color.toRGB().toMapColor()

            // 色を保存
            if (session.drawing.palette.selectedPaletteIndex in 0 until session.drawing.palette.storedPalettes.size) {
                session.drawing.palette.storedPalettes[session.drawing.palette.selectedPaletteIndex] =
                    session.drawing.palette.color
            }
        }
        // パレットを描画
        event.mapItem.renderer.g(DrawPalette(session.drawing.palette))
    }

    override fun draw(f: Drawable.() -> Unit) {
    }

    override fun init() {
        // パレットを描画
        renderer.g(DrawPalette(null))
    }
}