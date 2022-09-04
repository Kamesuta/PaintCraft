package com.kamesuta.paintcraft.util

import org.bukkit.util.Vector

/** デバッグの座標の更新を行うツールです */
interface DebugLocator {
    /** Vector型の座標を更新 */
    fun locate(type: DebugLocationType, location: Vector?)

    /** DebugLocatable型の座標を更新 */
    fun locate(type: DebugLocationType, location: DebugLocatable?)
}