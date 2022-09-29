package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.map.image.PixelImage

/**
 * マップにピクセルを描画するためのインターフェース
 */
fun interface Draw {
    /**
     * マップに描画する
     * @param canvas 描画先
     */
    fun draw(canvas: PixelImage)
}
