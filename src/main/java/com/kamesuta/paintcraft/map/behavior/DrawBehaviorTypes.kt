package com.kamesuta.paintcraft.map.behavior

/**
 * 描くツール
 */
object DrawBehaviorTypes {
    /** 各ツールレジストリ */
    val types = mutableMapOf<String, DrawBehavior>()

    /**
     * ビヘイビアを登録する
     * @param type ビヘイビア
     */
    fun register(type: DrawBehavior) {
        types[type.name] = type
    }

    init {
        register(DrawBehaviorPaint)
        register(DrawBehaviorPalette)
    }
}
