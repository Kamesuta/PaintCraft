package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.draw.DrawRollback
import com.kamesuta.paintcraft.util.vec.origin
import org.bukkit.entity.ItemFrame

/**
 * キャンバスのスナップショット
 * @param entries 変更されたキャンバスのエントリー
 */
data class CanvasMemento(val entries: Collection<Entry>) {
    /**
     * キャンバスのスナップショットの1アイテムフレーム分
     * @param itemFrame アイテムフレーム
     * @param mapItem マップ
     * @param data スナップショット
     */
    data class Entry(val itemFrame: ItemFrame, val mapItem: DrawableMapItem, val data: DrawRollback) {
        /**
         * スナップショットを反映する
         */
        fun rollback() {
            mapItem.draw {
                g(data)
            }
        }

        /**
         * 変更点をプレイヤーに送信する
         */
        fun updatePlayer() {
            mapItem.renderer.updatePlayer(itemFrame.location.origin)
        }
    }

    /**
     * スナップショットを反映する
     */
    fun rollback() {
        entries.forEach(Entry::rollback)
    }

    /**
     * 変更点をプレイヤーに送信する
     */
    fun updatePlayer() {
        entries.forEach(Entry::updatePlayer)
    }

    /** スナップショットを記憶するツール */
    class Builder {
        /** スナップショットの記録 (mapId: (編集した箇所のアイテムフレーム, マップ)) */
        private val entries = mutableMapOf<Int, Entry>()

        /** 変更があるかどうか */
        val isDirty get() = entries.isNotEmpty()

        /**
         * 現在編集中のマップ (Builderに連動する)
         */
        val editing = CanvasMemento(entries.values)

        /**
         * 描く前の内容を保存する
         * @param itemFrame アイテムフレーム
         * @param mapItem マップ
         */
        fun store(itemFrame: ItemFrame, mapItem: DrawableMapItem) {
            entries.getOrPut(mapItem.mapView.id) {
                // 新たに描いたマップアイテムのみ記憶
                Entry(itemFrame, mapItem, DrawRollback(mapItem.renderer.mapImage))
            }
        }

        /** スナップショットを作成する */
        fun build() = CanvasMemento(entries.values.toList())

        /** 描いた内容の記憶をクリアする */
        fun clear() = entries.clear()
    }
}
