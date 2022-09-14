package com.kamesuta.paintcraft.palette

/** 調整中の項目 */
enum class PaletteAdjustingType {
    /** なし */
    NONE,

    /** 色相 */
    HUE,

    /** 彩度/明度 */
    SATURATION_BRIGHTNESS,

    /** 保存されたパレット */
    STORED_PALETTE,

    /** 透明色 */
    TRANSPARENT_COLOR,

    /** ピッカー */
    COLOR_PICKER_COLOR,

    /** カラーコード */
    COLOR_CODE,
}