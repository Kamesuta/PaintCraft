package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.draw.Drawable
import org.bukkit.entity.Player

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
     * @param player 描き込んだプレイヤー
     * @param f 描きこむ
     */
    fun draw(player: Player, f: Drawable.() -> Unit) {}

    /**
     * 初期描画
     * @param draw 描き込み対象
     */
    fun init() {}
}