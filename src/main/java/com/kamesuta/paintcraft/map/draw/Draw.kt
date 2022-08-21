package com.kamesuta.paintcraft.map.draw

import org.bukkit.map.MapCanvas

fun interface Draw {
    fun draw(canvas: MapCanvas)
}