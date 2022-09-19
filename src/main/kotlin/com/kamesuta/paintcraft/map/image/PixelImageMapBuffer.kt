package com.kamesuta.paintcraft.map.image

/**
 * マップピクセルデータ
 * @param pixels ピクセルデータ
 */
open class PixelImageMapBuffer(pixels: ByteArray) : Cloneable, PixelImageBuffer(mapSize, mapSize, pixels) {
    /**
     * 128x128のピクセルを取得する
     */
    constructor() : this(ByteArray(mapSize * mapSize))

    /**
     * ピクセルを全てコピーする
     * @param destination コピー先
     */
    fun copyTo(destination: PixelImageMapBuffer) {
        System.arraycopy(pixels, 0, destination.pixels, 0, destination.pixels.size)
    }

    /**
     * ピクセルを全てコピーして新しいインスタンスを作成する
     * @return コピーしたインスタンス
     */
    override fun clone(): PixelImageMapBuffer {
        return PixelImageMapBuffer(pixels.clone())
    }
}
