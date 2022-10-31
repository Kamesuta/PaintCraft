package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.util.TimeWatcher
import com.kamesuta.paintcraft.util.clienttype.ClientType

/**
 * クリック状態
 */
class CanvasClicking(private val clientType: ClientType) {
    /** 最後の操作時刻 */
    var lastTime = 0L
        private set

    /** 操作モード */
    var clickMode: CanvasActionType = CanvasActionType.NONE
        private set

    /** 描いている時間内か */
    private val isInClickingTime: Boolean
        get() = clientType.threshold.drawDuration.isInTime(lastTime)

    /** 描くのを止める */
    private var pauseClick = false

    /** クリック状態を固定する */
    var isFreeze = false

    /** クリック状態を設定して固定する */
    fun setClickAndFreeze(clickMode: CanvasActionType) {
        this.clickMode = clickMode
        this.isFreeze = true
    }

    /** クリックボタンを離すまで、クリックを離した状態にする */
    fun stopClicking() {
        // 離すまで描画を止める
        pauseClick = true
        // 操作モードをリセット
        clickMode = CanvasActionType.NONE
    }

    /**
     * クリック状態を更新する
     * クリックしたとき、視線移動したときに呼ぶ
     * @param actionType クリック状態
     */
    fun updateClick(actionType: CanvasActionType) {
        // 固定されている場合は何もしない
        if (isFreeze) return

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