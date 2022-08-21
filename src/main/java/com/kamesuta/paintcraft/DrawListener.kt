package com.kamesuta.paintcraft

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasRotation
import com.kamesuta.paintcraft.canvas.CanvasSessionManager
import com.kamesuta.paintcraft.map.MapItem
import com.kamesuta.paintcraft.map.mapSize
import com.kamesuta.paintcraft.util.UV
import com.kamesuta.paintcraft.util.UVInt
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Rotation
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.roundToLong

class DrawListener : Listener {
    @EventHandler
    fun onAttack(attack: EntityDamageByEntityEvent) {
        val player = attack.damager as? Player
            ?: return
        if (player.inventory.itemInMainHand.type != Material.INK_SAC) {
            return
        }
        val itemFrame = attack.entity as? ItemFrame
            ?: return
        val item = itemFrame.item
        if (item.type != Material.FILLED_MAP) return
        val mapItem = MapItem.get(item)
            ?: return
        val (rawUV, _) = calculateUV(player.eyeLocation, itemFrame.location)
        val uv = transformUV(itemFrame.rotation, rawUV)
            ?: return
        manipulate(itemFrame, mapItem, uv, player, CanvasActionType.LEFT_CLICK)
        attack.isCancelled = true
    }

    @EventHandler
    fun onInteract(interact: PlayerInteractEvent) {
        val player = interact.player
        if (player.inventory.itemInMainHand.type != Material.INK_SAC) {
            return
        }
        val playerEyePos = player.eyeLocation
        val playerDirection = playerEyePos.direction

        val clickedBlock = interact.clickedBlock
        val clickedBlockHitLocation = interact.interactionPoint

        val center: Location
        val boxSize: Vector
        val hitLocation: Location?
        if (clickedBlock != null && clickedBlockHitLocation != null) {
            val blockCenter = clickedBlock.location.add(0.5, 0.5, 0.5).clone()
            center = playerEyePos.clone().add(blockCenter).multiply(0.5)
            boxSize = blockCenter.clone().subtract(playerEyePos).toVector()
            hitLocation = interact.interactionPoint
        } else {
            center = playerEyePos.clone().add(playerDirection.clone().multiply(2.0))
            boxSize = playerDirection.clone().multiply(4.0)
            val ray = playerEyePos.world.rayTraceBlocks(playerEyePos, playerEyePos.direction, boxSize.length() * 2.0 + 1.0)
            hitLocation = ray?.hitPosition?.toLocation(playerEyePos.world)
        }
        val entities = center.world.getNearbyEntities(
            center, abs(boxSize.x) + 1, abs(boxSize.y) + 1, abs(boxSize.z) + 1
        )
            // Check containing map.
            .filterIsInstance<ItemFrame>()
            .filter { it.item.type == Material.FILLED_MAP }
            .sortedBy { it.location.distance(playerEyePos) }

        for (itemFrame in entities) {
            // Check vector.
            if (playerDirection.dot(Vector(itemFrame.facing.modX, itemFrame.facing.modY, itemFrame.facing.modZ)) > 0) {
                continue
            }
            val mapItem = MapItem.get(itemFrame.item)
                ?: continue
            val (rawUV, look) = calculateUV(player.eyeLocation, itemFrame.location)
            val uv = transformUV(itemFrame.rotation, rawUV)
                ?: continue
            if (hitLocation != null
                && hitLocation.distance(playerEyePos) < itemFrame.location.clone().add(look).distance(playerEyePos)
            ) {
                continue
            }
            manipulate(
                itemFrame, mapItem, uv, player,
                when (interact.action) {
                    Action.RIGHT_CLICK_BLOCK -> CanvasActionType.RIGHT_CLICK
                    Action.RIGHT_CLICK_AIR -> CanvasActionType.RIGHT_CLICK
                    Action.PHYSICAL -> continue
                    else -> CanvasActionType.LEFT_CLICK
                }
            )
            interact.isCancelled = true
            break
        }
    }

    @EventHandler
    fun onInteractEntity(interact: PlayerInteractEntityEvent) {
        val itemFrame = interact.rightClicked as? ItemFrame
            ?: return
        val player = interact.player
        if (player.inventory.itemInMainHand.type != Material.INK_SAC) {
            return
        }
        if (itemFrame.item.type != Material.FILLED_MAP) {
            return
        }
        val mapItem = MapItem.get(itemFrame.item)
            ?: return
        val (rawUV, _) = calculateUV(player.eyeLocation, itemFrame.location)
        val uv = transformUV(itemFrame.rotation, rawUV)
            ?: return
        manipulate(itemFrame, mapItem, uv, player, CanvasActionType.RIGHT_CLICK)
        interact.isCancelled = true
    }

    private fun manipulate(
        itemFrameEntity: ItemFrame,
        mapItem: MapItem,
        uv: UVInt,
        player: Player,
        actionType: CanvasActionType
    ) {
        // Calculate block location
        val itemFrame = itemFrameEntity.location
        val blockLocation = itemFrame.clone().add(
            -0.5 * itemFrameEntity.facing.modX,
            -0.5 * itemFrameEntity.facing.modY,
            -0.5 * itemFrameEntity.facing.modZ,
        )
        val interact = CanvasInteraction(uv, player, blockLocation, itemFrame, actionType)

        // Paint on canvas.
        val session = CanvasSessionManager.getSession(player)
        session.tool.paint(player.inventory.itemInMainHand, mapItem, interact, session)
    }

    private fun transformUV(rotation: Rotation, uv: UV): UVInt? {
        val rot: CanvasRotation = CanvasRotation.fromRotation(rotation)
        val q = UV(rot.u(uv) + 0.5, rot.v(uv) + 0.5)
        val x = (q.u * mapSize).toInt()
        if (x >= mapSize || x < 0) return null
        val y = (q.v * mapSize).toInt()
        if (y >= mapSize || y < 0) return null
        return UVInt(x, y)
    }

    private fun calculateUV(playerEyePos: Location, itemFrame: Location): Pair<UV, Vector> {
        val playerDirection = playerEyePos.direction
        val itemFrameDirection = itemFrame.direction

        // Calculate canvas direction.
        val frameDirection = itemFrameDirection.let {
            val x = it.x.roundToLong().toDouble()
            val y = it.y.roundToLong().toDouble()
            val z = it.z.roundToLong().toDouble()
            Vector(x, y, z)
        }

        // Calculate bias vector.
        val bias = playerEyePos.toVector().subtract(itemFrame.toVector())

        // Do intersection.
        val v1 = frameDirection.clone().dot(bias)
        val v0 = frameDirection.clone().dot(playerDirection)
        val miu = v1 / v0 + 0.04
        val look = bias.clone().subtract(playerDirection.clone().multiply(miu))

        // Calculate uv coordination.
        val u = if (abs(frameDirection.x) > abs(frameDirection.z)) {
            if (frameDirection.x > 0)
                -look.z // 西向き
            else
                look.z // 東向き
        } else {
            if (abs(frameDirection.y) > 0)
                look.x // 上向き, 下向き
            else if (frameDirection.z > 0)
                look.x // 北向き
            else
                -look.x // 南向き
        }
        val v = if (abs(frameDirection.y) > 0) {
            if (frameDirection.y > 0)
                look.z // 上向き
            else
                -look.z // 下向き
        } else {
            -look.y // 横向き
        }
        return UV(u, v) to look
    }
}