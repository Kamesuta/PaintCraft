package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.map.draw.Draw
import com.kamesuta.paintcraft.map.draw.DrawLine
import com.kamesuta.paintcraft.map.draw.DrawRect
import com.kamesuta.paintcraft.map.image.PixelImageMapBuffer
import com.kamesuta.paintcraft.map.image.mapSize
import com.kamesuta.paintcraft.util.vec.Vec2i
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

/**
 * 図形描画アルゴリズムの挙動を確かめるためのテストツール
 * Swingアプリケーションなので単体で起動する (このクラスをメインクラスに指定して実行)
 */
class DrawManualTest : JFrame() {
    /** 描画するテストキャンバス */
    private var canvas = TestCanvas()

    init {
        // キャンバスの追加
        add("Center", canvas)
        // 調整用のパネルの追加
        add("North", JPanel(BorderLayout()).apply {
            // モード切替のコンボボックスの追加
            add("Center", JComboBox(DrawMode.values()).apply {
                addActionListener {
                    // モード切替のコンボボックスの選択値を取得
                    val selected = getItemAt(selectedIndex)
                        ?: return@addActionListener
                    canvas.drawMode = selected
                    // アプリ画面全体を再描画
                    this@DrawManualTest.repaint()
                }
            })
            // 太さのスピナーの追加
            val spinnerModel = SpinnerNumberModel(1, 0, 15, 1)
            add("East", JSpinner(spinnerModel).apply {
                addChangeListener {
                    // 太さのスピナーの値を取得
                    canvas.thickness = value as Int
                    // アプリ画面全体を再描画
                    this@DrawManualTest.repaint()
                }
            })
        })

        // ウィンドウの設定
        setSize(600, 600)
        title = "DrawManualTest"
    }

    companion object {
        /** テストツールのメイン関数 */
        @JvmStatic
        fun main(args: Array<String>) {
            // ウィンドウの作成
            val app = DrawManualTest()
            // ウィンドウの挙動の設定
            app.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            // ウィンドウの表示
            app.isVisible = true
        }
    }

    /** 描画モード */
    private enum class DrawMode {
        /** 直線 */
        LINE,

        /** 四角形 */
        RECT,
        ;

        /**
         * 描画モードに対応する描画クラスを取得
         * @param startUv 開始座標
         * @param endUv 終了座標
         * @param thickness 太さ
         * @return 描画クラス
         */
        fun getDraw(startUv: Vec2i, endUv: Vec2i, thickness: Int): Draw {
            return when (this) {
                LINE -> DrawLine(startUv.x, startUv.y, endUv.x, endUv.y, TestCanvas.black, thickness)
                RECT -> DrawRect(startUv.x, startUv.y, endUv.x, endUv.y, TestCanvas.black, false, thickness)
            }
        }
    }

    /** キャンバス */
    private class TestCanvas : JPanel() {
        /** クリックの始点座標 */
        var startUv = Vec2i(0, 0)

        /** クリックの終点座標 */
        var endUv = Vec2i(0, 0)

        /** 描画モード */
        var drawMode: DrawMode = DrawMode.LINE

        /** 太さ */
        var thickness: Int = 1

        /** キャンバスの左上のX座標 */
        val x1 get() = size.width / 2 - mapSize * pixelSize / 2

        /** キャンバスの左上のY座標 */
        val y1 get() = size.height / 2 - mapSize * pixelSize / 2

        init {
            // 始点座標のリスナー (クリック開始時)
            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    // クリックの始点座標を更新
                    startUv = Vec2i(
                        (e.x - x1) / pixelSize,
                        (e.y - y1) / pixelSize,
                    )
                    // アプリ画面全体を再描画
                    parent.repaint()
                }
            })

            // 終点座標のリスナー (クリック中)
            addMouseMotionListener(object : MouseAdapter() {
                override fun mouseDragged(e: MouseEvent) {
                    // クリックの終点座標を更新
                    endUv = Vec2i(
                        (e.x - x1) / pixelSize,
                        (e.y - y1) / pixelSize,
                    )
                    // アプリ画面全体を再描画
                    parent.repaint()
                }
            })
        }

        override fun paint(g: Graphics) {
            /** ピクセルデータの作成 */
            val mapCanvas = PixelImageMapBuffer()
            /** ピクセルデータに描画 */
            drawMode.getDraw(startUv, endUv, thickness).draw(mapCanvas);

            // ループでピクセルデータをJPanelに描画していく
            val x1 = x1
            val y1 = y1
            for (x in 0 until mapCanvas.width) {
                for (y in 0 until mapCanvas.height) {
                    // とりあえず黒か白かだけ判定
                    g.color = if (mapCanvas[x, y] == black) Color.BLACK else Color.WHITE
                    // 四角形でピクセルを描画
                    g.fillRect(x1 + x * pixelSize, y1 + y * pixelSize, pixelSize, pixelSize)
                }
            }

            // 開始地点と終了地点を描画
            g.color = Color.RED
            g.fillRect(x1 + startUv.x * pixelSize, y1 + startUv.y * pixelSize, pixelSize, pixelSize)
            g.fillRect(x1 + endUv.x * pixelSize, y1 + endUv.y * pixelSize, pixelSize, pixelSize)
        }

        companion object {
            /** 1ピクセルの大きさ */
            const val pixelSize = 4

            /** 黒のマップカラー */
            const val black: Byte = 58
        }
    }
}
