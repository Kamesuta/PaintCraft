package com.kamesuta.paintcraft.util.color

/**
 * マップパレットの色に変換
 * @return マップパレットの色 (Byte)
 */
expect fun RGBColor.toMapColor(): Byte

/** BukkitのmapColorをRGBColorに変換する */
expect object MapColor {
    /**
     * マップパレットの色からRGBカラーに変換
     * @param mapColor マップパレットの色 (Byte)
     * @return RGBカラー
     */
    fun toRGBColor(mapColor: Byte): RGBColor
}
