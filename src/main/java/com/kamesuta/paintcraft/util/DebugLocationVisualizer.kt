package com.kamesuta.paintcraft.util

import com.kamesuta.paintcraft.PaintCraft
import dev.kotx.flylib.command.Command
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import java.util.*

typealias DebugLocator = (type: DebugLocationType, location: Location?) -> Unit
typealias DebugLocatable = (locator: DebugLocator) -> Unit

/**
 * 位置を視覚化してデバッグするためのコマンド
 */
object DebugLocationVisualizer {
    private val states: MutableMap<UUID, PlayerState> = mutableMapOf()

    /** プレイヤーのステート */
    private operator fun get(player: Player): PlayerState {
        return states.getOrPut(player.uniqueId) { PlayerState(player) }
    }

    /** パーティクルを発生される */
    fun registerTick() {
        Bukkit.getScheduler().runTaskTimer(PaintCraft.instance, { _ ->
            states.forEach { (_, state) -> state.draw() }
        }, 0, 4)
    }

    /** デバッグ座標を更新 */
    fun locate(player: Player, type: DebugLocationType, location: Location?) {
        get(player).location(type, location)
    }

    /** デバッグ座標を更新 */
    inline fun Player.debugLocation(f: DebugLocatable) {
        if (!DebugLocationType.ENABLE_DEBUG) {
            return
        }

        f { type, location ->
            locate(this, type, location)
        }
    }

    /** デバッグ座標をクリア */
    fun Player.clearDebugLocation(group: DebugLocationType.DebugLocationGroup) {
        if (!DebugLocationType.ENABLE_DEBUG) {
            return
        }

        get(this).clear(group)
    }

    /** プレイヤーのデバッグ情報 */
    private class PlayerState(private val player: Player) {
        var particles: EnumMap<DebugLocationType, Particle> = EnumMap(DebugLocationType::class.java)
        private var locations: EnumMap<DebugLocationType, Location> = EnumMap(DebugLocationType::class.java)

        /** 指定タイプのデバッグ座標を更新 */
        fun location(type: DebugLocationType, location: Location?) {
            if (location == null) {
                locations.remove(type)
            } else {
                locations[type] = location.clone()
            }
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

            for ((type, location) in locations) {
                val particle = particles[type]
                if (particle != null) {
                    player.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0)
                }
            }
        }
    }

    /** コマンド追加 */
    class DebugLocationCommand : Command("debug_location") {
        init {
            usage {
                // 設定対象のプレイヤー
                entityArgument("player")
                // 対象の座標の種類
                selectionArgument("type", DebugLocationType.values().map { it.name.lowercase() })
                // 表示するパーティクルの種類
                selectionArgument(
                    "particle",
                    listOf("none").plus(Particle.values()
                        .filter { it.dataType == Void::class.java }
                        .map { it.name.lowercase() })
                )
                executes {
                    if (!DebugLocationType.ENABLE_DEBUG) {
                        sender.sendMessage("Debug location is disabled.")
                        return@executes
                    }

                    val entities = typedArgs[0] as List<*>
                    val typeName = typedArgs[1] as String
                    val particleName = typedArgs[2] as String
                    val type = enumValueOrNull<DebugLocationType>(typeName.uppercase())
                    if (type == null) {
                        sender.sendMessage("Invalid type: $typeName")
                        return@executes
                    }
                    val particle = if (particleName == "none") {
                        null
                    } else {
                        val p = enumValueOrNull<Particle>(particleName.uppercase())
                        if (p == null || p.dataType != Void::class.java) {
                            sender.sendMessage("Invalid particle: $particleName")
                            return@executes
                        }
                        p
                    }
                    entities.filterIsInstance<Player>().forEach { player ->
                        val state = DebugLocationVisualizer[player]
                        state.particles[type] = particle
                        sender.sendMessage("${player.name} ${type.name} ${if (particle != null) "enabled ${particle.name}" else "disabled"}")
                    }
                }
            }
        }
    }
}