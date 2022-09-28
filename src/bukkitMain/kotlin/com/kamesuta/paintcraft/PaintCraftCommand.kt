package com.kamesuta.paintcraft

import com.kamesuta.paintcraft.canvas.CanvasSessionManager
import com.kamesuta.paintcraft.canvas.paint.PaintFill
import com.kamesuta.paintcraft.canvas.paint.PaintLine
import com.kamesuta.paintcraft.canvas.paint.PaintPencil
import com.kamesuta.paintcraft.canvas.paint.PaintRect
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.behavior.DrawBehaviorTypes.DrawBehaviorPaintDesc
import com.kamesuta.paintcraft.map.behavior.DrawBehaviorTypes.DrawBehaviorPaletteDesc
import com.kamesuta.paintcraft.map.image.debug.PixelImageManualTest
import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.enumValueOrNull
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationCommand
import dev.kotx.flylib.command.Command
import dev.kotx.flylib.command.CommandContext
import dev.kotx.flylib.command.arguments.StringArgument
import org.bukkit.NamespacedKey
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class PaintCraftCommand : Command("paintcraft") {
    init {
        children(
            DebugLocationCommand(),
            GiveCanvasCommand(),
            SwitchDrawModeCommand(),
            PaletteCanvasCommand(),
            MigrateCanvasCommand(),
            ColorCommand(),
            MapColorCommand(),
            ThicknessCommand(),
            DebugPlaceCommand(),
            DebugImageVisualizerCommand(),
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
                    val mapDrawer = DrawableMapItem.create(it.world, DrawBehaviorPaintDesc)
                    it.inventory.addItem(mapDrawer.itemStack)
                }
            }
        }
    }
}

class PaletteCanvasCommand : Command("palette") {
    init {
        usage {
            entityArgument("player")
            executes {
                val entities = typedArgs[0] as List<*>
                entities.filterIsInstance<Player>().forEach {
                    val mapDrawer = DrawableMapItem.create(it.world, DrawBehaviorPaletteDesc)
                    it.inventory.addItem(mapDrawer.itemStack)
                }
            }
        }
    }
}

class MigrateCanvasCommand : Command("migrate") {
    init {
        usage {
            entityArgument("entity")
            executes {
                val entities = typedArgs[0] as List<*>
                entities.filterIsInstance<ItemFrame>().forEach { itemFrame ->
                    val item = itemFrame.item
                    item.editMeta {
                        val oldGroupIdKey = NamespacedKey(PaintCraft.instance, "paint_group_id")
                        it.persistentDataContainer.get(oldGroupIdKey, PersistentDataType.INTEGER) ?: return@editMeta
                        it.persistentDataContainer.set(
                            NamespacedKey(PaintCraft.instance, "type"),
                            PersistentDataType.STRING,
                            "paint"
                        )
                        val newGroupIdKey = NamespacedKey(PaintCraft.instance, "group_id")
                        it.persistentDataContainer.set(
                            newGroupIdKey,
                            PersistentDataType.INTEGER,
                            it.persistentDataContainer.get(oldGroupIdKey, PersistentDataType.INTEGER) ?: 1
                        )
                        it.persistentDataContainer.remove(
                            oldGroupIdKey,
                        )
                    }
                    itemFrame.setItem(item)
                }
            }
        }
    }
}

class SwitchDrawModeCommand : Command("switch") {
    enum class DrawMode {
        PEN,
        LINE,
        RECT,
        FILL,
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
                        session.mode.tool = when (type) {
                            DrawMode.PEN -> PaintPencil(session)
                            DrawMode.LINE -> PaintLine(session)
                            DrawMode.RECT -> PaintRect(session)
                            DrawMode.FILL -> PaintFill(session)
                        }
                    }
                }
            }
        }
    }
}

class ColorCommand : Command("color") {
    init {
        usage {
            // 設定対象のプレイヤー
            entityArgument("player")
            // 設定対象のプレイヤー
            stringArgument("color", StringArgument.Type.PHRASE)
            executes {
                val entities = typedArgs[0] as List<*>
                val colorName = typedArgs[1] as String
                val color = RGBColor.fromHexCode(colorName)
                if (color == null) {
                    sender.sendMessage("Invalid color: $colorName")
                    return@executes
                }
                entities.filterIsInstance<Player>().forEach {
                    val session = CanvasSessionManager.getSession(it)
                    session.mode.mapColor = color.toMapColor()
                }
            }
        }
    }
}

class MapColorCommand : Command("mapcolor") {
    init {
        usage {
            // 設定対象のプレイヤー
            entityArgument("player")
            // 設定対象のプレイヤー
            integerArgument("color", Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt())
            executes {
                val entities = typedArgs[0] as List<*>
                val color = (typedArgs[1] as Int).toByte()
                if (color > -21 && color < 0) {
                    sender.sendMessage("Invalid color: $color")
                    return@executes
                }
                entities.filterIsInstance<Player>().forEach {
                    val session = CanvasSessionManager.getSession(it)
                    session.mode.mapColor = color
                }
            }
        }
    }
}

class ThicknessCommand : Command("thickness") {
    init {
        usage {
            // 設定対象のプレイヤー
            entityArgument("player")
            // 設定対象のプレイヤー
            doubleArgument("thickness", 0.0, 128.0)
            executes {
                val entities = typedArgs[0] as List<*>
                val thickness = typedArgs[1] as Double
                entities.filterIsInstance<Player>().forEach {
                    val session = CanvasSessionManager.getSession(it)
                    session.mode.thickness = thickness
                }
            }
        }
    }
}

class DebugPlaceCommand : Command("debug_place") {
    init {
        usage {
            // 設定対象のプレイヤー
            entityArgument("player")
            // 範囲
            integerArgument("radius")
            executes {
                val entities = typedArgs[0] as List<*>
                val radius = typedArgs[1] as Int
                if (radius < 0) {
                    sender.sendMessage("Invalid radius: $radius")
                    return@executes
                }
                entities.filterIsInstance<Player>().forEach {
                    val block = it.location.block
                    val world = block.world
                    for (x in -radius..radius) {
                        for (z in -radius..radius) {
                            val target = block.getRelative(x, 0, z)
                            // ブロックが空気でないならスキップ
                            if (!target.type.isAir) continue
                            // すでにアイテムフレームが置かれているならスキップ
                            val center = target.location.add(0.5, 0.5, 0.5)
                            val others = center.getNearbyEntitiesByType(ItemFrame::class.java, 0.5, 0.5, 0.5)
                            if (others.isNotEmpty()) continue
                            // アイテムフレームを置く
                            val entity = world.spawn(center, ItemFrame::class.java)
                            // マップを作成してアイテムフレームに設定
                            val map = DrawableMapItem.create(world, DrawBehaviorPaintDesc)
                            entity.setItem(map.itemStack)
                        }
                    }
                }
            }
        }
    }
}

class DebugImageVisualizerCommand : Command("debug_image_visualizer") {
    override fun CommandContext.execute() {
        PixelImageManualTest.startTool()
    }
}
