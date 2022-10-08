package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.util.clienttype.ClientType
import com.kamesuta.paintcraft.util.vec.Line3d

interface FrameEntity {
    /** アイテムフレームの場所 */
    val location: Line3d

    /** アイテムフレームのブロック上の中心場所と向き */
    val blockLocation: Line3d

    /**
     * キャンバスフレームの平面の座標を求める
     * アイテムフレームの座標からキャンバス平面の座標を計算する
     * (tpでアイテムフレームを回転したときにずれる)
     * @param itemFrame アイテムフレーム
     * @param clientType クライアントの種類
     * @return キャンバスフレームの平面の座標
     */
    fun toFrameLocation(clientType: ClientType): FrameLocation

    /**
     * キャンバスの回転を計算
     * @param itemFrame アイテムフレーム
     * @param clientType クライアントの種類
     * @return キャンバスの回転
     */
    fun getCanvasRotation(clientType: ClientType): Pair<Float, Float>

    /**
     * アイテムフレームのFrameRotationを取得
     * @param clientType クライアントの種類
     * @return アイテムフレームのFrameRotation
     */
    fun getFrameRotation(clientType: ClientType): FrameRotation

    /**
     * 書き込み可能マップであれば取得する
     * @return 書き込み可能マップ
     */
    fun toDrawableMapItem(): DrawableMapItem?
}