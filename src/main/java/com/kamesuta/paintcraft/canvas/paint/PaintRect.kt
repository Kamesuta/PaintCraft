package com.kamesuta.paintcraft.canvas.paint

import com.kamesuta.paintcraft.canvas.CanvasActionType
import com.kamesuta.paintcraft.canvas.CanvasSession
import com.kamesuta.paintcraft.canvas.paint.tool.PaintDrawTool
import com.kamesuta.paintcraft.map.draw.DrawRect
import com.kamesuta.paintcraft.util.vec.Line3d
import com.kamesuta.paintcraft.util.vec.normalized
import com.kamesuta.paintcraft.util.vec.plus
import com.kamesuta.paintcraft.util.vec.times
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
        val color = session.drawing.palette.color

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
                // 復元
                session.drawing.edited.editing.rollback()
                // 線を描く
                drawRect(event, color)
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
                session.drawing.edited.store(itemFrame, mapItem)
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
}