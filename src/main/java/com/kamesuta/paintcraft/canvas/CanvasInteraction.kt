package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.util.UVInt
import org.bukkit.Location
import org.bukkit.command.CommandSender

/**
 * プレイヤーがキャンバスに描くときのインタラクション詳細。
 * @param uv XY座標
 * @param sender インタラクションを行ったプレイヤー
 * @param blockLocation インタラクションを行ったキャンバスが貼り付いているブロックの位置
 * @param frameLocation インタラクションを行ったキャンバスフレームの位置
 * @param actionType プレイヤーが右手を使っているかどうか
 */
class CanvasInteraction(
    val uv: UVInt,
    val sender: CommandSender,
    val blockLocation: Location,
    val frameLocation: Location,
    val actionType: CanvasActionType
) {
    /**
     * インタラクションが発生した座標のみ変更したインタラクションを返す。
     * @param x 新しいX座標
     * @param y 新しいY座標
     * @return 座標の変更を行った新しいインタラクション
     */
    fun reCoordinate(uv: UVInt): CanvasInteraction {
        return CanvasInteraction(uv, sender, blockLocation, frameLocation, actionType)
    }
}