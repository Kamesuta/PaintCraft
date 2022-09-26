package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.map.behavior.DrawBehavior
import com.kamesuta.paintcraft.map.behavior.DrawBehaviorTypes
import com.kamesuta.paintcraft.map.draw.Draw
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.map.image.PixelImageLayer
import com.kamesuta.paintcraft.map.image.PixelImageMapBuffer
import com.kamesuta.paintcraft.map.image.PixelImageMapCanvas
import com.kamesuta.paintcraft.map.image.drawPixelImage
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
class DrawableMapRenderer(private val behaviorDesc: DrawBehaviorTypes.Desc) : MapRenderer() {
    /** マップビュー */
    private lateinit var mapView: MapView

    /** 永続化されるピクセルデータ */
    private var mapViewBuffer: PixelImageMapBuffer? = null

    /** 永続化されるピクセルデータの更新領域 */
    private val mapViewBufferDirty = DirtyRect()

    /** マップピクセルデータ */
    private val composedCache = PixelImageMapBuffer()

    /** レイヤー書き出し先 */
    private val mapLayerCache = PixelImageMapBuffer()

    /** 書き込み中のピクセルデータのレイヤー */
    val mapLayer = PixelImageLayer<Player>(PixelImageMapBuffer())

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
        mapViewBuffer?.copyTo(mapLayer.base)
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
                mapLayer.base.copyTo(it)
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
                composedCache.copyTo(it)
            }
        }
    }

    /**
     * ベースイメージへ書き込みを行う
     * @param draw 描画関数
     */
    fun drawBase(draw: Draw) {
        // 描画
        draw.draw(mapLayer.base)
        // 変更フラグを設定する
        dirty = true
    }

    /**
     * 書き込みを行うオブジェクトを取得する
     * @param player 描き込んだプレイヤー
     * @return 書き込みを行うオブジェクト
     */
    fun drawer(player: Player): Drawable {
        return Drawable { draw ->
            // 描画
            draw.draw(mapLayer[player])
            // 変更フラグを設定する
            dirty = true
        }
    }

    /**
     * プレイヤーに更新を通知する
     * @param location アイテムフレームの位置
     */
    fun updatePlayer(location: Vector) {
        // 更新する半径 ( TODO: 半径のコンフィグ化 )
        val radius = 10.0
        // レイヤーを更新する
        composeLayer()
        // 変更箇所を取得する
        val updateArea = composedCache.dirty.rect
            ?: return // 変更箇所がなければ何もしない
        // 更新があるプレイヤーに通知する
        val players = DrawableMapReflection.getMapTrackingPlayers(mapView)
            ?: return
        // 更新領域のみのピクセルデータを作成する
        val part = composedCache.createSubImage(updateArea)
        for (player in players) {
            // 近くのプレイヤーのみに通知する
            if (player.location.toVector().distanceSquared(location) < radius * radius) {
                // プレイヤーに地図を送信する
                DrawableMapUpdater.sendMap(player, mapView, part, updateArea)
            }
        }
        // 永続化用の更新領域に追加する
        mapViewBufferDirty.flagDirty(composedCache.dirty)
    }

    /** ピクセルデータの内容をマップビューに保存し永続化する */
    private fun saveToMapView() {
        // 出力先のピクセルデータを取得する
        val dst = mapViewBuffer ?: return
        // レイヤーを更新する
        composeLayer()
        // 永続化されるバッファーにコピーする
        composedCache.copyTo(dst)
        // 変更箇所を取得する
        val updateArea = mapViewBufferDirty.rect
            ?: return
        // マップビューの更新範囲を更新する
        DrawableMapReflection.flagDirty(mapView, updateArea.p1.x, updateArea.p1.y)
        DrawableMapReflection.flagDirty(mapView, updateArea.p2.x, updateArea.p2.y)
        // 永続化用の更新領域をクリアする
        mapViewBufferDirty.clear()
    }

    /** レイヤーを合成する */
    private fun composeLayer() {
        // レイヤーを合成する
        mapLayer.compose(mapLayerCache)
        // 変更箇所をクリアする
        composedCache.dirty.clear()
        // レイヤーを更新する
        composedCache.drawPixelImage(0.0, 0.0, mapLayerCache)
    }
}