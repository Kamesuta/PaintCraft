package com.kamesuta.paintcraft

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
        server.pluginManager.registerEvents(DrawListener(), this)
    }

    companion object {
        lateinit var instance: PaintCraft
            private set
    }
}