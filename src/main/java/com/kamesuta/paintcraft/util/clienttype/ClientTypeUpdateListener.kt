package com.kamesuta.paintcraft.util.clienttype

import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.CanvasSessionManager
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * クライアントの種類をゲーム参加時、またはプラグインロード時に更新します。
 */
class ClientTypeUpdateListener : Listener {
    /** ゲーム参加時に更新 */
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        CanvasSessionManager.getSession(event.player).updateClientType()
    }

    /** 全員更新 */
    fun updateAll() {
        for (player in Bukkit.getOnlinePlayers()) {
            CanvasSessionManager.getSession(player).updateClientType()
        }
    }

    companion object {
        /** クライアントの種類の情報を更新する */
        private fun CanvasSession.updateClientType() {
            clientBrand = ClientBrandReflection.getClientBrand(player)
            clientType = ClientType[clientBrand]
        }
    }
}