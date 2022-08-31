package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.PaintCraft
import com.kamesuta.paintcraft.util.ReflectionAccessor
import org.bukkit.entity.Entity

/**
 * キャンバスリフレクションクラス
 */
object FrameReflection {
    /**
     * エンティティが乗り物に乗ったときのY方向のオフセットを取得します
     * @param entity 乗ったエンティティ
     * @return Y方向のオフセット
     */
    fun getYOffset(entity: Entity): Double {
        return entity.runCatching {
            val handle = ReflectionAccessor.invokeMethod(entity, "getHandle")
                ?: return@runCatching null
            val yOffset = ReflectionAccessor.invokeMethod(handle, "bb") as Double
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
            val handle = ReflectionAccessor.invokeMethod(entity, "getHandle")
                ?: return@runCatching null
            val mountedYOffset = ReflectionAccessor.invokeMethod(handle, "bc") as Double
            mountedYOffset
        }.onFailure {
            PaintCraft.instance.logger.warning("Failed to get mounted yOffset")
        }.getOrNull() ?: (entity.height * 0.75)
    }
}
