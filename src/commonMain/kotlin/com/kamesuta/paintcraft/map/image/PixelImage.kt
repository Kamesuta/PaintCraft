package com.kamesuta.paintcraft.map.image

/**
 * マップピクセルデータ
 */
interface PixelImage {
    /** マップの幅 */
    val width: Int

    /** マップの高さ */
    val height: Int

    /** ピクセルデータ */
    val pixels: ByteArray

    /**
     * ピクセルを設定する
     * @param x x座標
     * @param y y座標
     * @param color ピクセル値
     */
    operator fun set(x: Int, y: Int, color: Byte)

    /**
     * ピクセルを取得する
     * @param x x座標
     * @param y y座標
     * @return ピクセル値
     */
    operator fun get(x: Int, y: Int): Byte
}