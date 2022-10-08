package com.kamesuta.paintcraft.player

import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.vec.debug.DebugLocator
import org.bukkit.entity.Player

/**
 * プレイヤーの情報を保持するクラス
 * @param player プレイヤー
 */
data class PaintPlayerBukkit(val player: Player) : PaintPlayer {
    override val isSnapMode get() = player.isSneaking

    override fun debugLocation(f: DebugLocator.() -> Unit) {
        player.debugLocation(f)
    }
}