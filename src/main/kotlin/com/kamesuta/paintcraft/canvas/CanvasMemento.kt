package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.draw.DrawRollback
import com.kamesuta.paintcraft.util.vec.origin
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player

/**
 * キャンバスのスナップショット
 * @param entries 変更されたキャンバスのエントリー
 */
data class CanvasMemento(val entries: Collection<Entry>) {
    /**
     * キャンバスのスナップショットの1アイテムフレーム分
     * @param player 描き込んだプレイヤー
     * @param itemFrame アイテムフレーム
     * @param mapItem マップ
     */
    data class Entry(
        val player: Player,
        val itemFrame: ItemFrame,
        val mapItem: DrawableMapItem,
    ) {
        /** スナップショット */
        var data: DrawRollback? = null

        /** スナップショットを反映する */
        fun rollback() {
            val data = data
            if (data != null) {
                // スナップショットがある場合は、スナップショットを反映する
                mapItem.draw(player) {
                    g(data)
                }
            } else {
                // スナップショットがない場合はまさに変更中なため、レイヤーを破棄すれば戻る
                mapItem.renderer.mapLayer.reset(player)
            }
        }

        /** 変更点をプレイヤーに送信する */
        fun updatePlayer() {
            mapItem.renderer.updatePlayer(itemFrame.location.origin)
        }

        /** 変更を適用する */
        fun applyChange() {
            // 変更点を取得 (または空変更を作成)
            val layer = mapItem.renderer.mapLayer[player]
            // 変更点をスナップショットに保存
            data = DrawRollback(mapItem.renderer.mapLayer.base, layer)
            // スナップショットを永続化させる
            mapItem.renderer.mapLayer.apply(player)
        }
    }

    /** スナップショットを反映する */
    fun rollback() {
        entries.forEach(Entry::rollback)
    }

    /** 変更点をプレイヤーに送信する */
    fun updatePlayer() {
        entries.forEach(Entry::updatePlayer)
    }

    /** 変更を適用する */
    fun applyChange() {
        entries.forEach(Entry::applyChange)
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
         * @param player 描き込んだプレイヤー
         * @param itemFrame アイテムフレーム
         * @param mapItem マップ
         */
        fun store(player: Player, itemFrame: ItemFrame, mapItem: DrawableMapItem) {
            entries.getOrPut(mapItem.mapView.id) {
                // 新たに描いたマップアイテムのみ記憶
                Entry(player, itemFrame, mapItem)
            }
        }

        /** スナップショットを作成する */
        fun build() = CanvasMemento(entries.values.toList())

        /** 描いた内容の記憶をクリアする */
        fun clear() = entries.clear()
    }
}
