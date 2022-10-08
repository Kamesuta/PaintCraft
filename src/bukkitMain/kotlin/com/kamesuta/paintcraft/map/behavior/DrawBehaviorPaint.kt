package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.ICanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintEvent
import com.kamesuta.paintcraft.map.DrawableMapRenderer
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.player.PaintPlayer

/**
 * 描きこみを行うツール
 * @param renderer 描画クラス
 */
class DrawBehaviorPaint(private val renderer: DrawableMapRenderer) : DrawBehavior {
    override fun paint(session: ICanvasSession, event: PaintEvent) {
        // TODO: キャンバスセッションを抽象化する
        require(session is CanvasSession)

        session.mode.tool.paint(event)
    }

    override fun draw(player: PaintPlayer, f: Drawable.() -> Unit) {
        f(renderer.drawer(player))
    }
}