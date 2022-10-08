package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.player.PaintPlayer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * プレイヤーごとにキャンバスステートを保持するクラス
 */
object CanvasSessionManager {
    /**
     * プレイヤーのキャンバスステート
     * 別スレッド(パケットを受け取った時)で書き込みが発生するため、ConcurrentHashMapを使用する
     */
    private val sessions = ConcurrentHashMap<UUID, CanvasSession>()

    /**
     * プレイヤーのキャンバスステートを取得する
     * @param player プレイヤー
     * @return プレイヤーのキャンバスステート
     */
    fun getSession(player: PaintPlayer): CanvasSession {
        return sessions.getOrPut(player.uniqueId) { CanvasSession(player) }
    }
}