package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.map.behavior.DrawBehavior
import com.kamesuta.paintcraft.map.behavior.DrawBehaviorTypes
import com.kamesuta.paintcraft.map.draw.Draw
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.map.image.PixelImageMapBuffer
import com.kamesuta.paintcraft.map.image.PixelImageMapCanvas
import com.kamesuta.paintcraft.util.DirtyRect
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

    /** 永続化されるピクセルデータ */
    private var mapViewBuffer: PixelImageMapBuffer? = null

    /** 永続化されるピクセルデータの更新領域 */
    private val mapViewBufferDirty = DirtyRect()

    /** マップピクセルデータ */
    val mapImage: PixelImageMapBuffer = PixelImageMapBuffer()

    /** 描画ツール */
    lateinit var behavior: DrawBehavior
        private set

    /** キャンバス初回初期化フラグ */
    private var canvasInitialized = false

    /** 変更フラグ */
    private var dirty = true

    /**
     * addRenderer() された時に呼ばれるため、必ず使えるはず
     * @param map マップビュー
     */
    override fun initialize(map: MapView) {
        // ビューをいつでも使えるようにする
        mapView = map
        // 描画ツールを生成
        behavior = behaviorDesc.generator(this)
        // 永続化されたマップビューのデータを読み込む
        mapViewBuffer = DrawableMapReflection.getMapBuffer(mapView)
            ?.let { PixelImageMapBuffer(it) }
        // マップビューのデータがあればコピーする (そのまま使うとスレッドセーフでないため)
        mapViewBuffer?.copyTo(mapImage)
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
        // 最初は永続化されたマップビューのデータをキャンバスに書き込む
        if (!canvasInitialized) {
            // キャンバス初回初期化
            canvasInitialized = true
            // データをキャンバスにコピーする
            PixelImageMapCanvas.wrap(canvas)?.let {
                mapImage.copyTo(it)
            }
        }

        // 変更がある場合保存する
        if (dirty) {
            // 変更フラグをリセットする
            dirty = false
            // データを永続化されたマップビューのデータにコピーする
            saveToMapView()
            // データをキャンバスにコピーする
            PixelImageMapCanvas.wrap(canvas)?.let {
                mapImage.copyTo(it)
            }
        }
    }

    /**
     * 書き込みを行う
     */
    override fun g(draw: Draw) {
        // 描画
        draw.draw(mapImage)
        // 変更フラグを設定する
        dirty = true
    }

    /**
     * プレイヤーに更新を通知する
     */
    fun updatePlayer(location: Vector) {
        // 更新する半径 ( TODO: 半径のコンフィグ化 )
        val radius = 10.0
        // 変更箇所を取得する
        val updateArea = mapImage.dirty.rect
            ?: return // 変更箇所がなければ何もしない
        // 更新があるプレイヤーに通知する
        val players = DrawableMapReflection.getMapTrackingPlayers(mapView)
            ?: return
        // 更新領域のみのピクセルデータを作成する
        val part = mapImage.createSubImage(updateArea)
        for (player in players) {
            // 近くのプレイヤーのみに通知する
            if (player.location.toVector().distanceSquared(location) < radius * radius) {
                // プレイヤーに地図を送信する
                DrawableMapUpdater.sendMap(player, mapView, part, updateArea)
            }
        }
        // 永続化用の更新領域に追加する
        mapViewBufferDirty.flagDirty(mapImage.dirty)
        // 変更箇所をクリアする
        mapImage.dirty.clear()
    }

    /** ピクセルデータの内容をマップビューに保存し永続化する */
    private fun saveToMapView() {
        val dst = mapViewBuffer ?: return
        // 永続化されるバッファーにコピーする
        mapImage.copyTo(dst)

        // 変更箇所を取得する
        val updateArea = mapViewBufferDirty.rect
            ?: return
        // マップビューの更新範囲を更新する
        DrawableMapReflection.flagDirty(mapView, updateArea.p1.x, updateArea.p1.y)
        DrawableMapReflection.flagDirty(mapView, updateArea.p2.x, updateArea.p2.y)
        // 永続化用の更新領域をクリアする
        mapViewBufferDirty.clear()
    }
}