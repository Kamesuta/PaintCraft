package com.kamesuta.paintcraft.util

import com.kamesuta.paintcraft.PaintCraft
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * 永続化データを扱うユーティリティクラス
 * @param keyName キー名
 * @param type データタイプ
 */
class PersistentDataProperty<T>(keyName: String, private val type: PersistentDataType<T, T>) {
    /** キー */
    private val key = NamespacedKey(PaintCraft.instance, keyName)

    /**
     * データを取得する
     * @param container データコンテナ
     * @return データ
     */
    operator fun get(container: PersistentDataContainer): T? {
        return container.get(key, type)
    }

    /**
     * データを設定する
     * @param container データコンテナ
     * @param value データ
     */
    operator fun set(container: PersistentDataContainer, value: T?) {
        if (value != null) {
            container.set(key, type, value)
        } else {
            container.remove(key)
        }
    }

    companion object {
        /** バイト型 */
        fun byte(keyName: String) = PersistentDataProperty(keyName, PersistentDataType.BYTE)

        /** ショート型 */
        fun short(keyName: String) = PersistentDataProperty(keyName, PersistentDataType.SHORT)

        /** 整数型 */
        fun integer(keyName: String) = PersistentDataProperty(keyName, PersistentDataType.INTEGER)

        /** ロング型 */
        fun long(keyName: String) = PersistentDataProperty(keyName, PersistentDataType.LONG)

        /** 浮動小数点型 */
        fun float(keyName: String) = PersistentDataProperty(keyName, PersistentDataType.FLOAT)

        /** 倍精度浮動小数点型 */
        fun double(keyName: String) = PersistentDataProperty(keyName, PersistentDataType.DOUBLE)

        /** 文字列型 */
        fun string(keyName: String) = PersistentDataProperty(keyName, PersistentDataType.STRING)

        /** バイト配列型 */
        fun byteArray(keyName: String) = PersistentDataProperty(keyName, PersistentDataType.BYTE_ARRAY)

        /** 整数配列型 */
        fun integerArray(keyName: String) = PersistentDataProperty(keyName, PersistentDataType.INTEGER_ARRAY)

        /** ロング配列型 */
        fun longArray(keyName: String) = PersistentDataProperty(keyName, PersistentDataType.LONG_ARRAY)
    }
}