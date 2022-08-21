package com.kamesuta.paintcraft

import com.kamesuta.paintcraft.canvas.CanvasDrawListener
import dev.kotx.flylib.flyLib
import org.bukkit.plugin.java.JavaPlugin

class PaintCraft : JavaPlugin() {
    init {
        flyLib {
            command(PaintCraftCommand())
        }
    }

    override fun onEnable() {
        instance = this
        server.pluginManager.registerEvents(CanvasDrawListener(), this)
    }

    companion object {
        lateinit var instance: PaintCraft
            private set
    }
}