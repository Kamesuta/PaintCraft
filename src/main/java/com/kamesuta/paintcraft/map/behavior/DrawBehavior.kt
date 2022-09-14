package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent

/**
 * マップの描きこみを行うクラス
 */
interface DrawBehavior {
    /** ビヘイビアの名前 */
    val name: String

    /**
     * マップの描きこみを行う
     * @param session キャンバスセッション
     * @param event 描きこみイベント
     */
    fun draw(session: CanvasSession, event: PaintEvent)
}