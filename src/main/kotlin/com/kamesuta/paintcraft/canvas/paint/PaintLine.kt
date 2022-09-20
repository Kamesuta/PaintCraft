package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.tool.PaintDrawTool
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.normalized
import com.kamesuta.paintcraft.util.vec.plus
import com.kamesuta.paintcraft.util.vec.times
import org.bukkit.entity.ItemFrame

/**
 * 右クリック2点で線が引けるツール
 * @param session セッション
 */
class PaintLine(override val session: CanvasSession) : PaintTool {
    /** 線を描き中、触れたことがあるアイテムフレーム */
    private val entities = mutableSetOf<ItemFrame>()

    override fun beginPainting(event: PaintEvent) {
        // リセット
        entities.clear()
    }

    override fun paint(event: PaintEvent) {
        // 描く色
        val color = session.mode.mapColor

        // キャンバスに描く
        when (event.drawMode) {
            // 描くモードが左クリックの場合
            CanvasActionType.LEFT_CLICK -> {
                // 復元 (前回の状態を破棄)
                session.drawing.edited.editing.rollback()
                session.drawing.edited.clear()
            }
            // 描くモードが右クリックの場合
            CanvasActionType.RIGHT_CLICK -> {
                // アイテムフレームを登録
                entities.add(event.interact.ray.itemFrame)
                // 復元
                session.drawing.edited.editing.rollback()
                // 線を描く
                drawLine(event, color)
            }
            // その他 (想定外)
            else -> {
                // 何もしない
            }
        }
    }

    override fun getGuideLine(line: Line3d): Line3d {
        // 始点のイベント
        val startEvent = session.drawing.startEvent
            ?: return line

        // 上方向と右方向
        val right = startEvent.interact.ray.frameLocation.right
        val up = startEvent.interact.ray.frameLocation.up

        // 各方向を列挙
        val minDirection = sequence {
            for (y in -1..1) {
                for (x in -1..1) {
                    // 0,0,0 は除外
                    if (x == 0 && y == 0) continue
                    // 各方向の方向ベクトルを返す
                    yield((right * x.toDouble() + up * -y.toDouble()).normalized)
                }
            }
        }.minBy {
            // 各方向のベクトルと線の方向ベクトルの内積が最小のベクトル
            it.dot(line.direction)
        }
        // 方向が一番近い方向に伸びている先の点
        val minLocation = Line3d(line.origin, minDirection).closestPoint(line.target)
        // 線分を作成
        return Line3d.fromPoints(line.origin, minLocation)
    }

    /**
     * 線を描く
     * @param event 描きこむイベント
     * @param color 描く色
     */
    private fun drawLine(
        event: PaintEvent,
        color: Byte
    ) {
        // 最後の点+現在の点を結ぶ線を描く
        session.drawing.startEvent?.let {
            // 描画
            PaintDrawTool.drawLine(session, event, it, entities) {
                // 後で戻せるよう記憶しておく
                session.drawing.edited.store(itemFrame, mapItem)
                // マップに描きこむ
                mapItem.draw {
                    g(
                        DrawLine(
                            uvStart.x,
                            uvStart.y,
                            uvEnd.x,
                            uvEnd.y,
                            color,
                            session.mode.thickness.toInt(),
                        )
                    )
                }
            }
        }
    }
}