package com.kamesuta.paintcraft.map.image

/**
 * キーごとにレイヤーを分離します
 * 複数プレイヤーが同時にキャンバスに描画することをサポートします
 * @param base レイヤーのベース
 */
class PixelImageLayer<T>(val base: PixelImageMapBuffer) {
    /** レイヤー */
    private val layers = mutableListOf<Pair<T, PixelImageMapBuffer>>()

    /** プレイヤー->レイヤーのマップ */
    private val layerMap = mutableMapOf<T, PixelImageMapBuffer>()

    /**
     * レイヤーを取得または作成し、変更なし状態にする
     * @return レイヤー
     */
    operator fun get(key: T): PixelImageMapBuffer {
        // レイヤーが存在する場合はそれを使用
        val layer = layerMap[key]
        // レイヤーが存在しない場合は作成
            ?: PixelImageMapBuffer().also {
                layerMap.put(key, it)
                    ?: layers.add(key to it)
            }
        // レイヤーを変更なし状態で初期化
        layer.clearToUnchanged()
        // レイヤーを返す
        return layer
    }

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

    /**
     * 変更を破棄する
     * @param key プレイヤー
     */
    fun reset(key: T) {
        layerMap.remove(key)
            ?: return
        layers.removeIf { it.first == key }
    }

    /**
     * レイヤーを合成する
     * @return 合成したレイヤー
     */
    fun compose(output: PixelImageMapBuffer) {
        // まずコピーする
        base.copyTo(output)
        // コピーした後に変更フラグをリセット
        output.dirty.clear()
        // 差分を適用
        layers.forEach { output.drawPixelImage(0.0, 0.0, it.second) }
    }
}