package com.kamesuta.paintcraft.map.image.debug

import com.kamesuta.paintcraft.map.image.PixelImage

/**
 * デバッグ用のピクセルイメージ
 * テストクラスから表示できるようにするために作成
 */
var debugPixelImage: PixelImage? = null
    private set(value) {
        field = value
        onDebugPixelImageChanged?.invoke()
    }

/** デバッグ用のピクセルイメージが変更されたあとのコールバック */
var onDebugPixelImageChanged: (() -> Unit)? = null