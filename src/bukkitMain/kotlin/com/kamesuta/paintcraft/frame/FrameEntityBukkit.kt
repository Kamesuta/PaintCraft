package com.kamesuta.paintcraft.frame

import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.DrawableMapItemBukkit
import com.kamesuta.paintcraft.util.clienttype.ClientType
import com.kamesuta.paintcraft.util.vec.*
import org.bukkit.Rotation
import org.bukkit.entity.ItemFrame

data class FrameEntityBukkit(val itemFrame: ItemFrame) : FrameEntity {
    override val location: Line3d get() = itemFrame.location.toLine()

    override val blockLocation: Line3d
        get() = Line3d(itemFrame.location.toCenterLocation().origin, itemFrame.facing.direction.toVec3d())

    override fun getFrameRotation(clientType: ClientType): FrameRotation {
        return when (clientType.isLegacyRotation) {
            false -> itemFrame.rotation.toFrameRotation()
            true -> itemFrame.rotation.toFrameRotationLegacy()
        }
    }

    override fun toFrameLocation(clientType: ClientType): FrameLocation {
        // キャンバスの回転を計算
        val (canvasYaw, canvasPitch) = getCanvasRotation(clientType)
        // ブロックの中心座標
        val centerLocation = itemFrame.location.toCenterLocation().origin
        // アイテムフレームが透明かどうか
        val isFrameVisible = itemFrame.isVisible || !clientType.isInvisibleFrameSupported
        // キャンバス平面とアイテムフレームの差 = アイテムフレームの厚さ/2
        val canvasOffsetZ = if (isFrameVisible) 0.07 else 0.0075
        // アイテムフレームを構築
        return FrameLocation(centerLocation, canvasYaw, canvasPitch, canvasOffsetZ)
    }

    override fun getCanvasRotation(clientType: ClientType): Pair<Float, Float> {
        return if (clientType.isPitchRotationSupported) {
            // Java版1.13以降はYaw/Pitchの自由回転をサポートしている
            itemFrame.location.let { it.yaw to it.pitch }
        } else if (clientType.isFacingRotationOnly) {
            // BE版はブロックに沿った回転のみサポートしている
            val dir = Line3d(Vec3d.Zero, itemFrame.facing.direction.toVec3d())
            dir.yaw to dir.pitch
        } else {
            // Java版1.12以前はYaw回転のみサポートしている、Pitchは常に0
            itemFrame.location.yaw to 0.0f
        }
    }

    override fun toDrawableMapItem(): DrawableMapItem? =
        // TODO: 額縁内のアイテムが更新されたときに更新する
        cacheFrameItem.getOrPut(itemFrame) { DrawableMapItemBukkit.get(itemFrame.item) }

    companion object {
        /** アイテムフレームのキャッシュ */
        val cacheFrameItem = mutableMapOf<ItemFrame, DrawableMapItemBukkit?>()

        /**
         * BukkitのRotationから対応するFrameRotationを取得
         * @receiver BukkitのRotation
         * @return 対応するFrameRotation
         */
        private fun Rotation.toFrameRotation(): FrameRotation {
            return when (this) {
                Rotation.NONE -> FrameRotation.NONE
                Rotation.CLOCKWISE_45 -> FrameRotation.CLOCKWISE_45
                Rotation.CLOCKWISE -> FrameRotation.CLOCKWISE
                Rotation.CLOCKWISE_135 -> FrameRotation.CLOCKWISE_135
                Rotation.FLIPPED -> FrameRotation.FLIPPED
                Rotation.FLIPPED_45 -> FrameRotation.FLIPPED_45
                Rotation.COUNTER_CLOCKWISE -> FrameRotation.COUNTER_CLOCKWISE
                Rotation.COUNTER_CLOCKWISE_45 -> FrameRotation.COUNTER_CLOCKWISE_45
                else -> FrameRotation.NONE
            }
        }

        /**
         * BukkitのRotationから対応するFrameRotationを取得
         * 1.7.10以下のバージョン用、4方向しかない
         * @receiver BukkitのRotation
         * @return 対応するFrameRotation
         */
        private fun Rotation.toFrameRotationLegacy(): FrameRotation {
            return when (this) {
                Rotation.NONE -> FrameRotation.NONE
                Rotation.CLOCKWISE_45 -> FrameRotation.NONE
                Rotation.CLOCKWISE -> FrameRotation.CLOCKWISE_45
                Rotation.CLOCKWISE_135 -> FrameRotation.CLOCKWISE_45
                Rotation.FLIPPED -> FrameRotation.CLOCKWISE
                Rotation.FLIPPED_45 -> FrameRotation.CLOCKWISE
                Rotation.COUNTER_CLOCKWISE -> FrameRotation.CLOCKWISE_135
                Rotation.COUNTER_CLOCKWISE_45 -> FrameRotation.CLOCKWISE_135
                else -> FrameRotation.NONE
            }
        }
    }
}
