package com.kamesuta.paintcraft.util.vec.debug

import com.kamesuta.paintcraft.util.vec.Line3d
import org.bukkit.util.Vector

/** プレイヤーの視点にあったデバッグ座標を表示します */
fun interface DebugLocatable {
    /** 座標を取得します */
    fun debugLocate(eyeLocation: Line3d, locate: (Vector) -> Unit)
}