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

        // キャンバスのイベントを登録
        val drawListener = CanvasDrawListener()
        server.pluginManager.registerEvents(drawListener, this)

        // カーソルが動いたときのパケットハンドラーを登録する
        protocolManager.asynchronousManager.registerAsyncHandler(drawListener.createMovePacketAdapter()).start()
        // クリックしたときのパケットハンドラーを登録する
        protocolManager.asynchronousManager.registerAsyncHandler(drawListener.createClickPacketAdapter()).start()
    }

    companion object {
        /** プラグインインスタンス */
        lateinit var instance: PaintCraft
            private set
    }
}