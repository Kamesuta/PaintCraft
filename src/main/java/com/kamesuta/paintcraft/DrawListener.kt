package com.kamesuta.paintcraft

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasRotation
import com.kamesuta.paintcraft.canvas.CanvasSessionManager
import com.kamesuta.paintcraft.map.MapItem
import com.kamesuta.paintcraft.map.mapSize
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

class DrawListener : Listener {
    @EventHandler
    fun onAttack(attack: EntityDamageByEntityEvent) {
        if (attack.damager !is Player) return
        val itemFrame = attack.entity as ItemFrame?
            ?: return
        val item = itemFrame.item
        if (item.type != Material.FILLED_MAP) return
        val mapItem = MapItem.get(item)
            ?: return
        val player = attack.damager as Player
        if (manipulate(itemFrame, mapItem, player, CanvasActionType.LEFT_CLICK)) {
            attack.isCancelled = true
        }
    }

    @EventHandler
    fun onInteract(interact: PlayerInteractEvent) {
        val playerEyePos = interact.player.location
        val yaw = (playerEyePos.yaw + 90) * Math.PI / 180
        val pitch = -playerEyePos.pitch * Math.PI / 180
        val a = cos(yaw) * cos(pitch)
        val b = sin(pitch)
        val c = sin(yaw) * cos(pitch)
        val clickedBlock = interact.clickedBlock
        val (center, boxSize) = if (clickedBlock != null) {
            val blockCenter = clickedBlock.location.add(0.5, 0.5, 0.5).clone()
            val center = playerEyePos.clone().add(blockCenter).multiply(0.5)
            val boxSize = playerEyePos.clone().multiply(-1.0).add(blockCenter)
            center to boxSize
        } else {
            val center = playerEyePos.clone().add(a * 2, b * 2, c * 2)
            val boxSize = Location(playerEyePos.world, a * 4, b * 4, c * 4)
            center to boxSize
        }
        val entities = center.world.getNearbyEntities(
            center, abs(boxSize.x) + 1, abs(boxSize.y) + 1, abs(boxSize.z) + 1
        )
        for (itemFrame in entities) {
            // Check containing map.
            if (itemFrame is ItemFrame) {
                if (itemFrame.item.type != Material.FILLED_MAP) continue

                // Check vector.
                val vecFrameX = itemFrame.facing.modX
                val vecFrameZ = itemFrame.facing.modZ
                if (vecFrameX * a + vecFrameZ * c > 0) continue
                val mapItem = MapItem.get(itemFrame.item)
                    ?: continue
                if (manipulate(
                        itemFrame, mapItem, interact.player,
                        when (interact.action) {
                            Action.RIGHT_CLICK_BLOCK -> CanvasActionType.RIGHT_CLICK
                            Action.RIGHT_CLICK_AIR -> CanvasActionType.RIGHT_CLICK
                            Action.PHYSICAL -> continue
                            else -> CanvasActionType.LEFT_CLICK
                        }
                    )
                ) {
                    interact.isCancelled = true
                    break
                }
            }
        }
    }

    @EventHandler
    fun onInteractEntity(interact: PlayerInteractEntityEvent) {
        if (interact.rightClicked is ItemFrame) {
            val itemFrame = interact.rightClicked as ItemFrame
            if (itemFrame.item.type != Material.FILLED_MAP) return
            val mapItem = MapItem.get(itemFrame.item)
                ?: return
            if (manipulate(itemFrame, mapItem, interact.player, CanvasActionType.RIGHT_CLICK)) interact.isCancelled = true
        }
    }

    private fun manipulate(
        itemFrameEntity: ItemFrame,
        mapItem: MapItem,
        player: Player,
        actionType: CanvasActionType
    ): Boolean {
        // Calculate looking direction.
        val itemFrame = itemFrameEntity.location
        val playerEyePos = player.location.add(0.0, player.eyeHeight, 0.0)
        return calculateUV(playerEyePos, itemFrame) { u: Double, v: Double ->
            // transform uv.
            val rotation: CanvasRotation = CanvasRotation.fromRotation(itemFrameEntity.rotation)
            val up: Double = rotation.u(u, v)
            val vp: Double = rotation.v(u, v)
            val uq = up + 0.5
            val vq = vp + 0.5
            val x = (uq * mapSize).toInt()
            if (x >= mapSize || x < 0) return@calculateUV false
            val y = (vq * mapSize).toInt()
            if (y >= mapSize || y < 0) return@calculateUV false

            // Calculate block location
            val blockLocation = itemFrame.clone().add(
                -0.5 * itemFrameEntity.facing.modX,
                0.0, -0.5 * itemFrameEntity.facing.modZ
            )
            val interact = CanvasInteraction(x, y, player, blockLocation, itemFrame, actionType)

            // Paint on canvas.
            val session = CanvasSessionManager.getSession(player)
            session.tool.paint(player.inventory.itemInMainHand, mapItem, interact, session)
            true
        }
    }

    private fun <T> calculateUV(playerEyePos: Location, itemFrame: Location, function: (Double, Double) -> T): T {
        val yaw = (playerEyePos.yaw + 90) * Math.PI / 180
        val pitch = -playerEyePos.pitch * Math.PI / 180
        val a = cos(yaw) * cos(pitch)
        val b = sin(pitch)
        val c = sin(yaw) * cos(pitch)

        // Calculate canvas direction.
        val dir = Math.toRadians(itemFrame.yaw + 90.0)
        val A = cos(dir).roundToLong().toDouble()
        val C = sin(dir).roundToLong().toDouble()


        // Calculate bias vector.
        val x0 = playerEyePos.x - itemFrame.x
        val y0 = playerEyePos.y - itemFrame.y
        val z0 = playerEyePos.z - itemFrame.z

        // Do intersection.
        val v1 = A * x0 + C * z0
        val v0 = A * a + C * c
        val miu = -v1 / v0 - 0.04
        val xLook = x0 + miu * a
        val yLook = y0 + miu * b
        val zLook = z0 + miu * c

        // Calculate uv coordination.
        val v = -yLook
        val u: Double = if (abs(A) > abs(C)) {
            if (A > 0) -zLook else zLook
        } else {
            if (C > 0) xLook else -xLook
        }
        return function(u, v)
    }
}