package com.kamesuta.paintcraft.palette

import com.kamesuta.paintcraft.canvas.CanvasDrawingActionType
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.DrawableMapRenderer
import com.kamesuta.paintcraft.map.behavior.DrawBehavior
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.palette.DrawPalette.Companion.loadPalette
import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.transparent
import com.kamesuta.paintcraft.util.vec.origin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor

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
                        session.mode.setMapColor(color)
                    }
                }

                // 透明ボタンを選択した場合
                PaletteAdjustingType.TRANSPARENT_COLOR -> {
                    // 透明ボタンを押した場合は色を透明にする
                    session.mode.setMapColor(transparent)
                }

                // カラーピッカーボタンを選択した場合
                PaletteAdjustingType.COLOR_PICKER_COLOR -> {
                    // 既にカラーピッカーの場合は戻す
                    if (session.mode.tool is PaintColorPicker) {
                        // 元のツールに戻す
                        session.mode.tool = session.mode.prevTool
                    } else {
                        // カラーピッカーツールに変更
                        session.mode.tool = PaintColorPicker(session)
                        // コールバックを設定
                        session.mode.onColorChanged = {
                            // パレットに保存
                            paletteData.storeToPalette(session.mode)
                            // パレットを描画
                            event.mapItem.renderer.g(DrawPalette(paletteData, session.mode))
                            // コールバックを解除
                            //session.mode.onColorChanged = null
                        }
                    }
                }

                // カラーコードを選択した場合
                PaletteAdjustingType.COLOR_CODE -> {
                    // コールバックを設定
                    session.mode.onColorChanged = {
                        // パレットに保存
                        paletteData.storeToPalette(session.mode)
                        // パレットを描画
                        event.mapItem.renderer.g(DrawPalette(paletteData, session.mode))
                        // コールバックを解除
                        //session.mode.onColorChanged = null
                    }
                    // カラーコードテキスト
                    val mapColor = RGBColor.fromMapColor(session.mode.mapColor)
                    val hexCode = mapColor.toHexCode()
                    // チャット生成
                    val text = Component.text("Color Code: ")
                        .color(TextColor.color(0x00FFFF))
                        .append(Component.text(hexCode).color(TextColor.color(mapColor.toCode())))
                        .append(Component.text(" "))
                        .append(
                            Component.text("[Copy]")
                                .color(TextColor.color(0xFF7700))
                                .hoverEvent(
                                    HoverEvent.hoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Component.text("Click to copy: $hexCode")
                                    )
                                )
                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, hexCode))
                        )
                        .append(Component.text(" "))
                        .append(
                            Component.text("[Replace]")
                                .color(TextColor.color(0xFF7700))
                                .hoverEvent(
                                    HoverEvent.hoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Component.text("Click and type color code to use it")
                                    )
                                )
                                .clickEvent(
                                    ClickEvent.clickEvent(
                                        ClickEvent.Action.SUGGEST_COMMAND,
                                        "/paintcraft color @s "
                                    )
                                )
                        )
                    // チャット送信
                    session.player.sendMessage("")
                    session.player.sendMessage(text)
                    session.player.sendMessage("")
                }

                else -> {}
            }
        }

        // 新しい色を取得
        val color = DrawPalette.getColor(uv.x, uv.y, paletteData.adjustingType, hsb)
        // 色が変更されている場合のみ色を設定
        if (color != null) {
            session.mode.setHsbColor(color)
            // パレットに保存
            paletteData.storeToPalette(session.mode)
        }

        // 太さスライダーを選択した場合
        if (paletteData.adjustingType == PaletteAdjustingType.THICKNESS) {
            // 太さを取得
            session.mode.thickness = DrawPalette.getThickness(uv.y)
        }

        // パレットを描画
        event.mapItem.renderer.g(DrawPalette(paletteData, session.mode))

        // 更新をプレイヤーに送信
        renderer.updatePlayer(event.interact.ray.itemFrame.location.origin)
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