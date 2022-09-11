package com.kamesuta.paintcraft.util.vec.debug

/** デバッグ座標タイプ */
enum class DebugLocationType(
    val group: DebugLocationGroup = DebugLocationGroup.CANVAS_DRAW
) {
    EYE_LINE,
    BLOCK_HIT_LOCATION,
    CANVAS_HIT_LOCATION,
    CANVAS_PLANE,
    FRAME_LINE,
    FRAME_FACING,
    FRAME_FACING_BLOCK,
    CANVAS_LINE,
    SEGMENT_LINE,
    INTERSECT_LINE,
    INTERSECT_PLANE,
    INTERSECT_SEGMENT,
    INTERSECT_SEGMENT_CANVAS,
    SEARCH_LOCATION,
    SEARCH_SEGMENT,
    SEARCH_CANVAS_LINE,
    SNAP_SEGMENT,
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

