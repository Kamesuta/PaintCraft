package com.kamesuta.paintcraft.util

enum class DebugLocationType(val group: DebugLocationGroup = DebugLocationGroup.CANVAS_DRAW) {
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
    ;

    enum class DebugLocationGroup {
        CANVAS_DRAW,
    }

    companion object {
        const val ENABLE_DEBUG = true
    }
}

