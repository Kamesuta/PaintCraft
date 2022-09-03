package com.kamesuta.paintcraft.util.clienttype

import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
import org.bukkit.entity.Player
import java.lang.reflect.Field
import java.lang.reflect.Method

object ClientBrandReflection {
    /**
     * NMSにアクセスするためのクラス
     * NMSクラスが見つからなかったりした際、クラスの関数がそもそも呼べなくなるのを防ぐ
     */
    private object Accessor {
        // NMSクラス
        val entityPlayer: Class<*> = MinecraftReflection.getMinecraftClass("EntityPlayer")
        val playerConnection: Class<*> = MinecraftReflection.getMinecraftClass("PlayerConnection")
        val craftEntity: Class<*> = MinecraftReflection.getCraftBukkitClass("entity.CraftEntity")

        // NMS関数/フィールド
        val craftEntityGetHandle: Method = craftEntity.getDeclaredMethod("getHandle").apply { isAccessible = true }
        val entityPlayerPlayerConnection: Field =
            entityPlayer.getDeclaredField("playerConnection").apply { isAccessible = true }
        val playerConnectionClientBrandName: Field =
            playerConnection.getDeclaredField("clientBrandName").apply { isAccessible = true }
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
     * プレイヤーのクライアントブランドを取得します
     * @param player プレイヤー
     * @return クライアントブランド
     */
    fun getClientBrand(player: Player): String? {
        return runCatching {
            val handle = Accessor.craftEntityGetHandle(player)
                ?: return@runCatching null
            val connection = Accessor.entityPlayerPlayerConnection[handle]
                ?: return@runCatching null
            val clientBrand = Accessor.playerConnectionClientBrandName[connection] as String?
                ?: return@runCatching null
            clientBrand
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get client brand")
        }.getOrNull()
    }
}