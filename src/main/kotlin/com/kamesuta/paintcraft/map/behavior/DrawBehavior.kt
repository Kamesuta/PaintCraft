package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.draw.Drawable

/**
 * マップの描きこみを行うクラス
 */
interface DrawBehavior {
    /**
     * ツールを使用して描きこむ
     * @param session キャンバスセッション
     * @param event 描きこみイベント
     */
    fun paint(session: CanvasSession, event: PaintEvent) {}

    /**
     * マップの描きこみを行う
     * @param draw 描き込み対象
     * @param f 描きこむ
     */
    fun draw(f: Drawable.() -> Unit) {}

    /**
     * 初期描画
     * @param draw 描き込み対象
     */
    fun init() {}
}