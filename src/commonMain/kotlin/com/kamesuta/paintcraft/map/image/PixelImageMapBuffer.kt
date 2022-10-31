package com.kamesuta.paintcraft.map.image

import com.kamesuta.paintcraft.util.DirtyRect
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.unchanged

/**
 * マップピクセルデータ
 * マイクラのマップ用、変更された範囲を保持する
 * @param pixels ピクセルデータ
 */
open class PixelImageMapBuffer(pixels: ByteArray) : PixelImageBuffer(mapSize, mapSize, pixels) {
    /** 128x128のピクセルを取得する */
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

    /**
     * ピクセルを全てコピーする
     * @param destination コピー先
     */
    fun copyTo(destination: PixelImageMapBuffer) {
        System.arraycopy(pixels, 0, destination.pixels, 0, destination.pixels.size)
    }

    /**
     * 変更がない状態で初期化
     * @return 初期化されたインスタンス
     */
    fun clearToUnchanged(force: Boolean = false) {
        if (force) {
            // ピクセルを全て変更なしにする
            pixels.fill(unchanged)
        } else {
            // 変更された場所だけ変更なしにする
            dirty.rect?.let { fillUnchanged(it) }
        }
    }
}
