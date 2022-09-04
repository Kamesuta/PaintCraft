package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.map.draw.Draw
import com.kamesuta.paintcraft.map.draw.DrawRollback
import com.kamesuta.paintcraft.map.draw.Drawable
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView

/**
 * 書き込み可能レンダラー
 */
class DrawableMapRenderer : MapRenderer(), Drawable {
    /** 初期化フラグ */
    private var initialized = false

    /** マップビュー */
    private lateinit var mapView: MapView

    /** マップキャンバス */
    private lateinit var mapCanvas: MapCanvas

    /** 変更フラグ */
    private var dirty = false

    /** マップキャンバスを取得する */
    val canvas: MapCanvas?
        get() = if (initialized) mapCanvas else null

    /** 前回の状態 */
    var previewBefore: DrawRollback? = null

    /**
     * addRenderer() された時に呼ばれる
     * @param map マップビュー
     */
    override fun initialize(map: MapView) {
        // ビューをいつでも使えるようにする
        mapView = map
        // キャンバスを強制初期化
        mapCanvas = DrawableMapReflection.createAndPutCanvas(mapView, this)
            ?: return
        // マップをキャンバスに読み込む
        mapCanvas.loadFromMapView()
        // 地図上のプレイヤーカーソルをすべて削除する
        repeat(mapCanvas.cursors.size()) {
            mapCanvas.cursors.removeCursor(mapCanvas.cursors.getCursor(0))
        }
        // 初期化が完了したことを記録する
        initialized = true
    }

    /**
     * レンダリングする
     * Bukkitから一定間隔で呼ばれる
     * 更新があれば書き込みを行う
     * @param map マップビュー
     * @param canvas マップキャンバス
     * @param player プレイヤー
     */
    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        // 初期化チェック
        if (!initialized) {
            return
        }

        // 変更がある場合保存する
        if (dirty) {
            canvas.saveToMapView()
            dirty = false
        }
    }

    /**
     * 書き込みを行う
     */
    override fun g(draw: Draw) {
        // 初期化チェック
        if (!initialized) {
            return
        }

        // 描画
        draw.draw(mapCanvas)
        // 変更フラグを設定する
        dirty = true
    }

    /**
     * プレイヤーに更新を通知する
     * @param player プレイヤー
     */
    fun updatePlayer(player: Player) {
        // 初期化チェック
        if (!initialized) {
            return
        }

        // プレイヤーカーソルを更新する
        mapCanvas.updatePlayer(player)
    }

    companion object {
        /** プレイヤーに更新を通知する */
        private fun MapCanvas.updatePlayer(player: Player) {
            val dirty = DrawableMapReflection.getMapDirtyArea(player, mapView)
                ?: return
            val buffer = DrawableMapReflection.getCanvasBuffer(this)
                ?: return

            // プレイヤーに地図を送信する
            DrawableMapUpdater.sendMap(player, mapView, buffer, dirty)
        }

        /** キャンバスの内容をマップビューに保存し永続化する */
        private fun MapCanvas.saveToMapView() {
            val src = DrawableMapReflection.getCanvasBuffer(this)
            val dst = DrawableMapReflection.getMapBuffer(mapView)
            if (src != null && dst != null) {
                src.copyTo(dst)
            }
        }

        /** 永続化されたマップビューのデータを読み込む */
        private fun MapCanvas.loadFromMapView() {
            val src = DrawableMapReflection.getMapBuffer(mapView)
            val dst = DrawableMapReflection.getCanvasBuffer(this)
            if (src != null && dst != null) {
                src.copyTo(dst)
            }
        }
    }
}