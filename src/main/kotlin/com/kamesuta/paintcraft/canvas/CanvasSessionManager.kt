package com.kamesuta.paintcraft.canvas

import org.bukkit.entity.Player
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
    fun getSession(player: Player): CanvasSession {
        return sessions.computeIfAbsent(player.uniqueId) { CanvasSession(player) }
    }
}