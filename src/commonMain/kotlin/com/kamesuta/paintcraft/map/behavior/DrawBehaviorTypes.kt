package com.kamesuta.paintcraft.map.behavior

import com.kamesuta.paintcraft.map.DrawableMapRenderer
import com.kamesuta.paintcraft.palette.DrawBehaviorPalette

/**
 * 描くツール
 */
object DrawBehaviorTypes {
    /**
     * ビヘイビアのエントリ
     * @param name 名前
     * @param generator ビヘイビアの生成関数
     */
    class Desc(
        val name: String,
        val generator: (DrawableMapRenderer) -> DrawBehavior
    )

    /** 各ツールレジストリ */
    val types = mutableMapOf<String, Desc>()

    /**
     * ビヘイビアを登録する
     * @param desc 登録するビヘイビアのエントリ
     */
    fun register(desc: Desc) {
        types[desc.name] = desc
    }

    /** ペイント用ビヘイビア */
    val DrawBehaviorPaintDesc = Desc("paint", ::DrawBehaviorPaint)

    /** パレット用ビヘイビア */
    val DrawBehaviorPaletteDesc = Desc("palette", ::DrawBehaviorPalette)

    init {
        register(DrawBehaviorPaintDesc)
        register(DrawBehaviorPaletteDesc)
    }
}
