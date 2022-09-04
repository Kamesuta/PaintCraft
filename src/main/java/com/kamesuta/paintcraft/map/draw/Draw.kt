package com.kamesuta.paintcraft.map.draw

import org.bukkit.map.MapCanvas

/**
 * マップにピクセルを描画するためのインターフェース
 */
fun interface Draw {
    /**
     * マップに描画する
     * @param canvas 描画先
     */
    fun draw(canvas: MapCanvas)
}
