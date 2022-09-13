package com.kamesuta.paintcraft

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.kamesuta.paintcraft.frame.FrameDrawListener
import com.kamesuta.paintcraft.frame.FrameReflection
import com.kamesuta.paintcraft.map.DrawableMapReflection
import com.kamesuta.paintcraft.util.clienttype.ClientTypeReflection
import com.kamesuta.paintcraft.util.clienttype.ClientTypeUpdateListener
import com.kamesuta.paintcraft.util.vec.debug.DebugLocationVisualizer
import dev.kotx.flylib.flyLib
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level


class PaintCraft : JavaPlugin() {
    /** ProtocolManagerインスタンス */
    lateinit var protocolManager: ProtocolManager

    init {
        flyLib {
            command(PaintCraftCommand())
        }
    }

    override fun onEnable() {
        // プラグインインスタンスをstaticフィールドに保存
        instance = this
        // ProtocolLibを初期化
        protocolManager = ProtocolLibrary.getProtocolManager()

        // リフレクションクラスのチェック
        runCatching {
            // アイテムフレーム系クラスのチェック
            FrameReflection.checkReflection()
            // Map系クラスのチェック
            DrawableMapReflection.checkReflection()
            // クライアントのブランドクラスのチェック
            ClientTypeReflection.checkReflection()
            // ViaVersionがあればViaAPIを初期化
            if (server.pluginManager.isPluginEnabled("ViaVersion")) {
                ClientTypeReflection.initViaReflection()
            }
        }.onFailure { e ->
            logger.log(
                Level.SEVERE,
                "Failed to find classes, methods or fields in NMS. Disabling plugin.",
                e
            )
            // 読み込みに失敗したためプラグインを無効にする
            pluginLoader.disablePlugin(instance)
            return
        }

        // デバッグ用の位置表示
        DebugLocationVisualizer.registerTick()

        // フレームのイベントリスナーを初期化
        val drawListener = FrameDrawListener()
        // 通常ののイベントを登録
        server.pluginManager.registerEvents(drawListener, this)
        // ティックイベントを登録
        server.scheduler.runTaskTimer(this, drawListener, 0, 0)
        // カーソルが動いたときのパケットハンドラーを登録する
        protocolManager.asynchronousManager.registerAsyncHandler(drawListener.createMovePacketAdapter()).start()
        // クリックしたときのパケットハンドラーを登録する
        protocolManager.asynchronousManager.registerAsyncHandler(drawListener.createClickPacketAdapter()).start()

        // クライアントのブランドを更新するイベントリスナー (BE版判定のため)
        val clientTypeUpdateListener = ClientTypeUpdateListener()
        server.pluginManager.registerEvents(clientTypeUpdateListener, this)
        clientTypeUpdateListener.updateAll()
    }

    override fun onDisable() {
        // パケットハンドラーを登録解除する (プラグインリロード時に大量エラーが出ないように)
        protocolManager.asynchronousManager.unregisterAsyncHandlers(this)
    }

    companion object {
        /** プラグインインスタンス */
        lateinit var instance: PaintCraft
            private set
    }
}