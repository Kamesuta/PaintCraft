package com.kamesuta.paintcraft

import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.draw.DrawFill
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.util.DebugLocationVisualizer
import dev.kotx.flylib.command.Command
import org.bukkit.entity.Player
import org.bukkit.map.MapPalette
import java.awt.Color

class PaintCraftCommand : Command("paintcraft") {
    init {
        children(
            DebugLocationVisualizer.DebugLocationCommand(),
            GiveCanvasCommand(),
            DrawCanvasCommand(),
            FillCanvasCommand()
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

class DrawCanvasCommand : Command("draw") {
    init {
        usage {
            entityArgument("player")
            executes {
                val entities = typedArgs[0] as List<*>
                entities.filterIsInstance<Player>().forEach {
                    val mapDrawer = DrawableMapItem.get(it.inventory.itemInMainHand)

                    mapDrawer?.draw { g ->
                        g(DrawLine(128, 0, 0, 128, MapPalette.matchColor(Color.BLUE)))
                    }
                }
            }
        }
    }
}

class FillCanvasCommand : Command("fill") {
    init {
        usage {
            entityArgument("player")
            executes {
                val entities = typedArgs[0] as List<*>
                entities.filterIsInstance<Player>().forEach {
                    val mapDrawer = DrawableMapItem.get(it.inventory.itemInMainHand)

                    mapDrawer?.draw { g ->
                        g(DrawFill(64, 32, MapPalette.matchColor(Color.RED), MapPalette.matchColor(Color.YELLOW)))
                    }
                }
            }
        }
    }
}
