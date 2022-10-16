package com.kamesuta.paintcraft.util

import com.kamesuta.paintcraft.util.vec.Rect2i
import com.kamesuta.paintcraft.util.vec.Vec2i
import kotlin.math.max
import kotlin.math.min

/**
 * 更新領域を管理するクラス
 */
class DirtyRect {
    var isDirty = false
    var minX = 0
    var minY = 0
    var maxX = 0
    var maxY = 0

    /**
     * 更新領域を追加する
     * @param x x座標
     * @param y y座標
     */
    fun flagDirty(x: Int, y: Int) {
        if (isDirty) {
            minX = min(minX, x)
            minY = min(minY, y)
            maxX = max(maxX, x)
            maxY = max(maxY, y)
        } else {
            isDirty = true
            minX = x
            minY = y
            maxX = x
            maxY = y
        }
    }

    /**
     * 更新領域を追加する
     * @param rect 更新領域
     */
    fun flagDirty(rect: DirtyRect) {
        if (rect.isDirty) {
            flagDirty(rect.minX, rect.minY)
            flagDirty(rect.maxX, rect.maxY)
        }
    }

    /** 更新領域をリセットする */
    fun clear() {
        isDirty = false
    }

    /** 更新領域 */
    val rect: Rect2i?
        get() {
            // 1ピクセル以上変更されている場合のみ更新領域を返す
            if (!isDirty) return null
            // 更新領域を構築
            return Rect2i(Vec2i(minX, minY), Vec2i(maxX, maxY))
        }
}
