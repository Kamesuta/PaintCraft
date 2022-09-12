package com.kamesuta.paintcraft.canvas

/**
 * キャンバスの履歴を管理するクラス
 * @param drawing
 */
class CanvasHistory(private val drawing: CanvasDrawing) {
    /** 履歴 */
    // TODO: 最大履歴数を設定できるようにする
    private val history = mutableListOf<CanvasMemento>()

    /**
     * 履歴に追加
     * @param memento 追加する履歴
     */
    fun add(memento: CanvasMemento) {
        history.add(memento)
    }

    /** 履歴を戻す */
    fun undo() {
        val memento = history.removeLastOrNull() ?: return
        memento.apply()
        memento.updatePlayer()
    }
}