package com.kamesuta.paintcraft.canvas

import org.bukkit.Location
import org.bukkit.command.CommandSender

/**
 * プレイヤーがキャンバスに描くときのインタラクション詳細。
 * @param x X座標
 * @param y Y座標
 * @param sender インタラクションを行ったプレイヤー
 * @param blockLocation インタラクションを行ったキャンバスが貼り付いているブロックの位置
 * @param frameLocation インタラクションを行ったキャンバスフレームの位置
 * @param rightHanded プレイヤーが右手を使っているかどうか
 */
class CanvasInteraction(
    val x: Int, val y: Int,
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
    fun reCoordinate(x: Int, y: Int): CanvasInteraction {
        return CanvasInteraction(x, y, sender, blockLocation, frameLocation, actionType)
    }
}