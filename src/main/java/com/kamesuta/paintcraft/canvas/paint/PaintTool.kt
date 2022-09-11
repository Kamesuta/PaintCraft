package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.frame.FrameRayTraceResult
import com.kamesuta.paintcraft.util.vec.Line3d

/**
 * 描くためのツール
 */
interface PaintTool {
    /**
     * 描き込み開始
     * @param event 描き込みイベント
     */
    fun beginPainting(event: PaintEvent) {
    }

    /** 描き込み終了 */
    fun endPainting() {
    }

    /**
     * キャンバスに描く
     * @param event 描き込みイベント
     */
    fun paint(event: PaintEvent)

    /**
     * Shiftキーを押したときにスナップされる。その時のスナップされた線
     * @param line スナップされる前の線
     * @return スナップされた線
     */
    fun getGuideLine(line: Line3d) = line

    /** キャンバスセッション */
    val session: CanvasSession
}