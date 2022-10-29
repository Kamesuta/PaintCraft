package com.kamesuta.paintcraft.map.image

import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.unchanged
import com.kamesuta.paintcraft.util.color.toMapColor
import com.kamesuta.paintcraft.util.vec.Rect2i
import com.kamesuta.paintcraft.util.vec.Vec2i
import java.awt.image.BufferedImage
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 地図のピクセルサイズ
 */
const val mapSize = 128

/** ピクセルデータの矩形範囲 */
val PixelImage.rect: Rect2i get() = Rect2i(Vec2i(0, 0), Vec2i(width - 1, height - 1))

/**
 * 円を描画する
 * @receiver 描画先のピクセルデータ
 * @param x 描画するX座標 (中心の座標)
 * @param y 描画するY座標 (中心の座標)
 * @param radius 半径
 * @param color 色
 */
fun PixelImage.drawCircle(x: Double, y: Double, radius: Double, color: Byte) {
    val minX = floor(x - radius).toInt().coerceIn(0 until width)
    val minY = floor(y - radius).toInt().coerceIn(0 until height)
    val maxX = ceil(x + radius).toInt().coerceIn(0 until width)
    val maxY = ceil(y + radius).toInt().coerceIn(0 until height)
    for (iy in minY..maxY) {
        for (ix in minX..maxX) {
            if ((ix - x) * (ix - x) + (iy - y) * (iy - y) < radius * radius) {
                this[ix, iy] = color
            }
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
    val minX = x0.coerceIn(0 until width)
    val minY = y0.coerceIn(0 until height)
    val maxX = (x0 + image.width - 1).coerceIn(0 until width)
    val maxY = (y0 + image.height - 1).coerceIn(0 until height)
    for (iy in minY..maxY) {
        for (ix in minX..maxX) {
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
    val minX = rect.min.x.coerceIn(0 until width)
    val minY = rect.min.y.coerceIn(0 until height)
    val maxX = rect.max.x.coerceIn(0 until width)
    val maxY = rect.max.y.coerceIn(0 until height)
    for (iy in minY..maxY) {
        for (ix in minX..maxX) {
            val x = (ix - rect.min.x) * image.width / rect.width
            val y = (iy - rect.min.y) * image.height / rect.height
            val color = image[x, y]
            if (color == unchanged) continue
            this[ix, iy] = color
        }
    }
}

/**
 * ピクセルデータを切り抜いて描画する
 * @receiver 描画先のピクセルデータ
 * @param rect くり抜く範囲
 * @param image 描画するピクセルデータ
 */
fun PixelImage.drawPixelImageCrop(rect: Rect2i, image: PixelImage) {
    val minX = rect.min.x.coerceIn(0 until width)
    val minY = rect.min.y.coerceIn(0 until height)
    val maxX = rect.max.x.coerceIn(0 until width)
    val maxY = rect.max.y.coerceIn(0 until height)
    for (iy in minY..maxY) {
        for (ix in minX..maxX) {
            val color = image[ix, iy]
            if (color == unchanged) continue
            this[ix, iy] = color
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

/**
 * 変更がない状態で埋める
 * @receiver 描画先のピクセルデータ
 * @param rect 埋める範囲
 */
fun PixelImage.fillUnchanged(rect: Rect2i) {
    val minX = rect.min.x.coerceIn(0 until width)
    val minY = rect.min.y.coerceIn(0 until height)
    val maxX = rect.max.x.coerceIn(0 until width)
    val maxY = rect.max.y.coerceIn(0 until height)
    for (iy in minY..maxY) {
        for (ix in minX..maxX) {
            this[ix, iy] = unchanged
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
    val minX = x0.coerceIn(0 until width)
    val minY = y0.coerceIn(0 until height)
    val maxX = (x0 + image.width - 1).coerceIn(0 until width)
    val maxY = (y0 + image.height - 1).coerceIn(0 until height)
    for (iy in minY..maxY) {
        for (ix in minX..maxX) {
            val color = RGBColor.fromCodeWithAlpha(image.getRGB(ix - x0, iy - y0))
            this[ix, iy] = color.toMapColor()
        }
    }
}

/**
 * テキストの幅を取得する
 * @receiver 描画先のピクセルデータ
 */
expect fun PixelImage.getTextWidth(text: String): Int

/**
 * テキストを描画する
 * CraftMapCanvasの実装からカラーコードの処理を簡略化してある
 * @receiver 描画先のピクセルデータ
 * @param x 描画するX座標 (左上の座標)
 * @param y 描画するY座標 (左上の座標)
 * @param color テキストの色
 * @param text 描画するテキスト
 */
expect fun PixelImage.drawText(x: Double, y: Double, color: Byte, text: String)
