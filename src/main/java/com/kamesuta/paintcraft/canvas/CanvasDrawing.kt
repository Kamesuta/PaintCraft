package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.canvas.paint.PaintEvent

/**
 * キャンバスの描画状態
 */
class CanvasDrawing {
    /** クリック開始時の操作 */
    var startEvent: PaintEvent? = null
        private set

    /** 描きこみの種類 */
    var drawMode: CanvasActionType = CanvasActionType.NONE
        private set

    /** 編集したマップアイテム */
    val edited = CanvasMemento.Builder()

    /** 編集履歴 */
    val history = CanvasHistory(this)

    /** 描画中か */
    val isDrawing: Boolean
        get() = startEvent != null

    /** 最後にクリックした操作、カーソルがキャンバスから外れたときのため記憶する */
    var lastEvent: PaintEvent? = null

    /**
     * 前回の状態と比較して、クリックの変化を取得する
     * @param isPressed 新しいクリック状態
     */
    fun getDrawingAction(isPressed: Boolean): CanvasDrawingActionType {
        // 前回と今回のクリック状態を比較
        return if (isPressed == isDrawing) {
            // 変化なし
            CanvasDrawingActionType.NONE
        } else {
            if (isPressed) {
                // 描画開始
                CanvasDrawingActionType.BEGIN
            } else {
                // 描画終了
                CanvasDrawingActionType.END
            }
        }
    }

    /**
     * クリック開始時の操作
     * @param event クリック開始時の操作
     */
    fun beginDrawing(event: PaintEvent) {
        // 編集したマップアイテムを初期化
        edited.clear()
        // クリック開始時の操作を記録
        startEvent = event
        // 描画モードを取得
        drawMode = event.drawMode
    }

    /**
     * クリック終了時の操作
     */
    fun endDrawing() {
        // クリック開始時の位置を初期化
        startEvent = null
        // 描画モードを初期化
        drawMode = CanvasActionType.NONE
    }
}