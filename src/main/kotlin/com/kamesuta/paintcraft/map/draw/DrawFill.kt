package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.map.image.PixelImage
import com.kamesuta.paintcraft.map.image.PixelImageBuffer
import com.kamesuta.paintcraft.map.image.mapSize

/**
 * 塗りつぶしを行うクラス
 * @param x 塗りつぶしを行うX座標
 * @param y 塗りつぶしを行うY座標
 * @param color 塗りつぶす色
 */
class DrawFill(
    private val x: Int,
    private val y: Int,
    private val color: Byte,
) : Draw {
    override fun draw(canvas: PixelImage) {
        // 塗りつぶし済みマークを作成
        val colored = PixelImageBuffer(canvas.width, canvas.height)
        // 元の色を取得
        val srcColor = canvas[x, y]
        // 塗りつぶしを行う
        fillBucket(canvas, colored, x, y, srcColor, color)
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
        canvas: PixelImage,
        colored: PixelImageBuffer,
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
        if (colored[x, y] == FILLED) {
            return
        }
        // 色が一致しないなら無視
        if (canvas[x, y] != srcColor) {
            return
        }
        // 色を塗りつぶす
        canvas[x, y] = newColor
        // 塗りつぶし済みとして記録
        colored[x, y] = FILLED

        // 上下左右に再帰的に塗りつぶしを行う
        fillBucket(canvas, colored, x - 1, y, srcColor, newColor)
        fillBucket(canvas, colored, x + 1, y, srcColor, newColor)
        fillBucket(canvas, colored, x, y - 1, srcColor, newColor)
        fillBucket(canvas, colored, x, y + 1, srcColor, newColor)
    }

    companion object {
        /** 塗りつぶし済みマーク */
        const val FILLED: Byte = 1
    }
}