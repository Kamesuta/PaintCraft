package com.kamesuta.paintcraft.map.image

import org.bukkit.map.MapFont
import kotlin.math.roundToInt

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
