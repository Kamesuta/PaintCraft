package com.kamesuta.paintcraft.util.vec.debug

import com.kamesuta.paintcraft.PaintCraft
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * 位置を視覚化してデバッグするためのコマンド
 */
object DebugLocationVisualizer {
    private val states: MutableMap<UUID, DebugPlayerState> = mutableMapOf()

    /** プレイヤーのステート */
    internal operator fun get(player: Player): DebugPlayerState {
        return states.getOrPut(player.uniqueId) { DebugPlayerState(player) }
    }

    /** パーティクルを発生される */
    fun registerTick() {
        Bukkit.getScheduler().runTaskTimer(PaintCraft.instance, { _ ->
            states.forEach { (_, state) -> state.draw() }
        }, 0, 4)
    }

    /** デバッグ座標を更新 */
    fun locate(player: Player, type: DebugLocationType, location: DebugLocatable) {
        get(player).location(type, location)
    }

    /** デバッグ座標を更新 */
    inline fun Player.debugLocation(f: DebugLocator.() -> Unit) {
        if (!DebugLocationType.ENABLE_DEBUG) {
            return
        }

        // デバッグ座標を更新
        f(DebugLocator { type, location ->
            location?.let { locate(this, type, it) }
        })
    }

    /** デバッグ座標をクリア */
    fun Player.clearDebugLocation(group: DebugLocationType.DebugLocationGroup) {
        if (!DebugLocationType.ENABLE_DEBUG) {
            return
        }

        get(this).clear(group)
    }
}