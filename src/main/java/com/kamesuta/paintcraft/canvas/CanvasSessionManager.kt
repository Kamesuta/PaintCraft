package com.kamesuta.paintcraft.canvas

import org.bukkit.entity.Player
import java.util.UUID

object CanvasSessionManager {
    private val sessions = HashMap<UUID, CanvasSession>();

    fun getSession(player: Player): CanvasSession {
        return sessions.computeIfAbsent(player.uniqueId) { CanvasSession(player) }
    }
}