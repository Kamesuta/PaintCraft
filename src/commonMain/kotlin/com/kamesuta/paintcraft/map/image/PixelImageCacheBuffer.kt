package com.kamesuta.paintcraft.map.image

import com.kamesuta.paintcraft.util.DirtyRect
import com.kamesuta.paintcraft.util.vec.Rect2i

/**
 * マップピクセルデータ
 * @param pixels ピクセルデータ
 */
open class PixelImageCacheBuffer(
    final override val pixels: ByteArray
) : PixelImage {
    /** マップの幅 */
    final override var width: Int = 0
        private set

    /** マップの高さ */
    final override var height: Int = 0
        private set

    /**
     * 128x128のピクセルを取得する
     */
    constructor() : this(ByteArray(mapSize * mapSize))

    /** 更新領域 */
    val dirty = DirtyRect()

    override operator fun set(x: Int, y: Int, color: Byte) {
        // マップの範囲外なら無視
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return
        }

        // 変更がない場合は何もしない
        if (pixels[x + y * width] == color) return

        // 変更があった場合は更新領域を拡大
        dirty.flagDirty(x, y)

        // ピクセルを更新
        pixels[x + y * width] = color
    }

    override operator fun get(x: Int, y: Int): Byte {
        // マップの範囲外なら無視
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return 0
        }

        return pixels[x + y * width]
    }

    /**
     * 更新する領域を切り抜く
     * くり抜いた領域をこのインスタンスにコピーする
     * @param src ピクセルデータ
     * @param dirtyRect 更新する領域
     */
    fun subImage(src: PixelImageBuffer, dirtyRect: Rect2i) {
        // バッファーサイズを超えていないかチェック
        require(dirtyRect.width * dirtyRect.height <= pixels.size) { "Invalid map size" }
        // サイズを更新
        width = dirtyRect.width
        height = dirtyRect.height
        // ピクセルデータをコピー
        drawPixelImageFast(dirtyRect.min.x, dirtyRect.min.y, src)
        // 更新領域をセット
        dirty.clear()
        dirty.flagDirty(dirtyRect)
    }

    companion object {
        /**
         * マップピクセルデータの差分を適用する
         * @receiver 適用先マップピクセルデータ
         * @param src マップピクセルデータ差分
         */
        fun PixelImage.applyImage(src: PixelImageCacheBuffer) {
            drawPixelImage(src.dirty.minX, src.dirty.minY, src)
        }

        /**
         * 高速にピクセルデータを描画する
         * このメソッドはピクセルデータの範囲チェックを行わない
         * @receiver 描画先のピクセルデータ
         * @param x 描画するX座標 (左上の座標)
         * @param y 描画するY座標 (左上の座標)
         * @param src 描画するピクセルデータ
         */
        private fun PixelImage.drawPixelImageFast(x: Int, y: Int, src: PixelImage) {
            val dstPixels = pixels
            val dstWidth = width
            val dstHeight = height
            val srcPixels = src.pixels
            val srcWidth = src.width
            for (iy in 0 until dstHeight) {
                System.arraycopy(srcPixels, (x + (y + iy) * srcWidth), dstPixels, iy * dstWidth, dstWidth)
            }
        }
    }
}
