package com.kamesuta.paintcraft.canvas

/**
 * キャンバスの履歴を管理するクラス
 * @param drawing
 */
class CanvasHistory(private val drawing: CanvasDrawing) {
    /** 履歴 */
    private val history = ArrayDeque<CanvasMemento>()

    /**
     * 履歴に追加
     * @param memento 追加する履歴
     */
    fun add(memento: CanvasMemento) {
        history.add(memento)
        // 履歴の最大数を超えたら古いものから削除する
        if (history.size > MAX_HISTORY - 1) {
            history.removeFirst()
        }
    }

    /** 履歴を戻す */
    fun undo() {
        if (drawing.edited.isDirty) {
            // 編集中の変更点がある場合は戻す
            drawing.edited.clear()
            // 更新を通知
            drawing.edited.updatePlayer()
        } else {
            // 履歴を取り出す
            val memento = history.removeLastOrNull() ?: return
            // 変更点を戻す
            memento.rollback()
            // 更新を通知
            memento.updatePlayer()
        }
    }

    companion object {
        /** 履歴の最大保持数 */
        // TODO: configで設定できるようにする
        const val MAX_HISTORY = 10
    }
}