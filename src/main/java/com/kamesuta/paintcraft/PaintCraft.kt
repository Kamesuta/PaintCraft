package com.kamesuta.paintcraft

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.kamesuta.paintcraft.canvas.CanvasDrawListener
import com.kamesuta.paintcraft.util.DebugLocationVisualizer
import dev.kotx.flylib.flyLib
import org.bukkit.plugin.java.JavaPlugin


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
        protocolManager = ProtocolLibrary.getProtocolManager();

        // デバッグ用の位置表示
        DebugLocationVisualizer.registerTick()

        // キャンバスのイベントリスナーを初期化
        val drawListener = CanvasDrawListener()
        // キャンバスのイベントを登録
        server.pluginManager.registerEvents(drawListener, this)
        // ティックイベントを登録
        server.scheduler.runTaskTimer(this, drawListener, 0, 0)
        // カーソルが動いたときのパケットハンドラーを登録する
        protocolManager.asynchronousManager.registerAsyncHandler(drawListener.createMovePacketAdapter()).start()
        // クリックしたときのパケットハンドラーを登録する
        protocolManager.asynchronousManager.registerAsyncHandler(drawListener.createClickPacketAdapter()).start()
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