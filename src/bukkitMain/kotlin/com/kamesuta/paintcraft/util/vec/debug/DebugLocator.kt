package com.kamesuta.paintcraft.util.vec.debug

/** デバッグの座標の更新を行うツールです */
fun interface DebugLocator {
    /**
     * デバッグシェープを描画
     * @param type 表示するデバッグの種類
     * @param location 描画するシェープ
     */
    fun locate(type: DebugLocationType, location: DebugLocatable?)
}