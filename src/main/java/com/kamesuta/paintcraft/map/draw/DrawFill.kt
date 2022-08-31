package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.map.DrawableMapBuffer
import com.kamesuta.paintcraft.map.DrawableMapBuffer.Companion.mapSize
import org.bukkit.Bukkit
import org.bukkit.map.MapCanvas

/**
 * 塗りつぶしを行うクラス
 * @param x 塗りつぶしを行うX座標
 * @param y 塗りつぶしを行うY座標
 * @param srcColor 塗りつぶす対象の色
 * @param newColor 塗りつぶす色
 */
class DrawFill(
    private val x: Int,
    private val y: Int,
    private val srcColor: Byte,
    private val newColor: Byte,
) : Draw {
    override fun draw(canvas: MapCanvas) {
        // 広範囲を再帰関数で塗りつぶすため非同期で行う
        Bukkit.getScheduler().runTaskAsynchronously(PaintCraft.instance) { ->
            // 塗りつぶし済みマークを作成
            val colored = DrawableMapBuffer()
            // 塗りつぶしを行う
            fillBucket(canvas, colored, x, y, srcColor, newColor)
        }
    }

    /**
     * 再帰関数で塗りつぶしを行う
     * @param canvas 塗りつぶしを行うキャンバス
     * @param colored 塗りつぶし済みのマップ
     * @param x 塗りつぶしを行うX座標
     * @param y 塗りつぶしを行うY座標
     * @param srcColor 塗りつぶす対象の色
     * @param newColor 塗りつぶす色
     */
    private fun fillBucket(
        canvas: MapCanvas,
        colored: DrawableMapBuffer,
        x: Int,
        y: Int,
        srcColor: Byte,
        newColor: Byte
    ) {
        // 範囲外は無視
        if (x < 0 || y < 0) {
            return
        }
        if (x >= mapSize || y >= mapSize) {
            return
        }
        // 色が既に塗りつぶし済みなら無視
        if (colored[x, y] != 0.toByte()) {
            return
        }
        // 色が一致しないなら無視
        if (canvas.getPixel(x, y) != srcColor) {
            return
        }
        // 色を塗りつぶす
        canvas.setPixel(x, y, newColor)
        // 塗りつぶし済みとして記録
        colored[x, y] = 1.toByte()

        // 上下左右に再帰的に塗りつぶしを行う
        fillBucket(canvas, colored, x - 1, y, srcColor, newColor)
        fillBucket(canvas, colored, x + 1, y, srcColor, newColor)
        fillBucket(canvas, colored, x, y - 1, srcColor, newColor)
        fillBucket(canvas, colored, x, y + 1, srcColor, newColor)
    }
}