package com.kamesuta.paintcraft

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerMoveEvent

class DrawListener : Listener {
    @EventHandler
    fun onDraw(event: PlayerInteractEntityEvent) {
        event.player.sendMessage("Interact: ${event.player.name}")
        event.isCancelled = true
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        event.player.sendMessage("Move: ${event.player.name}")
    }
}