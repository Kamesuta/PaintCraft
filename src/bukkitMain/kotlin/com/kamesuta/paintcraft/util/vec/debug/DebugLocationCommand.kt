package com.kamesuta.paintcraft.util.vec.debug

import com.kamesuta.paintcraft.util.enumValueOrNull
import dev.kotx.flylib.command.Command
import org.bukkit.Particle
import org.bukkit.entity.Player

/** 座標デバッグコマンド追加 */
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
                listOf("none").plus(
                    Particle.values()
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
