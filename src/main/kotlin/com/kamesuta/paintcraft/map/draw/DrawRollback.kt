package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.map.DrawableMapBuffer
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import com.kamesuta.paintcraft.map.DrawableMapReflection
import org.bukkit.map.MapCanvas

/**
 * 前の状態に差分だけ戻す
 * @param prevCanvas 前の状態 (この状態に復元する)
 */
class DrawRollback(prevCanvas: MapCanvas) : Draw {
    /** 前の状態のピクセルデータ */
    private val prev: DrawableMapBuffer

    init {
        // キャンバスからバッファーをコピーし保存する
        prev = DrawableMapReflection.getCanvasBuffer(prevCanvas)?.clone() ?: DrawableMapBuffer()
    }

    override fun draw(canvas: MapCanvas) {
        // キャンバスからバッファー取得
        val now = DrawableMapReflection.getCanvasBuffer(canvas)
            ?: return

        // すべてのピクセルのうち、前の状態と異なるピクセルだけ前の状態を描画する
        for (i in 0 until mapSize * mapSize) {
            val prevPixel = prev.pixels[i]
            if (prevPixel != now.pixels[i]) {
                canvas.setPixel(i % mapSize, i / mapSize, prevPixel)
            }
        }
    }
}