package com.kamesuta.paintcraft.util

enum class DebugLocationType(val group: DebugLocationGroup = DebugLocationGroup.CANVAS_DRAW) {
    EYE_LOCATION,
    EYE_DIRECTION,
    BLOCK_HIT_LOCATION,
    CANVAS_HIT_LOCATION,
    ;

    enum class DebugLocationGroup {
        CANVAS_DRAW,
    }

    companion object {
        const val ENABLE_DEBUG = true
    }
}

