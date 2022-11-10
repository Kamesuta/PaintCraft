package com.kamesuta.paintcraft.map

import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoop
import io.netty.channel.socket.nio.NioSocketChannel
import org.bukkit.entity.Player
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * パケット系リフレクションクラス
 */
object DrawableMapUpdaterReflection {
    /**
     * NMSにアクセスするためのクラス
     * NMSクラスが見つからなかったりした際、クラスの関数がそもそも呼べなくなるのを防ぐ
     */
    private object Accessor {
        // NMSクラス
        val entityPlayer: Class<*> = MinecraftReflection.getMinecraftClass("EntityPlayer")
        val playerConnection: Class<*> = MinecraftReflection.getMinecraftClass("PlayerConnection")
        val networkManager: Class<*> = MinecraftReflection.getMinecraftClass("NetworkManager")
        val craftEntity: Class<*> = MinecraftReflection.getCraftBukkitClass("entity.CraftEntity")

        // NMS関数/フィールド
        val craftEntityGetHandle: Method = craftEntity.getDeclaredMethod("getHandle").apply { isAccessible = true }
        val entityPlayerPlayerConnection: Field =
            entityPlayer.getDeclaredField("playerConnection").apply { isAccessible = true }
        val playerConnectionNetworkManager: Field =
            playerConnection.getDeclaredField("networkManager").apply { isAccessible = true }
        val networkManagerChannel: Field = networkManager.getDeclaredField("channel").apply { isAccessible = true }
    }

    /**
     * NMSクラスが存在するかチェックします
     * 存在しない場合は例外を投げます
     */
    @Throws(ReflectiveOperationException::class)
    fun checkReflection() {
        try {
            // NMSクラスが見つからなかったらエラー
            Accessor.javaClass
        } catch (e: ExceptionInInitializerError) {
            // 中身を返す
            throw e.cause ?: e
        }
    }

    /**
     * プレイヤーのNettyイベントループを取得します
     * @return イベントループ
     */
    fun getEventLoop(player: Player): NioEventLoop? {
        return runCatching {
            val handle = Accessor.craftEntityGetHandle(player)
                ?: return@runCatching null
            val connection = Accessor.entityPlayerPlayerConnection[handle]
                ?: return@runCatching null
            val networkManager = Accessor.playerConnectionNetworkManager[connection]
                ?: return@runCatching null
            val channel = Accessor.networkManagerChannel[networkManager] as? Channel
                ?: return@runCatching null
            return channel.parent().eventLoop() as? NioEventLoop
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get player event loop")
        }.getOrNull()
    }
}
