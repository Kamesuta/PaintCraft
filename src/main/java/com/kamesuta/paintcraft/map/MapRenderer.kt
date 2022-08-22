package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.map.draw.Draw
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.util.concurrent.ConcurrentLinkedQueue

class MapRenderer : MapRenderer() {
    /** 初期化フラグ */
    private var initialized = false
    /** マップビュー */
    private lateinit var mapView: MapView
    /** マップキャンバス */
    private lateinit var mapCanvas: MapCanvas
    /** 変更フラグ */
    private var dirty = false

    override fun render(map: MapView, canvas: MapCanvas, player: Player) {
        // マップが初期化されていない場合は初期化する
        if (!initialized) {
            // マップをキャンバスに読み込む
            canvas.loadFromMapView()
            // 地図上のプレイヤーカーソルをすべて削除する
            repeat(canvas.cursors.size()) {
                canvas.cursors.removeCursor(canvas.cursors.getCursor(0))
            }
            // キャンバスとビューをいつでも使えるようにする
            mapView = map
            mapCanvas = canvas
            // 初期化が完了したことを記録する
            initialized = true
        }

        // 変更がある場合保存する
        if (dirty) {
            canvas.saveToMapView()
            dirty = false
        }
    }

    fun draw(draw: Draw) {
        // 初期化チェック
        if (!initialized) {
            return
        }

        // 描画
        draw.draw(mapCanvas)
        // 変更フラグを設定する
        dirty = true
    }
}