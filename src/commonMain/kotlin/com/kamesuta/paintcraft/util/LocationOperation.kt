package com.kamesuta.paintcraft.util

import com.kamesuta.paintcraft.util.vec.Line3d

/**
 * 目線の位置情報の更新した座標を返す
 */
enum class LocationOperation(val operation: (Line3d, Line3d) -> Line3d) {
    /** 目線を更新しない */
    NONE({ old, _ -> old }),

    /** 目線の向きを更新 */
    LOOK({ old, new -> Line3d(old.origin, new.direction) }),

    /** 目線の位置を更新 */
    POSITION({ old, new -> Line3d(new.origin, old.direction) }),

    /** 目線の位置と向きを更新 */
    POSITION_LOOK({ _, new -> new })
}