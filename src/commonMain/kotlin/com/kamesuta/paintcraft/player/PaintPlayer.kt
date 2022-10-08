package com.kamesuta.paintcraft.player

import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.debug.DebugLocator
import java.util.*

/**
 * プレイヤーの情報を保持するクラス
 */
interface PaintPlayer {
    /** プレイヤーの名前 */
    val name: String

    /** 目線の位置 */
    val eyeLocation: Line3d

    /** スナップモード (スニーク状態) */
    val isSnapMode: Boolean

    /** ワールド */
    val world: PaintWorld

    /** プレイヤーのUUID */
    val uniqueId: UUID

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
     * デバッグ座標を初期化
     * @receiver プレイヤー
     */
    fun clearDebug()

    /**
     * プレイヤーがペンを持っているかどうかを確認する
     * @receiver プレイヤー
     * @return ペンを持っているかどうか
     */
    fun hasPencil(): Boolean

    /**
     * プレイヤーがアイテムをドロップするべきではないかどうかを確認する
     * @receiver プレイヤー
     * @return プレイヤーがアイテムをドロップするべきではない場合true
     */
    fun shouldNotDrop(): Boolean

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