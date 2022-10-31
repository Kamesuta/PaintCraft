package com.kamesuta.paintcraft.util.clienttype

import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
import org.bukkit.entity.Player
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*

object ClientTypeReflection {
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
        val playerConnectionGetClientBrandName: Method =
            playerConnection.getDeclaredMethod("getClientBrandName").apply { isAccessible = true }
    }

    /**
     * ViaVersionにアクセスするためのクラス
     */
    private class ViaAccessor {
        // ViaVersionクラス
        val via: Class<*> = Class.forName("com.viaversion.viaversion.api.Via")
        val viaApi: Class<*> = Class.forName("com.viaversion.viaversion.api.ViaAPI")

        // ViaVersion関数/フィールド
        val viaGetApi: Method = via.getDeclaredMethod("getAPI").apply { isAccessible = true }
        val viaApiGetPlayerVersion: Method =
            viaApi.getDeclaredMethod("getPlayerVersion", UUID::class.java).apply { isAccessible = true }
    }

    /** ViaVersionのアクセサー */
    private var viaAccessor: ViaAccessor? = null

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
     * ViaVersionクラスが存在するかチェックし初期化します
     * 存在しない場合は例外を投げます
     */
    @Throws(ReflectiveOperationException::class)
    fun initViaReflection() {
        // ViaVersionクラスが見つからなかったらエラー
        viaAccessor = ViaAccessor()
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
            val clientBrand = Accessor.playerConnectionGetClientBrandName(connection) as String?
                ?: return@runCatching null
            clientBrand
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get client brand")
        }.getOrNull()
    }

    /**
     * プレイヤーのバージョンを取得します
     * ViaVersionが存在しない場合はnullを返します
     * @param player プレイヤー
     * @return バージョン
     */
    fun getClientVersion(player: Player): Int? {
        // ViaVersionが存在しない場合はなし
        val viaAccessor = viaAccessor ?: return null
        return runCatching {
            val api = viaAccessor.viaGetApi(null)
                ?: return@runCatching null
            val version = viaAccessor.viaApiGetPlayerVersion(api, player.uniqueId) as Int
            version
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get client version (ViaVersion)")
        }.getOrNull()
    }
}