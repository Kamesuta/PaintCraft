package com.kamesuta.paintcraft.map.image

/**
 * キーごとにレイヤーを分離します
 * 複数プレイヤーが同時にキャンバスに描画することをサポートします
 * @param base レイヤーのベース
 */
class PixelImageLayer<T>(private val base: PixelImageMapBuffer) {
    /** レイヤー */
    private val layers = mutableListOf<Pair<T, PixelImageMapBuffer>>()

    /** プレイヤー->レイヤーのマップ */
    private val layerMap = mutableMapOf<T, PixelImageMapBuffer>()

    /** キャッシュ */
    private val cache = PixelImageMapBuffer()

    /**
     * レイヤーを追加する (nullの場合削除)
     * @param layer 追加するレイヤー
     */
    operator fun set(key: T, layer: PixelImageMapBuffer?) {
        if (layer != null) {
            layerMap.put(key, layer)
                ?: return
            layers.add(key to layer)
        } else {
            layerMap.remove(key)
                ?: return
            layers.removeIf { it.first == key }
        }
    }

    /**
     * レイヤーを取得する
     * @return レイヤー
     */
    fun get(key: T): PixelImageMapBuffer? = layerMap[key]

    /**
     * レイヤーをベースレイヤーに適用する
     * @param key プレイヤー
     */
    fun apply(key: T) {
        val layer = layerMap.remove(key)
            ?: return
        layers.removeIf { it.first == key }
        base.drawPixelImage(0.0, 0.0, layer)
    }

    /** レイヤーを削除する */
    fun clear() {
        layers.clear()
        layerMap.clear()
    }

    /**
     * レイヤーを合成する
     * @return 合成したレイヤー
     */
    fun compose(): PixelImageMapBuffer {
        cache.dirty.clear()
        base.copyTo(cache)
        layers.forEach { cache.drawPixelImage(0.0, 0.0, it.second) }
        return cache
    }
}