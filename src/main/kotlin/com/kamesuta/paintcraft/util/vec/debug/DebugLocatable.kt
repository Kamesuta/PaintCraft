package com.kamesuta.paintcraft.util.vec.debug

import com.kamesuta.paintcraft.util.vec.Line3d
import org.bukkit.util.Vector

/** プレイヤーの視点にあったデバッグ座標を表示します */
fun interface DebugLocatable {
    /**
     * 座標を取得します
     * @param eyeLocation 目線
     * @param locate 描画する関数
     */
    fun debugLocate(eyeLocation: Line3d, locate: (Vector) -> Unit)
}