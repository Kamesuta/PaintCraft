package com.kamesuta.paintcraft.palette

import com.kamesuta.paintcraft.canvas.CanvasDrawingActionType
import com.kamesuta.paintcraft.canvas.CanvasMode
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.DrawableMapRenderer
import com.kamesuta.paintcraft.map.behavior.DrawBehavior
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.palette.DrawPalette.Companion.loadPalette
import com.kamesuta.paintcraft.util.color.HSBColor
import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.color.RGBColor.Companion.toRGB
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.transparent

/**
 * パレット
 * @param renderer 描画クラス
 */
class DrawBehaviorPalette(private val renderer: DrawableMapRenderer) : DrawBehavior {
    /** パレットに保存されるデータ */
    private val paletteData = PaletteData()

    override fun paint(session: CanvasSession, event: PaintEvent) {
        // UV座標を取得
        val uv = event.interact.uv
        // 現在の色を取得
        val hsb = session.mode.hsbColor
        // クリック開始時の場合のみ調整中のモードを設定
        if (session.drawing.drawingAction == CanvasDrawingActionType.BEGIN) {
            // 新しい調整モードを取得して置き換える
            paletteData.adjustingType = DrawPalette.getAdjustingType(uv.x, uv.y)

            // 保存パレットの場合は選択中の色を変更
            when (paletteData.adjustingType) {
                // パレットを選択した場合
                PaletteAdjustingType.STORED_PALETTE -> {
                    // 選択した保存パレットのスロット番号を取得
                    val paletteIndex = DrawPalette.getStoredPaletteIndex(uv.y)
                    // 色を読み込む
                    val color = paletteData.storedPalettes.getOrNull(paletteIndex)
                    if (color != null) {
                        // 選択中のスロット番号を変更
                        paletteData.selectedPaletteIndex = paletteIndex
                        // 色を変更
                        val rgbColor = RGBColor.fromMapColor(color)
                        session.mode.hsbColor = rgbColor.toHSB()
                        session.mode.color = color
                    }
                }

                // 透明ボタンを選択した場合
                PaletteAdjustingType.TRANSPARENT_COLOR -> {
                    // 透明ボタンを押した場合は色を透明にする
                    session.mode.color = 0
                }

                // カラーピッカーボタンを選択した場合
                PaletteAdjustingType.COLOR_PICKER_COLOR -> {
                    // 既にカラーピッカーの場合は何もしない
                    if (session.mode.tool !is PaintColorPicker) {
                        // カラーピッカーツールに変更
                        val prevTool = session.mode.tool
                        session.mode.tool = PaintColorPicker(session) {
                            // カラーピッカーを終了した場合は元のツールに戻す
                            session.mode.tool = prevTool
                            // カラーピッカーで選択した色を設定
                            session.mode.applyColor(null, it)
                            // パレットを描画
                            event.mapItem.renderer.g(DrawPalette(paletteData, session.mode))
                        }
                    }
                }

                else -> {}
            }
        }
        // 新しい色を取得
        val color = DrawPalette.getColor(uv.x, uv.y, paletteData.adjustingType, hsb)
        // 色が変更されている場合のみ色を設定
        if (color != null) {
            session.mode.applyColor(color, null)
        }
        // パレットを描画
        event.mapItem.renderer.g(DrawPalette(paletteData, session.mode))
    }

    /**
     * 選択中の色を変更する
     */
    private fun CanvasMode.applyColor(newHsbColor: HSBColor?, newMapColor: Byte?) {
        // どちらかの色があればもう一方の色を計算
        hsbColor = newHsbColor ?: newMapColor?.let { RGBColor.fromMapColor(it).toHSB() }
                ?: return
        color = newMapColor ?: newHsbColor?.toRGB()?.toMapColor()
                ?: return

        // 色を保存
        if (color != transparent && paletteData.selectedPaletteIndex in 0 until paletteData.storedPalettes.size) {
            paletteData.storedPalettes[paletteData.selectedPaletteIndex] = color
        }
    }

    override fun draw(f: Drawable.() -> Unit) {
    }

    override fun init() {
        // テクスチャからパレットを復元
        renderer.mapCanvas.loadPalette(paletteData)

        // パレットを描画
        renderer.g(DrawPalette(paletteData, null))
    }
}