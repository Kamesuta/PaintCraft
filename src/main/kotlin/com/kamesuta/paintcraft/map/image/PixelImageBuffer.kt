package com.kamesuta.paintcraft.map.image

import com.kamesuta.paintcraft.util.vec.Rect2i

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
) : Cloneable, PixelImage {
    /**
     * 配列のサイズをチェックする
     */
    init {
        require(pixels.size == width * height) { "Invalid map size" }
    }

    /**
     * 128x128のピクセルを取得する
     * @param width マップの幅
     * @param height マップの高さ
     */
    constructor(width: Int, height: Int) : this(width, height, ByteArray(width * height))

    override operator fun set(x: Int, y: Int, color: Byte) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return
        }

        pixels[x + y * width] = color
    }

    override operator fun get(x: Int, y: Int): Byte {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return 0
        }

        return pixels[x + y * width]
    }

    /**
     * 更新する領域を切り抜く
     * @param dirty 更新する領域
     * @return 切り抜かれたピクセルデータ配列
     */
    fun createSubImage(dirty: Rect2i): PixelImageBuffer {
        val width = dirty.width
        val height = dirty.height
        val part = PixelImageBuffer(width, height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                part[x, y] = this[dirty.p1.x + x, dirty.p1.y + y]
            }
        }
        return part
    }

    /**
     * ピクセルを全てコピーして新しいインスタンスを作成する
     * @return コピーしたインスタンス
     */
    public override fun clone(): PixelImageBuffer {
        return PixelImageBuffer(width, height, pixels.clone())
    }
}
