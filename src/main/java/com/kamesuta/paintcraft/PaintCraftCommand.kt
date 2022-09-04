package com.kamesuta.paintcraft

import com.kamesuta.paintcraft.canvas.CanvasSessionManager
import com.kamesuta.paintcraft.canvas.paint.PaintLine
import com.kamesuta.paintcraft.canvas.paint.PaintPencil
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.enumValueOrNull
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationCommand
import dev.kotx.flylib.command.Command
import org.bukkit.entity.Player

class PaintCraftCommand : Command("paintcraft") {
    init {
        children(
            DebugLocationCommand(),
            GiveCanvasCommand(),
            SwitchDrawModeCommand(),
        )
    }
}

class GiveCanvasCommand : Command("give") {
    init {
        usage {
            entityArgument("player")
            executes {
                val entities = typedArgs[0] as List<*>
                entities.filterIsInstance<Player>().forEach {
                    val mapDrawer = DrawableMapItem.create(it.world)
                    it.inventory.addItem(mapDrawer.itemStack)

                    mapDrawer.draw { g ->
//                        g(DrawRect(0, 0, 128, 128, MapPalette.matchColor(Color.RED), true))
//                        g(DrawLine(0, 0, 128, 128, MapPalette.matchColor(Color.WHITE)))
//                        g(DrawRect(10, 10, 110, 110, MapPalette.matchColor(Color.WHITE), false))
                    }
                }
            }
        }
    }
}

class SwitchDrawModeCommand : Command("switch") {
    enum class DrawMode {
        PEN,
        LINE,
    }

    init {
        usage {
            // 設定対象のプレイヤー
            entityArgument("player")
            // 対象の座標の種類
            selectionArgument("type", DrawMode.values().map { it.name.lowercase() })
            executes {
                val entities = typedArgs[0] as List<*>
                val typeName = typedArgs[1] as String
                val type = enumValueOrNull<DrawMode>(typeName.uppercase())
                if (type == null) {
                    sender.sendMessage("Invalid mode: $typeName")
                    return@executes
                }
                entities.filterIsInstance<Player>().forEach {
                    CanvasSessionManager.getSession(it).let { session ->
                        session.tool = when (type) {
                            DrawMode.PEN -> PaintPencil(session)
                            DrawMode.LINE -> PaintLine(session)
                        }
                    }
                }
            }
        }
    }
}
