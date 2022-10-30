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
    fun subImage(src: PixelImage, dirtyRect: Rect2i) {
        width = dirtyRect.width
        height = dirtyRect.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                this[x, y] = src[dirtyRect.min.x + x, dirtyRect.min.y + y]
            }
        }
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
            drawPixelImage(src.dirty.minX.toDouble(), src.dirty.minY.toDouble(), src)
        }
    }
}
