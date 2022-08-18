package com.kamesuta.paintcraft

import com.kamesuta.paintcraft.canvas.MapItem
import dev.kotx.flylib.command.Command
import org.bukkit.entity.Player
import java.awt.Color

class PaintCraftCommand : Command("paintcraft") {
    init {
        children(GiveCanvasCommand(), DrawCanvasCommand())
    }
}

class GiveCanvasCommand : Command("give") {
    init {
        usage {
            entityArgument("player")
            executes {
                val entities = typedArgs[0] as List<*>
                entities.filterIsInstance<Player>().forEach {
                    val mapDrawer = MapItem.create(it.world)
                    it.inventory.addItem(mapDrawer.itemStack)

                    mapDrawer.draw { g ->
                        g.color = Color.RED
                        g.fillRect(0, 0, 128, 128)
                        g.color = Color.WHITE
                        g.drawLine(0, 0, 128, 128)
                        g.drawRect(10, 10, 100, 100)
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
                    val mapDrawer = MapItem.get(it.inventory.itemInMainHand)

                    mapDrawer?.draw { g ->
                        g.color = Color.BLUE
                        g.drawLine(128, 0, 0, 128)
                    }
                }
            }
        }
    }
}