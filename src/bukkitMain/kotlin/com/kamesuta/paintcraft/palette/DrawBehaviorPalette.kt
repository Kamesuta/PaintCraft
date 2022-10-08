package com.kamesuta.paintcraft.palette

import com.kamesuta.paintcraft.canvas.CanvasDrawingActionType
import com.kamesuta.paintcraft.canvas.CanvasMode
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.DrawableMapRenderer
import com.kamesuta.paintcraft.map.behavior.DrawBehavior
import com.kamesuta.paintcraft.palette.DrawPalette.Companion.loadPalette
import com.kamesuta.paintcraft.player.PaintSession
import com.kamesuta.paintcraft.util.color.MapColor
import com.kamesuta.paintcraft.util.color.RGBColor.Companion.toRGB
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.transparent
import com.kamesuta.paintcraft.util.color.toMapColor

/**
 * パレット
 * @param renderer 描画クラス
 */
class DrawBehaviorPalette(private val renderer: DrawableMapRenderer) : DrawBehavior {
    /** パレットに保存されるデータ */
    private val paletteData = PaletteData()

    override fun paint(session: PaintSession, event: PaintEvent) {
        // TODO: キャンバスセッションを抽象化する
        require(session is CanvasSession)

        // UV座標を取得
        val uv = event.interact.uv
        // クリック開始時の場合のみ調整中のモードを設定
        if (session.drawing.drawingAction == CanvasDrawingActionType.BEGIN) {
            // 新しい調整モードを取得して置き換える
            paletteData.adjustingType = DrawPalette.getAdjustingType(uv.x, uv.y)

            // コールバックをリセット
            session.mode.onColorChanged = null
            // 元のツールに戻す
            val isPickerTool = session.mode.tool is PaintColorPicker
            if (isPickerTool) {
                session.mode.tool = session.mode.prevTool
            }
            paletteData.isPickerTool = false

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
                        paletteData.mapColor = color
                        paletteData.hsbColor = MapColor.toRGBColor(color).toHSB()
                    }
                }

                // 透明ボタンを選択した場合
                PaletteAdjustingType.TRANSPARENT_COLOR -> {
                    // 既に透明かどうかを判定
                    if (paletteData.mapColor != transparent) {
                        // 透明ボタンを押した場合は色を透明にする
                        paletteData.mapColor = transparent
                    } else {
                        // 元の色に戻す
                        // パレットから色を読み込む
                        val color = paletteData.storedPalettes.getOrNull(paletteData.selectedPaletteIndex)
                        // 透明ボタンを押した場合は色を白にする
                        paletteData.mapColor = color ?: paletteData.hsbColor.toRGB().toMapColor()
                    }
                }

                // カラーピッカーボタンを選択した場合
                PaletteAdjustingType.COLOR_PICKER_COLOR -> {
                    // カラーピッカーツールを選択していない場合はカラーピッカーツールを選択する
                    if (!isPickerTool) {
                        // カラーピッカーツールに変更
                        session.mode.tool = PaintColorPicker(session)
                        paletteData.isPickerTool = true
                        // コールバックを設定
                        session.mode.onColorChanged = {
                            // パレットに保存
                            paletteData.mapColor = session.mode.mapColor
                            paletteData.hsbColor = MapColor.toRGBColor(paletteData.mapColor).toHSB()
                            paletteData.storeColorToPalette()
                            // パレットを描画
                            drawPalette(event)
                        }
                    }
                }

                // カラーコードを選択した場合
                PaletteAdjustingType.COLOR_CODE -> {
                    // コールバックを設定
                    session.mode.onColorChanged = {
                        // パレットに保存
                        paletteData.mapColor = session.mode.mapColor
                        paletteData.hsbColor = MapColor.toRGBColor(paletteData.mapColor).toHSB()
                        paletteData.storeColorToPalette()
                        // パレットを描画
                        drawPalette(event)
                    }
                    // カラーコードテキスト
                    val rgbColor = MapColor.toRGBColor(paletteData.mapColor)
                    val mapColor = paletteData.mapColor
                    session.player.sendColorMessage(rgbColor, mapColor)
                }

                else -> {}
            }
        }

        // 新しい色を取得
        val color = DrawPalette.getColor(uv.x, uv.y, paletteData.adjustingType, paletteData.hsbColor)
        // 色が変更されている場合のみ色を設定
        if (color != null) {
            paletteData.hsbColor = color
            paletteData.mapColor = color.toRGB().toMapColor()
            // パレットに保存
            paletteData.storeColorToPalette()
        }

        // 太さスライダーを選択した場合
        if (paletteData.adjustingType == PaletteAdjustingType.THICKNESS) {
            // 太さを取得
            paletteData.thickness = DrawPalette.getThickness(uv.y)
        }

        // パレットをプレイヤーのキャンバスに保存
        storeToCanvas(session.mode)

        // パレットを描画
        drawPalette(event)
    }

    override fun init() {
        // テクスチャからパレットを復元
        renderer.mapLayer.base.loadPalette(paletteData)

        // パレットを描画
        renderer.drawBase(DrawPalette(paletteData))
    }

    /**
     * パレットを描画する
     * @param event イベント
     */
    private fun drawPalette(event: PaintEvent) {
        // パレットを描画
        event.mapItem.renderer.drawBase(DrawPalette(paletteData))

        // 更新をプレイヤーに送信
        renderer.updatePlayer(event.interact.ray.itemFrame.location.origin)
    }

    /**
     * パレットをプレイヤーのキャンバスに保存する
     * @param mode モード
     */
    private fun storeToCanvas(mode: CanvasMode) {
        // パレットの色をキャンバスに保存
        mode.mapColor = paletteData.mapColor
        // パレットの太さをキャンバスに保存
        mode.thickness = paletteData.thickness
    }
}