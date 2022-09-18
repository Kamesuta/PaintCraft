package com.kamesuta.paintcraft.util

import org.bukkit.Location

/**
 * 目線の位置情報の更新した座標を返す
 */
enum class LocationOperation(val operation: (Location, Location) -> Location) {
    /** 目線を更新しない */
    NONE({ old, _ -> old.clone() }),

    /** 目線の向きを更新 */
    LOOK({ old, new -> Location(new.world, old.x, old.y, old.z, new.yaw, new.pitch) }),

    /** 目線の位置を更新 */
    POSITION({ old, new -> Location(new.world, new.x, new.y, new.z, old.yaw, old.pitch) }),

    /** 目線の位置と向きを更新 */
    POSITION_LOOK({ _, new -> new.clone() })
}