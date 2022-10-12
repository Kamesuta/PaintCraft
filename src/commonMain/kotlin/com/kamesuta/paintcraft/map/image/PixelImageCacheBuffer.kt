package com.kamesuta.paintcraft.map.image

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

    /**
     * ピクセルデータをリサイズする
     * @param newWidth マップの幅
     * @param newHeight マップの高さ
     */
    fun resize(newWidth: Int, newHeight: Int) {
        // 配列のサイズをチェックする
        require(pixels.size >= newWidth * newHeight) { "Invalid map size" }
        width = newWidth
        height = newHeight
    }

    override operator fun set(x: Int, y: Int, color: Byte) {
        // マップの範囲外なら無視
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return
        }

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
     * @param dirty 更新する領域
     */
    fun subImage(src: PixelImage, dirty: Rect2i) {
        width = dirty.width
        height = dirty.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                this[x, y] = src[dirty.min.x + x, dirty.min.y + y]
            }
        }
    }
}
