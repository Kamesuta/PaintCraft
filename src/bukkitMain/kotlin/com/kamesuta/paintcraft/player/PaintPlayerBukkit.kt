package com.kamesuta.paintcraft.player

import com.kamesuta.paintcraft.util.clienttype.ClientTypeReflection
import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationType
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.clearDebugLocation
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer.debugLocation
import com.kamesuta.paintcraft.util.vec.debug.DebugLocator
import com.kamesuta.paintcraft.util.vec.toLine
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * プレイヤーの情報を保持するクラス
 * @param player プレイヤー
 */
data class PaintPlayerBukkit(val player: Player) : PaintPlayer {
    override val name: String get() = player.name

    override val eyeLocation get() = player.eyeLocation.toLine()

    override val isSnapMode get() = player.isSneaking

    override val world = PaintWorldBukkit(player.world)

    override val uniqueId = player.uniqueId

    override fun debugLocation(f: DebugLocator.() -> Unit) {
        player.debugLocation(f)
    }

    override fun getClientBrand() = ClientTypeReflection.getClientBrand(player)

    override fun getClientVersion() = ClientTypeReflection.getClientVersion(player)

    override fun clearDebug() = player.clearDebugLocation(DebugLocationType.DebugLocationGroup.CANVAS_DRAW)

    override fun hasPencil() =
        player.gameMode != GameMode.SPECTATOR && player.inventory.itemInMainHand.isPencil()

    override fun shouldNotDrop() = !hasPencil() && player.inventory.itemInMainHand.type != Material.AIR

    override fun sendColorMessage(
        rgbColor: RGBColor,
        mapColor: Byte,
    ) {
        val hexCode = rgbColor.toHexCode()
        // チャット生成
        val hexColorText = Component.text("Color Code: ")
            .color(TextColor.color(0x00FFFF))
            .append(Component.text(hexCode).color(TextColor.color(rgbColor.toCode())))
            .append(Component.text(" "))
            .append(
                Component.text("[Copy]")
                    .color(TextColor.color(0xFF7700))
                    .hoverEvent(
                        HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.text("Click to copy: $hexCode")
                        )
                    )
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, hexCode))
            )
            .append(Component.text(" "))
            .append(
                Component.text("[Replace]")
                    .color(TextColor.color(0xFF7700))
                    .hoverEvent(
                        HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.text("Click and type color code to use it")
                        )
                    )
                    .clickEvent(
                        ClickEvent.clickEvent(
                            ClickEvent.Action.SUGGEST_COMMAND,
                            "/paintcraft color @s "
                        )
                    )
            )
        val mapColorText = Component.text("Map Color: ")
            .color(TextColor.color(0x00FFFF))
            .append(Component.text("$mapColor").color(TextColor.color(rgbColor.toCode())))
            .append(Component.text(" "))
            .append(
                Component.text("[Copy]")
                    .color(TextColor.color(0xFF7700))
                    .hoverEvent(
                        HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.text("Click to copy: $mapColor")
                        )
                    )
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "$mapColor"))
            )
            .append(Component.text(" "))
            .append(
                Component.text("[Replace]")
                    .color(TextColor.color(0xFF7700))
                    .hoverEvent(
                        HoverEvent.hoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Component.text("Click and type map color to use it")
                        )
                    )
                    .clickEvent(
                        ClickEvent.clickEvent(
                            ClickEvent.Action.SUGGEST_COMMAND,
                            "/paintcraft mapcolor @s "
                        )
                    )
            )
        // チャット送信
        player.sendMessage("")
        player.sendMessage(hexColorText)
        player.sendMessage(mapColorText)
        player.sendMessage("")
    }

    companion object {
        /**
         * これはペンかどうか
         * @receiver アイテム
         * @return ペンならtrue
         */
        fun ItemStack.isPencil() = type == Material.INK_SAC
    }
}