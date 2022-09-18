package com.kamesuta.paintcraft.canvas

/**
 * 絵を描いている状態
 */
enum class CanvasDrawingActionType {
    /** 変化なし */
    NONE,

    /** 描き込み開始 */
    BEGIN,

    /** 描き込み終了 */
    END,
}