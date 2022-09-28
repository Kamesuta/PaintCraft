package com.kamesuta.paintcraft.map.image

import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.unchanged
import com.kamesuta.paintcraft.util.color.toMapColor
import com.kamesuta.paintcraft.util.vec.Rect2i
import org.bukkit.map.MapFont
import java.awt.image.BufferedImage
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 地図のピクセルサイズ
 */
const val mapSize = 128

/**
 * 円を描画する
 * @receiver 描画先のピクセルデータ
 * @param x 描画するX座標 (中心の座標)
 * @param y 描画するY座標 (中心の座標)
 * @param radius 半径
 * @param color 色
 */
fun PixelImage.drawCircle(x: Double, y: Double, radius: Double, color: Byte) {
    val minX = floor(x - radius).toInt().coerceIn(0, width - 1)
    val minY = floor(y - radius).toInt().coerceIn(0, height - 1)
    val maxX = ceil(x + radius).toInt().coerceIn(0, width - 1)
    val maxY = ceil(y + radius).toInt().coerceIn(0, height - 1)
    for (iy in minY..maxY) {
        for (ix in minX..maxX) {
            if ((ix - x) * (ix - x) + (iy - y) * (iy - y) < radius * radius) {
                this[ix, iy] = color
            }
        }
    }
}

/**
 * 画像を描画する
 * この関数は重いので毎フレーム呼ぶのは非推奨
 * 画像を描画するときは一旦この関数でピクセルデータに変換し、キャッシュした上でdrawPixelImageで描画する
 * @receiver 描画先のピクセルデータ
 * @param x 描画するX座標 (左上の座標)
 * @param y 描画するY座標 (左上の座標)
 * @param image 描画する画像
 */
fun PixelImage.drawImage(x: Double, y: Double, image: BufferedImage) {
    val x0 = x.roundToInt()
    val y0 = y.roundToInt()
    val minX = x0.coerceIn(0, width)
    val minY = y0.coerceIn(0, height)
    val maxX = (x0 + image.width).coerceIn(0, width)
    val maxY = (y0 + image.height).coerceIn(0, height)
    for (iy in minY until maxY) {
        for (ix in minX until maxX) {
            val color = RGBColor.fromCodeWithAlpha(image.getRGB(ix - x0, iy - y0))
            this[ix, iy] = color.toMapColor()
        }
    }
}

/**
 * ピクセルデータを描画する
 * @receiver 描画先のピクセルデータ
 * @param x 描画するX座標 (左上の座標)
 * @param y 描画するY座標 (左上の座標)
 * @param image 描画するピクセルデータ
 */
fun PixelImage.drawPixelImage(x: Double, y: Double, image: PixelImage) {
    val x0 = x.roundToInt()
    val y0 = y.roundToInt()
    val minX = x0.coerceIn(0, width)
    val minY = y0.coerceIn(0, height)
    val maxX = (x0 + image.width).coerceIn(0, width)
    val maxY = (y0 + image.height).coerceIn(0, height)
    for (iy in minY until maxY) {
        for (ix in minX until maxX) {
            val color = image[ix - x0, iy - y0]
            if (color == unchanged) continue
            this[ix, iy] = color
        }
    }
}

/**
 * ピクセルデータを描画する
 * @receiver 描画先のピクセルデータ
 * @param image 描画するピクセルデータ
 */
fun PixelImage.drawPixelImage(image: PixelImage) {
    val width = min(image.width, width)
    val height = min(image.height, height)
    for (iy in 0 until height) {
        for (ix in 0 until width) {
            val color = image[ix, iy]
            if (color == unchanged) continue
            this[ix, iy] = color
        }
    }
}

/**
 * ピクセルデータを拡大、縮小して描画する
 * @receiver 描画先のピクセルデータ
 * @param rect 描画する範囲
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
            val color = image[x, y]
            if (color == unchanged) continue
            this[ix, iy] = color
        }
    }
}

/**
 * テキストを描画する
 * CraftMapCanvasの実装からカラーコードの処理を簡略化してある
 * @receiver 描画先のピクセルデータ
 * @param x 描画するX座標 (左上の座標)
 * @param y 描画するY座標 (左上の座標)
 * @param font フォント
 * @param color テキストの色
 * @param text 描画するテキスト
 */
fun PixelImage.drawText(x: Double, y: Double, font: MapFont, color: Byte, text: String) {
    // 現在の位置
    var ix = x.roundToInt()
    var iy = y.roundToInt()
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

/**
 * マスクでピクセルデータを切り抜く (マスクがunchangedのピクセルはunchangedになる)
 * @receiver 切り抜かれる前のピクセルデータ
 * @param mask 切り抜くマスク (unchanged: 切り抜かない, それ以外: 切り抜く)
 */
fun PixelImage.maskPixelImage(mask: PixelImage) {
    val width = min(mask.width, width)
    val height = min(mask.height, height)
    for (iy in 0 until height) {
        for (ix in 0 until width) {
            val color = mask[ix, iy]
            if (color != unchanged) continue
            this[ix, iy] = unchanged
        }
    }
}
