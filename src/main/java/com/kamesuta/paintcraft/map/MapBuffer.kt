package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.util.UVIntArea

/**
 * マップピクセルデータ
 * @param pixels ピクセルデータ
 */
class MapBuffer(val pixels: ByteArray) {
    /**
     * 128x128のピクセルを取得する
     */
    constructor() : this(ByteArray(mapSize * mapSize))

    /**
     * ピクセルを設定する
     * @param x x座標
     * @param y y座標
     * @param color ピクセル値
     */
    operator fun set(x: Int, y: Int, color: Byte) {
        if (x < 0 || x >= mapSize || y < 0 || y >= mapSize) {
            return
        }

        val index = y * mapSize + x
        if (index < 0 || index >= pixels.size) {
            return
        }

        pixels[index] = color
    }

    /**
     * ピクセルを取得する
     * @param x x座標
     * @param y y座標
     * @return ピクセル値
     */
    operator fun get(x: Int, y: Int): Byte {
        if (x < 0 || x >= mapSize || y < 0 || y >= mapSize) {
            return 0
        }

        val index = y * mapSize + x
        if (index < 0 || index >= pixels.size) {
            return 0
        }

        return pixels[index]
    }

    /**
     * 更新する領域を切り抜く
     * @param dirty 更新する領域
     * @return 切り抜かれたピクセルデータ配列
     */
    fun createSubImage(dirty: UVIntArea): ByteArray {
        val width = dirty.width
        val height = dirty.height
        val part = ByteArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                part[x + y * width] = this[dirty.p1.u + x, dirty.p1.v + y]
            }
        }
        return part
    }

    /**
     * ピクセルを全てコピーする
     * @param destination コピー先
     */
    fun copyTo(destination: MapBuffer) {
        System.arraycopy(pixels, 0, destination.pixels, 0, destination.pixels.size)
    }

    companion object {
        /**
         * 地図のピクセルサイズ
         */
        const val mapSize = 128
    }
}