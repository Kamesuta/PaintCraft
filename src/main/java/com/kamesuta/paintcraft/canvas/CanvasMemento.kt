package com.kamesuta.paintcraft.canvas

import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.draw.DrawRollback
import com.kamesuta.paintcraft.util.vec.origin
import org.bukkit.entity.ItemFrame

/**
 * キャンバスのスナップショット
 * @param entries 変更されたキャンバスのエントリー
 */
data class CanvasMemento(val entries: List<Entry>) {
    /**
     * キャンバスのスナップショットの1アイテムフレーム分
     * @param itemFrame アイテムフレーム
     * @param mapItem マップ
     * @param data スナップショット
     */
    data class Entry(val itemFrame: ItemFrame, val mapItem: DrawableMapItem, val data: DrawRollback)

    /**
     * スナップショットを反映する
     */
    fun apply() {
        entries.forEach { (_, mapItem, data) ->
            mapItem.draw {
                g(data)
            }
        }
    }

    /**
     * 変更点をプレイヤーに送信する
     */
    fun updatePlayer() {
        entries.forEach { (itemFrame, mapItem, _) ->
            mapItem.renderer.updatePlayer(itemFrame.location.origin)
        }
    }
}
