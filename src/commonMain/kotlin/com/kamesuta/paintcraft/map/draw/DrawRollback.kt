package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.map.image.PixelImage
import com.kamesuta.paintcraft.map.image.PixelImageBuffer
import com.kamesuta.paintcraft.map.image.drawPixelImage
import com.kamesuta.paintcraft.map.image.maskPixelImage

/**
 * 前の状態に差分だけ戻す
 * @param prev 前の状態 (この状態に復元する)
 * @param mask マスク (unchangedの部分は復元しない)
 */
class DrawRollback(prev: PixelImage, mask: PixelImage) : Draw {
    /**
     * 前の状態のピクセルデータ
     * キャンバスからバッファーをコピーしマスクする
     */
    private val masked = PixelImageBuffer(prev.width, prev.height, prev.pixels.clone())

    init {
        // マスクでピクセルデータを切り抜く
        masked.maskPixelImage(mask)
    }

    override fun draw(canvas: PixelImage) {
        // すべてのピクセルのうち、前の状態と異なるピクセルだけ前の状態を描画する
        canvas.drawPixelImage(masked)
    }
}