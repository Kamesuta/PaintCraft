package com.kamesuta.paintcraft.map.image

/**
 * マップピクセルデータ
 * @param width マップの幅
 * @param height マップの高さ
 * @param pixels ピクセルデータ
 */
open class PixelImageBuffer(
    final override val width: Int,
    final override val height: Int,
    final override val pixels: ByteArray
) : PixelImage {
    /**
     * 配列のサイズをチェックする
     */
    init {
        require(pixels.size == width * height) { "Invalid map size" }
    }

    /**
     * ピクセルデータを作成する
     * @param width マップの幅
     * @param height マップの高さ
     */
    constructor(width: Int, height: Int) : this(width, height, ByteArray(width * height))

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
}
