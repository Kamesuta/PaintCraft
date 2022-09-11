package com.kamesuta.paintcraft.util.vec.debug

/** デバッグの座標の更新を行うツールです */
fun interface DebugLocator {
    /** DebugLocatable型の座標を更新 */
    fun locate(type: DebugLocationType, location: DebugLocatable?)
}