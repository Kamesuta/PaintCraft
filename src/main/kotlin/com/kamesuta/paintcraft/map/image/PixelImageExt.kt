package com.kamesuta.paintcraft.map.image

import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.vec.Rect2i
import org.bukkit.map.MapFont
import java.awt.image.BufferedImage

/**
 * 地図のピクセルサイズ
 */
const val mapSize = 128

/**
 * 画像を描画する
 * この関数は重いので毎フレーム呼ぶのは非推奨
 * 画像を描画するときは一旦この関数でピクセルデータに変換し、キャッシュした上でdrawPixelImageで描画する
 * @param x 描画するX座標 (左上の座標)
 * @param y 描画するY座標 (左上の座標)
 * @receiver 描画先のピクセルデータ
 * @param image 描画する画像
 */
fun PixelImage.drawImage(x: Int, y: Int, image: BufferedImage) {
    val minX = x.coerceIn(0, width)
    val minY = y.coerceIn(0, height)
    val maxX = (x + image.width).coerceIn(0, width)
    val maxY = (y + image.height).coerceIn(0, height)
    for (iy in minY until maxY) {
        for (ix in minX until maxX) {
            val color = RGBColor.fromCodeWithAlpha(image.getRGB(ix - x, iy - y))
            this[ix, iy] = color.toMapColor()
        }
    }
}

/**
 * ピクセルデータを描画する
 * @param x 描画するX座標 (左上の座標)
 * @param y 描画するY座標 (左上の座標)
 * @receiver 描画先のピクセルデータ
 * @param image 描画するピクセルデータ
 */
fun PixelImage.drawPixelImage(x: Int, y: Int, image: PixelImage) {
    val minX = x.coerceIn(0, width)
    val minY = y.coerceIn(0, height)
    val maxX = (x + image.width).coerceIn(0, width)
    val maxY = (y + image.height).coerceIn(0, height)
    for (iy in minY until maxY) {
        for (ix in minX until maxX) {
            this[ix, iy] = image[ix - x, iy - y]
        }
    }
}

/**
 * ピクセルデータを拡大、縮小して描画する
 * @param rect 描画する範囲
 * @receiver 描画先のピクセルデータ
 * @param image 描画するピクセルデータ
 */
fun PixelImage.drawPixelImageWithResize(rect: Rect2i, image: PixelImage) {
    val minX = rect.p1.x.coerceIn(0, width)
    val minY = rect.p1.y.coerceIn(0, height)
    val maxX = rect.p2.x.coerceIn(0, width)
    val maxY = rect.p2.y.coerceIn(0, height)
    for (iy in minY until maxY) {
        for (ix in minX until maxX) {
            val x = (ix - rect.p1.x) * image.width / rect.width
            val y = (iy - rect.p1.y) * image.height / rect.height
            this[ix, iy] = image[x, y]
        }
    }
}

/**
 * テキストを描画する
 * CraftMapCanvasの実装からカラーコードの処理を簡略化してある
 * @param x 描画するX座標 (左上の座標)
 * @param y 描画するY座標 (左上の座標)
 * @param font フォント
 * @param color テキストの色
 * @param text 描画するテキスト
 * @receiver 描画先のピクセルデータ
 */
fun PixelImage.drawText(x: Int, y: Int, font: MapFont, color: Byte, text: String) {
    // 現在の位置
    var ix = x
    var iy = y
    // X座標のスタート地点
    val xStart = ix
    // テキストの文字数分ループ
    for (ch in text) {
        if (ch == '\n') {
            // 改行する
            ix = xStart
            iy += font.height + 1
        } else {
            // 文字を描画する
            // Y座標が範囲外になる場合は描画しない
            if (iy !in -font.height until height) {
                continue
            }
            // スプライトを取得する
            val sprite = font.getChar(ch)
                ?: continue // 無効な文字ならスキップ
            // X座標が範囲内のときのみ描画する
            if (ix in -sprite.width until width) {
                // スプライトを描画する
                for (r in 0 until font.height) {
                    for (c in 0 until sprite.width) {
                        // 透明ならスキップ
                        if (sprite[r, c]) {
                            // ピクセルを描画する
                            this[ix + c, iy + r] = color
                        }
                    }
                }
            }
            // 次の文字の描画位置に移動する
            ix += sprite.width + 1
        }
    }
}
