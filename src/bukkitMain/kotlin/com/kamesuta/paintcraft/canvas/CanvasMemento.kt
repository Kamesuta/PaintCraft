package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.frame.FrameEntity
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.draw.DrawRollback
import com.kamesuta.paintcraft.player.PaintPlayer

/**
 * キャンバスのスナップショット
 * @param entries 変更されたキャンバスのエントリー
 */
class CanvasMemento private constructor(private val entries: Collection<Entry.Memento>) {
    /**
     * キャンバスのスナップショットの1アイテムフレーム分
     * @param player 描き込んだプレイヤー
     * @param itemFrame アイテムフレーム
     * @param mapItem マップ
     */
    private sealed class Entry(
        val player: PaintPlayer,
        val itemFrame: FrameEntity,
        val mapItem: DrawableMapItem,
    ) {
        /** 変更点をプレイヤーに送信する */
        fun updatePlayer() {
            mapItem.renderer.updatePlayer(itemFrame.location.origin)
        }

        /**
         * 変更が完了したスナップショット
         * @param data スナップショット
         */
        class Memento(
            player: PaintPlayer,
            itemFrame: FrameEntity,
            mapItem: DrawableMapItem,
            val data: DrawRollback
        ) : Entry(player, itemFrame, mapItem) {
            /** スナップショットを反映する */
            fun rollback() {
                // スナップショットがある場合は、スナップショットを反映する
                mapItem.renderer.drawBase(data)
                // スナップショットを永続化させる
                mapItem.renderer.mapLayer.apply(player)
            }
        }

        class Building(
            player: PaintPlayer,
            itemFrame: FrameEntity,
            mapItem: DrawableMapItem,
        ) : Entry(player, itemFrame, mapItem) {
            /** 変更を破棄する */
            fun clearChange() {
                // スナップショットがない場合はまさに変更中なため、レイヤーを破棄すれば戻る
                mapItem.renderer.mapLayer.clear(player)
            }

            /** 変更を適用する */
            fun applyChange(): Memento {
                // 変更点を取得 (または空変更を作成)
                val layer = mapItem.renderer.mapLayer[player]
                // 変更点をスナップショットに保存
                val data = DrawRollback(mapItem.renderer.mapLayer.base, layer)
                // スナップショットを永続化させる
                mapItem.renderer.mapLayer.apply(player)
                // 新しい状態
                return Memento(player, itemFrame, mapItem, data)
            }
        }
    }

    /** スナップショットを反映する */
    fun rollback() {
        // 変更点を戻す
        entries.forEach { it.rollback() }
    }

    /** 変更点をプレイヤーに送信する */
    fun updatePlayer() {
        // 変更点を送信
        entries.forEach { it.updatePlayer() }
    }

    /** スナップショットを記憶するツール */
    class Builder {
        /** スナップショットの記録 (mapId: (編集した箇所のアイテムフレーム, マップ)) */
        private val entries = mutableMapOf<Int, Entry.Building>()

        /** 変更があるかどうか */
        val isDirty get() = entries.isNotEmpty()

        /**
         * 描く前の内容を保存する
         * @param player 描き込んだプレイヤー
         * @param itemFrame アイテムフレーム
         * @param mapItem マップ
         */
        fun store(player: PaintPlayer, itemFrame: FrameEntity, mapItem: DrawableMapItem) {
            entries.getOrPut(mapItem.mapId) {
                // 新たに描いたマップアイテムのみ記憶
                Entry.Building(player, itemFrame, mapItem)
            }
        }

        /** 変更を適用してスナップショットを作成する */
        fun build(): CanvasMemento {
            // 変更点を適用
            val snapshot = entries.values.map { it.applyChange() }
            // 変更点をクリア
            entries.clear()
            // 履歴作成
            return CanvasMemento(snapshot)
        }

        /** 変更を破棄 */
        fun clearChange() {
            // 変更点を破棄
            entries.values.forEach { it.clearChange() }
        }

        /** 変更をクリアする */
        fun clear() {
            // 変更点をクリア
            entries.clear()
        }

        /** 変更点をプレイヤーに送信する */
        fun updatePlayer() {
            // 変更点を送信
            entries.values.forEach { it.updatePlayer() }
        }
    }
}
