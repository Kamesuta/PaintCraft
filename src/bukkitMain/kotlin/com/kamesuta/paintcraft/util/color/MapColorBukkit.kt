package com.kamesuta.paintcraft.util.color

import org.bukkit.map.MapPalette

/**
 * マップパレットの色に変換
 * @return マップパレットの色 (Byte)
 */
actual fun RGBColor.toMapColor(): Byte {
    @Suppress("DEPRECATION")
    return MapPalette.matchColor(color)
}

/** BukkitのmapColorをRGBColorに変換する */
actual object MapColor {
    /**
     * マップパレットの色からRGBカラーに変換
     * @param mapColor マップパレットの色 (Byte)
     * @return RGBカラー
     */
    actual fun toRGBColor(mapColor: Byte): RGBColor {
        @Suppress("DEPRECATION")
        return RGBColor(MapPalette.getColor(mapColor))
    }
}
