package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.canvas.paint.PaintPencil
import com.kamesuta.paintcraft.canvas.paint.PaintTool
import org.bukkit.entity.Player

class CanvasSession(val player: Player) {
    var tool: PaintTool = PaintPencil()
}