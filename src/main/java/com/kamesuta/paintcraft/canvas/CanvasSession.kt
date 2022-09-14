package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.frame.FrameRayTrace
import com.kamesuta.paintcraft.util.clienttype.ClientType
import org.bukkit.entity.Player

/**
 * キャンバスのステート
 * @param player プレイヤー
 */
class CanvasSession(val player: Player) {
    /** 前回の正確な目線の位置 (補完されていない生の位置) */
    var prevEyeLocation = player.eyeLocation

    /** 正確な目線の位置 (補完されていない生の位置) */
    var eyeLocation = player.eyeLocation

    /** 最後のエンティティ右クリック時刻 */
    var lastInteract = 0L

    /** 最後のアイテムドロップ時刻 */
    var lastDropItem = 0L

    /** 最後のエンティティ移動時刻 */
    var lastVehicleMove = 0L

    /** クライアントの種類 */
    val clientType = ClientType()

    /** 描画状態 */
    val clicking = CanvasClicking(clientType)

    /** 描画状態 */
    val drawing = CanvasDrawing()

    /** 選択中のモード */
    val mode = CanvasMode(this)

    /** レイツール */
    val rayTrace = FrameRayTrace(player, clientType)
}