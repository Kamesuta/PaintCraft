package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.map.behavior.DrawBehavior
import com.kamesuta.paintcraft.map.draw.Draw
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.map.image.PixelImageLayer
import com.kamesuta.paintcraft.player.PaintPlayer
import com.kamesuta.paintcraft.util.vec.Vec3d

/**
 * 書き込み可能レンダラー
 */
interface DrawableMapRenderer {
    /** 書き込み中のピクセルデータのレイヤー */
    val mapLayer: PixelImageLayer<PaintPlayer>

    /** 描画ツール */
    val behavior: DrawBehavior

    /**
     * ベースイメージへ書き込みを行う
     * @param draw 描画関数
     */
    fun drawBase(draw: Draw)

    /**
     * 書き込みを行うオブジェクトを取得する
     * @param player 描き込んだプレイヤー
     * @return 書き込みを行うオブジェクト
     */
    fun drawer(player: PaintPlayer): Drawable

    /**
     * プレイヤーに更新を通知する
     * @param location アイテムフレームの位置
     */
    fun updatePlayer(location: Vec3d)
}