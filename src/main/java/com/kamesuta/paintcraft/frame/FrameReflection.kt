package com.kamesuta.paintcraft.frame

import com.comphenix.protocol.utility.MinecraftReflection
import com.kamesuta.paintcraft.PaintCraft
import org.bukkit.entity.Entity
import java.lang.reflect.Method

/**
 * キャンバスリフレクションクラス
 */
object FrameReflection {
    /**
     * NMSにアクセスするためのクラス
     * NMSクラスが見つからなかったりした際、DrawableMapReflectionクラスの関数がそもそも呼べなくなるのを防ぐ
     */
    private object Accessor {
        // NMSクラス
        val entity: Class<*> = MinecraftReflection.getMinecraftClass("Entity")
        val craftEntity: Class<*> = MinecraftReflection.getCraftBukkitClass("entity.CraftEntity")

        // NMS関数/フィールド
        val craftEntityGetHandle: Method = craftEntity.getDeclaredMethod("getHandle").apply { isAccessible = true }
        val entityGetYOffset: Method = entity.getDeclaredMethod("bb").apply { isAccessible = true }
        val entityGetMountedYOffset: Method = entity.getDeclaredMethod("bc").apply { isAccessible = true }
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
     * エンティティが乗り物に乗ったときのY方向のオフセットを取得します
     * @param entity 乗ったエンティティ
     * @return Y方向のオフセット
     */
    fun getYOffset(entity: Entity): Double {
        return entity.runCatching {
            val handle = Accessor.craftEntityGetHandle(entity)
                ?: return@runCatching null
            val yOffset = Accessor.entityGetYOffset(handle) as Double
            yOffset
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get yOffset")
        }.getOrNull() ?: 0.0
    }

    /**
     * エンティティが乗られたときのY方向のオフセットを取得します
     * @param entity 乗られたエンティティ
     * @return Y方向のオフセット
     */
    fun getMountedYOffset(entity: Entity): Double {
        return entity.runCatching {
            val handle = Accessor.craftEntityGetHandle(entity)
                ?: return@runCatching null
            val mountedYOffset = Accessor.entityGetMountedYOffset(handle) as Double
            mountedYOffset
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get mounted yOffset")
        }.getOrNull() ?: (entity.height * 0.75)
    }
}
