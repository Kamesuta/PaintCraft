package com.kamesuta.paintcraft.player

import com.kamesuta.paintcraft.util.vec.debug.DebugLocator

/**
 * プレイヤーの情報を保持するクラス
 */
interface PaintPlayer {
    /** スナップモード (スニーク状態) */
    val isSnapMode: Boolean

    /** デバッグ座標を更新 */
    fun debugLocation(f: DebugLocator.() -> Unit)
}