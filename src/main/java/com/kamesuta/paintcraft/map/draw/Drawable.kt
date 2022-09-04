package com.kamesuta.paintcraft.map.draw

/**
 * マップにピクセルを描画することができるインターフェース
 */
fun interface Drawable {
    /**
     * マップにピクセルを描画します
     * @param draw ピクセルを描画するための命令
     */
    fun g(draw: Draw)
}