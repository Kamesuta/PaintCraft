package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.player.PaintPlayer

/**
 * キャンバスのステート
 */
interface ICanvasSession {
    /** プレイヤー */
    val player: PaintPlayer
}