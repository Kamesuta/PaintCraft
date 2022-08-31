package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.canvas.paint.PaintLine
import com.kamesuta.paintcraft.canvas.paint.PaintTool
import com.kamesuta.paintcraft.util.TimeWatcher
import org.bukkit.entity.Player

/**
 * キャンバスのステート
 * @param player プレイヤー
 */
class CanvasSession(val player: Player) {
    /** 正確な目線の位置 (補完されていない生の位置) */
    var eyeLocation = player.eyeLocation

    /** 塗りつぶしツール */
    var tool: PaintTool = PaintLine(this)

    /** 最後のエンティティ右クリック時刻 */
    var lastInteract = 0L

    /** 最後のエンティティ移動時刻 */
    var lastVehicleMove = 0L

    companion object {
        /** 最後のエンティティ右クリックから左クリックを無視し続ける時間 */
        val interactEntityDuration = TimeWatcher(20)

        /** 最後のエンティティ移動時刻からティックイベントを無視し続ける時間 */
        val vehicleMoveDuration = TimeWatcher(20)
    }
}