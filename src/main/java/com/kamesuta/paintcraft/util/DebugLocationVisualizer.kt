package com.kamesuta.paintcraft.util

import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.util.vec.Line3d.Companion.toLine
import dev.kotx.flylib.command.Command
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*

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
    fun locate(player: Player, type: DebugLocationType, location: DebugLocatable) {
        get(player).location(type, location)
    }

    /** デバッグ座標を更新するツール */
    class DebugLocatorImpl(private val player: Player) : DebugLocator {
        /** Vector型の座標を更新 */
        override fun locate(type: DebugLocationType, location: Vector?) {
            location?.let { locate(player, type) { _, locate -> locate(it) } }
        }

        /** DebugLocatable型の座標を更新 */
        override fun locate(type: DebugLocationType, location: DebugLocatable?) {
            location?.let { locate(player, type, it) }
        }
    }

    /** デバッグ座標を更新 */
    inline fun Player.debugLocation(f: DebugLocator.() -> Unit) {
        if (!DebugLocationType.ENABLE_DEBUG) {
            return
        }

        // デバッグ座標を更新
        f(DebugLocatorImpl(this))
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
        private var locations: EnumMap<DebugLocationType, MutableList<DebugLocatable>> =
            EnumMap(DebugLocationType::class.java)

        /** 指定タイプのデバッグ座標を更新 */
        fun location(type: DebugLocationType, location: DebugLocatable) {
            locations.computeIfAbsent(type) { mutableListOf() } += location
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