package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.tool.PaintDrawTool
import com.kamesuta.paintcraft.map.DrawableMapItem
import com.kamesuta.paintcraft.map.draw.DrawRect
import com.kamesuta.paintcraft.map.draw.DrawRollback
import com.kamesuta.paintcraft.util.vec.*
import org.bukkit.entity.ItemFrame
import org.bukkit.map.MapPalette
import java.awt.Color
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

/**
 * 始点と終点で長方形を描画する
 * @param session セッション
 */
class PaintRect(override val session: CanvasSession) : PaintTool {
    override fun paint(event: PaintEvent) {
        // 描く色
        @Suppress("DEPRECATION")
        val color = MapPalette.matchColor(Color.BLACK)

        // キャンバスに描く
        when (event.drawMode) {
            // 描くモードが左クリックの場合
            CanvasActionType.LEFT_CLICK -> {
                // 復元 (前回の状態を破棄)
                rollback(rollbackCanvas = true, deleteRollback = true)
            }
            // 描くモードが右クリックの場合
            CanvasActionType.RIGHT_CLICK -> {
                // 復元
                rollback(rollbackCanvas = true, deleteRollback = false)
                // 線を描く
                drawRect(event, color)
            }
            // その他 (想定外)
            else -> {
                // 何もしない
            }
        }

        // 変更箇所をプレイヤーに送信
        session.drawing.edited.values.forEach { (itemFrame, drawableMap) ->
            drawableMap.renderer.updatePlayer(itemFrame.location.origin)
        }
    }

    override fun endPainting() {
        // 前回の状態に破棄
        rollback(rollbackCanvas = false, deleteRollback = true)
    }

    override fun getGuideLine(line: Line3d): Line3d {
        // 始点のイベント
        val startEvent = session.drawing.startEvent
            ?: return line

        // 上方向と右方向
        val right = startEvent.interact.ray.frameLocation.right
        val up = startEvent.interact.ray.frameLocation.up

        // 各方向を列挙
        val closestDirection = sequence {
            for (y in arrayOf(-1, 1)) {
                for (x in arrayOf(-1, 1)) {
                    // 各方向の方向ベクトルを返す
                    yield((right * x.toDouble() + up * -y.toDouble()).normalized)
                }
            }
        }.maxBy {
            // 各方向のベクトルと線の方向ベクトルの内積が最小のベクトル
            it.dot(line.direction)
        }
        // 方向が一番近い方向に伸びている先の点
        val closestLength = min(
            abs(Line3d(line.origin, right).closestPointSignedDistance(line.target)),
            abs(Line3d(line.origin, up).closestPointSignedDistance(line.target)),
        )
        // 線分を作成
        return Line3d(line.origin, closestDirection * closestLength * 2.0.pow(0.5))
    }

    /**
     * 長方形を描く
     * @param event 描きこむイベント
     * @param color 描く色
     */
    private fun drawRect(
        event: PaintEvent,
        color: Byte
    ) {
        // 最後の点+現在の点を結ぶ長方形を描く
        session.drawing.startEvent?.let {
            // 描画
            PaintDrawTool.drawRect(session, event, it) {
                // 後で戻せるよう記憶しておく
                store(itemFrame, mapItem)
                // マップに描きこむ
                mapItem.draw {
                    g(
                        DrawRect(
                            uvStart.x,
                            uvStart.y,
                            uvEnd.x,
                            uvEnd.y,
                            color,
                            false,
                        )
                    )
                }
            }
        }
    }

    /**
     * 描く前の内容を保存する
     * @param itemFrame アイテムフレーム
     * @param mapItem マップ
     */
    private fun store(itemFrame: ItemFrame, mapItem: DrawableMapItem) {
        session.drawing.edited.computeIfAbsent(mapItem.mapView.id) {
            // 新たに描いたマップアイテムのみ記憶
            mapItem.renderer.previewBefore = DrawRollback(mapItem.renderer.mapCanvas)
            itemFrame to mapItem
        }
    }

    /**
     * 新たに描いた内容を取り消す
     * @param rollbackCanvas trueならキャンバスを復元する
     * @param deleteRollback 前回の状態を破棄するかどうか
     */
    private fun rollback(rollbackCanvas: Boolean, deleteRollback: Boolean) {
        session.drawing.edited.values.forEach { (_, mapItem) ->
            if (rollbackCanvas) {
                // キャンバスを復元
                mapItem.renderer.previewBefore?.let {
                    mapItem.draw {
                        // キャンバスに描く
                        g(it)
                    }
                }
            }
            // 前回の状態を消す
            if (deleteRollback) {
                mapItem.renderer.previewBefore = null
            }
        }
        if (deleteRollback) {
            // 描いた内容の記憶を消す
            session.drawing.edited.clear()
        }
    }
}