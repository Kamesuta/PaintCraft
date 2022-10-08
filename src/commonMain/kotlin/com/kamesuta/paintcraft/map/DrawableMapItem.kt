package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.player.PaintPlayer

/**
 * 書き込み可能マップ
 * @param mapId マップID
 * @param renderer レンダラー
 */
open class DrawableMapItem(
    val mapId: Int,
    val renderer: DrawableMapRenderer,
) {
    /**
     * マップに描画する
     * @param player 描き込んだプレイヤー
     * @param f 描画する関数
     */
    fun draw(player: PaintPlayer, f: Drawable.() -> Unit) {
        renderer.behavior.draw(player, f)
    }
}