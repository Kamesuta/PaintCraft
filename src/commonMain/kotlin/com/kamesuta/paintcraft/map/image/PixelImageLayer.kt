package com.kamesuta.paintcraft.map.image

import com.kamesuta.paintcraft.util.DirtyRect

/**
 * キーごとにレイヤーを分離します
 * 複数プレイヤーが同時にキャンバスに描画することをサポートします
 */
class PixelImageLayer<T> {
    /** レイヤーのベース */
    val base = PixelImageMapBuffer()

    /** レイヤーの出力 */
    val output = PixelImageMapBuffer()

    /** レイヤー */
    private val layers = mutableListOf<Pair<T, PixelImageMapBuffer>>()

    /** プレイヤー->レイヤーのマップ */
    private val layerMap = mutableMapOf<T, PixelImageMapBuffer>()

    /**
     * レイヤーを取得または作成して初期化する
     * @return レイヤー
     */
    operator fun get(key: T): PixelImageMapBuffer {
        // レイヤーが存在する場合はそれを使用
        val layer = layerMap[key]
        // レイヤーが存在しない場合は作成
            ?: PixelImageMapBuffer().also {
                // 変更なしで初期化
                it.clearToUnchanged(true)
                // レイヤーを追加
                layerMap.put(key, it)
                    ?: layers.add(key to it)
            }
        // レイヤーを返す
        return layer
    }

    /** 全レイヤーで変更があった場所 */
    private val dirtyArea: DirtyRect
        get() {
            val dirtyRect = DirtyRect()
            layers.forEach { (_, layer) ->
                dirtyRect.flagDirty(layer.dirty)
            }
            return dirtyRect
        }

    /**
     * レイヤーをベースレイヤーに適用する
     * @param key プレイヤー
     */
    fun apply(key: T) {
        // layersとlayerMap両方からレイヤーを削除
        val layer = layerMap.remove(key)
            ?: return
        // 消したレイヤーの変更箇所を記録
        base.dirty.flagDirty(layer.dirty)
        // マップから削除
        layers.removeIf { it.first == key }
        // 変更があった場所だけベースレイヤーに適用
        layer.dirty.rect?.let { base.drawPixelImageCrop(it, layer) }
    }

    /**
     * 変更を破棄して削除する
     * @param key プレイヤー
     */
    fun reset(key: T) {
        // layersとlayerMap両方からレイヤーを削除
        val layer = layerMap.remove(key)
            ?: return
        // 消したレイヤーの変更箇所を記録
        base.dirty.flagDirty(layer.dirty)
        // マップから削除
        layers.removeIf { it.first == key }
    }

    /**
     * 変更を破棄する
     * @param key プレイヤー
     */
    fun clear(key: T) {
        // layersを取得してクリア
        val layer = layerMap[key]
            ?: return
        // 消したレイヤーの変更箇所を記録
        base.dirty.flagDirty(layer.dirty)
        // クリア
        layer.clearToUnchanged()
    }

    /**
     * レイヤーを合成する
     * @param prevDirty 前回変更があった場所
     * @return 合成したレイヤー
     */
    fun compose() {
        // コピーした後に変更フラグをリセット
        output.dirty.clear()
        // 変更点を取得
        val dirtyRect = dirtyArea.apply { flagDirty(base.dirty) }
        val dirty = dirtyRect.rect
            ?: return
        // まずコピーする
        output.drawPixelImageCrop(dirty, base)
        // 差分を適用
        layers.forEach { (_, layer) ->
            // 変更されている部分のみ適用
            output.drawPixelImageCrop(dirty, layer)
        }
        // クリア箇所をクリア
        base.dirty.clear()
    }
}