package com.kamesuta.paintcraft.canvas

/**
 * キャンバスの履歴を管理するクラス
 * @param drawing
 */
class CanvasHistory(private val drawing: CanvasDrawing) {
    /** 履歴 */
    private val history = mutableListOf<CanvasMemento>()

    /**
     * 履歴に追加
     * @param memento 追加する履歴
     */
    fun add(memento: CanvasMemento) {
        history.add(memento)
        // 履歴の最大数を超えたら古いものから削除する
        if (history.size > MAX_HISTORY) {
            history.removeFirst()
        }
    }

    /** 履歴を戻す */
    fun undo() {
        val memento = history.removeLastOrNull() ?: return
        memento.rollback()
        memento.updatePlayer()
    }

    companion object {
        /** 履歴の最大保持数 */
        // TODO: configで設定できるようにする
        const val MAX_HISTORY = 10
    }
}