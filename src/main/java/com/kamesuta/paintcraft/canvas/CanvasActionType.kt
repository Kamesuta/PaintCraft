package com.kamesuta.paintcraft.canvas

/**
 * キャンバスのクリックの種類
 */
enum class CanvasActionType(val isPressed: Boolean) {
    /** なし */
    NONE(false),

    /** 左クリック */
    LEFT_CLICK(true),

    /** 右クリック */
    RIGHT_CLICK(true),

    /** マウスを移動 */
    MOUSE_MOVE(false),
}