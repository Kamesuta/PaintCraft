package com.kamesuta.paintcraft

import com.kamesuta.paintcraft.canvas.MapDrawer
import dev.kotx.flylib.command.Command
import org.bukkit.entity.Player
import java.awt.Color

class PaintCraftCommand : Command("paintcraft") {
    init {
        children(GiveCanvasCommand())
    }
}

class GiveCanvasCommand : Command("give") {
    init {
        usage {
            entityArgument("player")
            executes {
                val entities = typedArgs[0] as List<*>
                entities.filterIsInstance<Player>().forEach {
                    val (mapItemStack, mapDrawer) = MapDrawer.genNew(it.world)
                    it.inventory.addItem(mapItemStack)

                    mapDrawer.draw { g ->
                        g.color = Color.RED
                        g.fillRect(0, 0, 128, 128)
                        g.color = Color.WHITE
                        g.drawLine(0, 0, 128, 128)
                        g.drawRect(10, 10, 100, 100)
                        g.dispose()
                    }
                }
            }
        }
    }
}