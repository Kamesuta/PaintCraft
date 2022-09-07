package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.util.TimeWatcher

/**
 * クリック状態
 */
class CanvasClicking(private val session: CanvasSession) {
    /** 最後の操作時刻 */
    var lastTime = 0L
        private set

    /** 操作モード */
    var clickMode: CanvasActionType = CanvasActionType.NONE
        private set

    /** 描いている時間内か */
    private val isInClickingTime: Boolean
        get() = session.clientType.threshold.drawDuration.isInTime(lastTime)

    /** 描くのを止める */
    private var pauseClick = false

    /** クリックボタンを離すまで、クリックを離した状態にする */
    fun stopClicking() {
        pauseClick = true
    }

    /**
     * クリック状態を更新する
     * クリックしたとき、視線移動したときに呼ぶ
     * @param actionType クリック状態
     */
    fun updateClick(actionType: CanvasActionType) {
        if (actionType == CanvasActionType.MOUSE_MOVE) {
            // 動いたときは時間を見てとめる
            if (!isInClickingTime) {
                // 離したので描くのを再開
                pauseClick = false
                // 操作モードをリセット
                clickMode = CanvasActionType.NONE
            }
        } else {
            if (clickMode != CanvasActionType.NONE && clickMode != actionType) {
                // 押していたボタンとは別のボタンを押したときは描くのを止める
                // 離すまで描画を止める
                pauseClick = true
                // 操作モードをリセット
                clickMode = CanvasActionType.NONE
            } else if (!pauseClick) {
                // 描画が一時停止されていない状態で、左右クリックした場合は状態を更新する
                // 操作モードをセット
                clickMode = actionType
            }
            // クリック時間を更新する
            lastTime = TimeWatcher.now
        }
    }
}