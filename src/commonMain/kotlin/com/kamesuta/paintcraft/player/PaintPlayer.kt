package com.kamesuta.paintcraft.player

import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.debug.DebugLocator

/**
 * プレイヤーの情報を保持するクラス
 */
interface PaintPlayer {
    /** 目線の位置 */
    val eyeLocation: Line3d

    /** スナップモード (スニーク状態) */
    val isSnapMode: Boolean

    /** ワールド */
    val world: PaintWorld

    /** デバッグ座標を更新 */
    fun debugLocation(f: DebugLocator.() -> Unit)

    /**
     * プレイヤーのクライアントブランドを取得します
     * @return クライアントブランド
     */
    fun getClientBrand(): String?

    /**
     * プレイヤーのバージョンを取得します
     * ViaVersionが存在しない場合はnullを返します
     * @return バージョン
     */
    fun getClientVersion(): Int?

    /**
     * プレイヤーに色情報のチャットを送信します
     * @param rgbColor RGBカラー
     * @param mapColor マップカラー
     */
    fun sendColorMessage(
        rgbColor: RGBColor,
        mapColor: Byte,
    )
}