package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.map.behavior.DrawBehavior
import com.kamesuta.paintcraft.map.behavior.DrawBehaviorTypes
import com.kamesuta.paintcraft.map.draw.Draw
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.util.vec.origin
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import org.bukkit.util.Vector

/**
 * 書き込み可能レンダラー
 * @param behaviorDesc 描画ツール生成情報
 */
class DrawableMapRenderer(private val behaviorDesc: DrawBehaviorTypes.Desc) : MapRenderer(), Drawable {
    /** マップビュー */
    private lateinit var mapView: MapView

    /** マップキャンバス */
    lateinit var mapCanvas: MapCanvas

    /** 描画ツール */
    lateinit var behavior: DrawBehavior

    /** 変更フラグ */
    private var dirty = false

    /**
     * addRenderer() された時に呼ばれるため、必ず使えるはず
     * @param map マップビュー
     */
    override fun initialize(map: MapView) {
        // ビューをいつでも使えるようにする
        mapView = map
        // 描画ツールを生成
        behavior = behaviorDesc.generator(this)
        // キャンバスを強制初期化
        mapCanvas = DrawableMapReflection.createAndPutCanvas(mapView, this)
            ?: return
        // マップをキャンバスに読み込む
        mapCanvas.loadFromMapView()
        // 地図上のプレイヤーカーソルをすべて削除する
        repeat(mapCanvas.cursors.size()) {
            mapCanvas.cursors.removeCursor(mapCanvas.cursors.getCursor(0))
        }
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
        // 描画
        draw.draw(mapCanvas)
        // 変更フラグを設定する
        dirty = true
    }

    /**
     * プレイヤーに更新を通知する
     */
    fun updatePlayer(location: Vector) {
        // プレイヤーカーソルを更新する ( TODO: 半径のコンフィグ化 )
        mapCanvas.updatePlayer(location, 10.0)
    }

    companion object {
        /** プレイヤーに更新を通知する */
        private fun MapCanvas.updatePlayer(location: Vector, radius: Double) {
            // 変更箇所を取得する
            val updates = DrawableMapReflection.getMapDirtyArea(mapView)
                ?: return // 変更箇所がなければ何もしない
            // 新しいバッファーを取得
            val buffer = DrawableMapReflection.getCanvasBuffer(this)
                ?: return
            // 更新があるプレイヤーに通知する
            updates.asSequence().filter { (player, _) ->
                // 近くのプレイヤーのみに通知する
                player.location.origin.distanceSquared(location) <= radius * radius
            }.forEach { (player, updateArea) ->
                // プレイヤーに地図を送信する
                DrawableMapUpdater.sendMap(player, mapView, buffer, updateArea)
            }
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