package com.kamesuta.paintcraft.map

import com.kamesuta.paintcraft.map.behavior.DrawBehavior
import com.kamesuta.paintcraft.map.behavior.DrawBehaviorTypes
import com.kamesuta.paintcraft.map.draw.Draw
import com.kamesuta.paintcraft.map.draw.Drawable
import com.kamesuta.paintcraft.map.image.PixelImageCacheBuffer
import com.kamesuta.paintcraft.map.image.PixelImageCacheBuffer.Companion.applyImage
import com.kamesuta.paintcraft.map.image.PixelImageLayer
import com.kamesuta.paintcraft.map.image.PixelImageMapBuffer
import com.kamesuta.paintcraft.map.image.PixelImageMapCanvas
import com.kamesuta.paintcraft.player.PaintPlayer
import com.kamesuta.paintcraft.player.PaintPlayerBukkit
import com.kamesuta.paintcraft.util.DirtyRect
import com.kamesuta.paintcraft.util.vec.Vec3d
import com.kamesuta.paintcraft.util.vec.origin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView

/**
 * 書き込み可能レンダラー
 * @param behaviorDesc 描画ツール生成情報
 */
class DrawableMapRendererBukkit(private val behaviorDesc: DrawBehaviorTypes.Desc) : MapRenderer(), DrawableMapRenderer {
    /** マップビュー */
    private lateinit var mapView: MapView

    /** 永続化されるピクセルデータ */
    private var mapViewBuffer: PixelImageMapBuffer? = null

    override val mapLayer = PixelImageLayer<PaintPlayer>()

    /** 描画ツール */
    override lateinit var behavior: DrawBehavior
        private set

    /** キャンバス初回初期化フラグ */
    private var canvasInitialized = false

    /** キャンバスのピクセルイメージのキャッシュ */
    private var canvasBuffer: PixelImageMapCanvas? = null

    /** 変更フラグ */
    private var dirty = true

    /** マップの更新通知 */
    private val canvasUpdater = DrawableMapUpdater()

    /** マップピクセルデータ (保存用) */
    private val saveCache = PixelImageCacheBuffer()

    /** マップピクセルデータ (描いた人更新用) */
    private val updateDrawerCache = PixelImageCacheBuffer()

    /** マップピクセルデータ (周りの人更新用) */
    private val updateAroundCache = PixelImageCacheBuffer()

    /** マップの更新通知をしたチック */
    private var lastUpdateTick = 0

    /** マップの更新通知をしたプレイヤー */
    private var lastUpdatePlayer: PaintPlayer? = null

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
        // 変更がある場合保存する
        if (dirty) {
            // データを永続化されたマップビューのデータにコピーする
            saveToMapView()
        }

        // 初回はキャンバスをラップする
        if (!canvasInitialized) {
            canvasBuffer = PixelImageMapCanvas.wrap(canvas)
        }

        // 初回または変更がある場合は永続化されたマップビューのデータをキャンバスに書き込む
        if (dirty || !canvasInitialized) {
            // データをキャンバスにコピーする
            canvasBuffer?.let {
                // 永続化バッファー
                val buffer = mapViewBuffer ?: return@let
                // 永続化バッファー→キャンバスにコピー
                buffer.copyTo(it)
            }
        }

        // キャンバス初回初期化
        canvasInitialized = true
        // 変更フラグをリセットする
        dirty = false
    }

    override fun drawBase(draw: Draw) {
        // 描画
        draw.draw(mapLayer.base)
        // 変更フラグを設定する
        dirty = true
    }

    override fun drawer(player: PaintPlayer): Drawable {
        return Drawable { draw ->
            // 描画
            draw.draw(mapLayer[player])
            // 変更フラグを設定する
            dirty = true
        }
    }

    override fun updatePlayer(player: PaintPlayer, location: Vec3d) {
        // プレイヤーがBukkitのプレイヤーかチェック
        if (player !is PaintPlayerBukkit) return

        // 最後に更新したプレイヤーなら
        if (lastUpdatePlayer == player) {
            // レイヤーを更新する
            composeLayer(updateDrawerCache)
        }
        // 変更箇所がなければ何もしない
        if (updateDrawerCache.dirty.rect != null) {
            // 更新領域のみのピクセルデータを作成する
            canvasUpdater.createPacket(mapView, updateDrawerCache)
            // 描いたプレイヤーに通知する
            canvasUpdater.sendMap(player.player)
        }

        // 1チックに1回以下のみ更新する
        val shouldUpdateAround =
            Bukkit.getCurrentTick().let { tick -> tick > lastUpdateTick.also { lastUpdateTick = tick } }
        if (!shouldUpdateAround) return

        // 最後に更新したプレイヤーを更新
        lastUpdatePlayer = player

        // 更新する半径 ( TODO: 半径のコンフィグ化 )
        val radius = 10.0
        // 更新があるプレイヤーに通知する
        val trackingPlayers = (DrawableMapReflection.getMapTrackingPlayers(mapView) ?: return)
            .asSequence()
            // 描いたプレイヤーは通知済みなので除外する
            .filter { it != player.player }
            // 近くのプレイヤーのみに通知する
            .filter { it.location.origin.distanceSquared(location) < radius * radius }
            .toList()
        // 周りにプレイヤーがいない場合は通知しない
        if (trackingPlayers.isEmpty()) return

        // レイヤーを更新する
        composeLayer(updateAroundCache)
        // 変更箇所がなければ何もしない
        if (updateAroundCache.dirty.rect != null) {
            // 更新領域のみのピクセルデータを作成する
            canvasUpdater.createPacket(mapView, updateAroundCache)
            // 周りのプレイヤーに地図を送信する
            trackingPlayers.forEach { canvasUpdater.sendMap(it) }
        }
    }

    /** ピクセルデータの内容をマップビューに保存し永続化する */
    private fun saveToMapView() {
        // 永続化バッファー
        val buffer = mapViewBuffer ?: return
        // レイヤーを更新する→永続化されるバッファーにコピーする
        composeLayer(saveCache)
        // 変更箇所を取得する
        val updateArea = saveCache.dirty.rect
            ?: return
        // 永続化バッファーに反映する
        buffer.applyImage(saveCache)
        // マップビューの更新範囲を更新する
        DrawableMapReflection.flagDirty(mapView, updateArea.min.x, updateArea.min.y)
        DrawableMapReflection.flagDirty(mapView, updateArea.max.x, updateArea.max.y)
    }

    /** レイヤーを合成する */
    private fun composeLayer(cache: PixelImageCacheBuffer) {
        // レイヤーを合成する
        mapLayer.compose()
        // 前回の変更箇所と今回の変更箇所をマージする
        val dirtyArea = DirtyRect().apply {
            flagDirty(cache.dirty)
            flagDirty(mapLayer.output.dirty)
        }
        // 変更箇所のみレイヤーを更新する
        val dirty = dirtyArea.rect
            ?: return
        cache.subImage(mapLayer.output, dirty)
    }
}