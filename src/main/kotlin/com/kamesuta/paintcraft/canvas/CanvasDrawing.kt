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

    /** 描きこみの変化 */
    var drawingAction: CanvasDrawingActionType = CanvasDrawingActionType.NONE
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
     * 前回の状態と比較して、クリックの変化を更新する
     * @param isPressed 新しいクリック状態
     */
    fun updateDrawingAction(isPressed: Boolean) {
        // 前回と今回のクリック状態を比較
        drawingAction = if (isPressed == isDrawing) {
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
        // 前回変更があれば履歴に追加
        if (edited.isDirty) {
            // 前回の変更を適用
            val memento = edited.build()
            // 前回の状態を履歴に保存
            history.add(memento)
        }
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