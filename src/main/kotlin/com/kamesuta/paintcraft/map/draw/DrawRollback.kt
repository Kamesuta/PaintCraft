package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.map.image.PixelImage
import com.kamesuta.paintcraft.map.image.drawPixelImage

/**
 * 前の状態に差分だけ戻す
 * @param prevCanvas 前の状態 (この状態に復元する)
 */
class DrawRollback(prevCanvas: PixelImage) : Draw {
    /** 前の状態のピクセルデータ */
    private val prev: PixelImage

    init {
        // キャンバスからバッファーをコピーし保存する
        prev = prevCanvas.clone()
    }

    override fun draw(canvas: PixelImage) {
        // すべてのピクセルのうち、前の状態と異なるピクセルだけ前の状態を描画する
        canvas.drawPixelImage(0.0, 0.0, prev)
    }
}