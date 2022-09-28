package com.kamesuta.paintcraft.util.vec.debug

import com.kamesuta.paintcraft.util.vec.Line3d.Companion.toLine
import org.bukkit.Particle
import org.bukkit.entity.Player
import java.util.*

/** プレイヤーのデバッグ情報 */
internal class DebugPlayerState(private val player: Player) {
    var particles: EnumMap<DebugLocationType, Particle> = EnumMap(DebugLocationType::class.java)
    private var locations: EnumMap<DebugLocationType, MutableList<DebugLocatable>> =
        EnumMap(DebugLocationType::class.java)

    /** 指定タイプのデバッグ座標を更新 */
    fun location(type: DebugLocationType, location: DebugLocatable) {
        locations.getOrPut(type) { mutableListOf() } += location
    }

    /** 指定グループのデバッグ座標をクリア */
    fun clear(group: DebugLocationType.DebugLocationGroup) {
        locations.keys.removeIf { it.group == group }
    }

    /** パーティクルを発生させる */
    fun draw() {
        if (!DebugLocationType.ENABLE_DEBUG) {
            return
        }

        val eyeLocation = player.eyeLocation.toLine()
        val world = player.world
        for ((type, location) in locations) {
            val particle = particles[type]
            if (particle != null) {
                location.forEach { locator ->
                    locator.debugLocate(eyeLocation) {
                        player.spawnParticle(particle, it.toLocation(world), 1, 0.0, 0.0, 0.0, 0.0)
                    }
                }
            }
        }
    }
}
