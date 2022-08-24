package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasInteraction
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.PaintTool.Companion.isDrawTime
import com.kamesuta.paintcraft.canvas.paint.PaintTool.Companion.now
import com.kamesuta.paintcraft.map.MapDye
import com.kamesuta.paintcraft.map.MapItem
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.map.draw.DrawRect
import com.kamesuta.paintcraft.map.mapSize
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapPalette
import java.awt.Color

class PaintPencil(override val session: CanvasSession) : PaintTool {
    private data class PaintEvent(
        val mapItem: MapItem,
        val interact: CanvasInteraction,
    )

    private var lastEvent: PaintEvent? = null
    private var lastTime = 0L
    private var drawMode: CanvasActionType? = null

    override val isDrawing: Boolean
        get() = isDrawTime(lastTime)

    override fun paint(
        itemStack: ItemStack,
        mapItem: MapItem,
        interact: CanvasInteraction,
    ) {
        // ツールを持っている場合のみ
        if (itemStack.type != Material.INK_SAC) {
            return
        }

        // Tickイベント
        tick()

        // 左右クリックした場合は状態を更新する
        if (interact.actionType != CanvasActionType.MOUSE_MOVE) {
            lastTime = now
            // 現在描いているモードを保存
            drawMode = interact.actionType
        }

        // 描く色
        val color: MapDye = MapPalette.matchColor(Color.BLACK)

        // キャンバスに描く
        mapItem.draw { g ->
            when (drawMode) {
                // 描くモードが左クリックの場合
                CanvasActionType.LEFT_CLICK -> {
                    // 全消し
                    g(DrawRect(0, 0, mapSize - 1, mapSize - 1, 0, true))
                }
                // 描くモードが右クリックの場合
                CanvasActionType.RIGHT_CLICK -> {
                    if (interact.actionType == CanvasActionType.MOUSE_MOVE) {
                        lastEvent?.let { ev ->
                            g(DrawLine(ev.interact.uv.u, ev.interact.uv.v, interact.uv.u, interact.uv.v, color))
                        }
                    }
                }
                // その他 (想定外)
                else -> {
                    // 何もしない
                }
            }
        }

        // イベントを保存
        if (lastEvent == null || interact.actionType == CanvasActionType.MOUSE_MOVE) {
            lastEvent = PaintEvent(mapItem, interact)
        }
    }

    override fun tick() {
        if (lastEvent != null && !isDrawing) {

            lastEvent = null
        }
    }
}