package com.kamesuta.paintcraft.util.vec.debug

/** デバッグ座標タイプ */
enum class DebugLocationType(
    val group: DebugLocationGroup = DebugLocationGroup.CANVAS_DRAW
) {
    EYE_LOCATION,
    EYE_DIRECTION,
    EYE_LINE,
    BLOCK_HIT_LOCATION,
    CANVAS_HIT_LOCATION,
    CANVAS_PLANE,
    FRAME_LOCATION,
    FRAME_DIRECTION,
    FRAME_FACING,
    FRAME_FACING_BLOCK,
    CANVAS_LOCATION,
    CANVAS_DIRECTION,
    SEGMENT_ORIGIN,
    SEGMENT_TARGET,
    INTERSECT_LINE_ORIGIN,
    INTERSECT_LINE_TARGET,
    INTERSECT_LINE,
    INTERSECT_PLANE,
    INTERSECT_SEGMENT,
    ;

    /** デバッグ座標のグループ、座標をクリアするときに使用 */
    enum class DebugLocationGroup {
        /** キャンバス描画時 */
        CANVAS_DRAW,
    }

    companion object {
        /* デバッグを有効化 */
        const val ENABLE_DEBUG = true
    }
}

