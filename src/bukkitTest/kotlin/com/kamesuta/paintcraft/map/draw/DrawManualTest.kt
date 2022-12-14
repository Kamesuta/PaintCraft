package com.kamesuta.paintcraft.map.draw

import com.kamesuta.paintcraft.map.image.PixelImageMapBuffer
import com.kamesuta.paintcraft.map.image.mapSize
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.black
import com.kamesuta.paintcraft.util.vec.Vec2d
import java.awt.BorderLayout
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import kotlin.math.roundToInt

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
            add("East", JPanel(FlowLayout()).apply {
                // 太さのスピナーの追加
                val spinnerModel = SpinnerNumberModel(1, 0, 64, 1)
                add(JSpinner(spinnerModel).apply {
                    addChangeListener {
                        // 太さのスピナーの値を取得
                        canvas.thickness = (value as Int).toDouble()
                        // アプリ画面全体を再描画
                        this@DrawManualTest.repaint()
                    }
                })
                add(JButton("Swap").apply {
                    addActionListener {
                        // 始点と終点を入れ替える
                        canvas.swap()
                        // アプリ画面全体を再描画
                        this@DrawManualTest.repaint()
                    }
                })
                add(JButton("Update").apply {
                    addActionListener {
                        // アプリ画面全体を再描画
                        this@DrawManualTest.repaint()
                    }
                })
            })
        })

        // ウィンドウの設定
        setSize(600, 600)
        title = "DrawManualTest"
    }

    companion object {
        /** 1ピクセルの大きさ */
        const val pixelSize = 4

        /** テストツールのメイン関数 */
        @JvmStatic
        fun main(args: Array<String>) {
            // ウィンドウの作成
            val app = DrawManualTest()
            // ウィンドウの挙動の設定
            app.defaultCloseOperation = EXIT_ON_CLOSE
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

        /** 四角形 (塗りつぶし) */
        RECT_FILL,
        ;

        /**
         * 描画モードに対応する描画クラスを取得
         * @param startUv 開始座標
         * @param endUv 終了座標
         * @param thickness 太さ
         * @return 描画クラス
         */
        fun getDraw(startUv: Vec2d, endUv: Vec2d, thickness: Double): Draw {
            return when (this) {
                LINE -> DrawLine(startUv.x, startUv.y, endUv.x, endUv.y, black, thickness)
                RECT -> DrawRect(startUv.x, startUv.y, endUv.x, endUv.y, black, false, thickness)
                RECT_FILL -> DrawRect(startUv.x, startUv.y, endUv.x, endUv.y, black, true, thickness)
            }
        }
    }

    /** キャンバス */
    private class TestCanvas : JPanel() {
        /** クリックの始点座標 */
        var startUv = Vec2d(0.0, 0.0)

        /** クリックの終点座標 */
        var endUv = Vec2d(0.0, 0.0)

        /** 描画モード */
        var drawMode: DrawMode = DrawMode.LINE

        /** 太さ */
        var thickness: Double = 1.0

        /** キャンバスの左上のX座標 */
        val x1 get() = size.width / 2 - mapSize * pixelSize / 2

        /** キャンバスの左上のY座標 */
        val y1 get() = size.height / 2 - mapSize * pixelSize / 2

        init {
            // 始点座標のリスナー (クリック開始時)
            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    // クリックの始点座標を更新
                    startUv = Vec2d(
                        (e.x - x1).toDouble() / pixelSize,
                        (e.y - y1).toDouble() / pixelSize,
                    )
                    // アプリ画面全体を再描画
                    parent.repaint()
                }
            })

            // 終点座標のリスナー (クリック中)
            addMouseMotionListener(object : MouseAdapter() {
                override fun mouseDragged(e: MouseEvent) {
                    // クリックの終点座標を更新
                    endUv = Vec2d(
                        (e.x - x1).toDouble() / pixelSize,
                        (e.y - y1).toDouble() / pixelSize,
                    )
                    // アプリ画面全体を再描画
                    parent.repaint()
                }
            })
        }

        /** 始点と終点を入れ替える */
        fun swap() {
            val tmp = startUv
            startUv = endUv
            endUv = tmp
        }

        override fun paint(g: Graphics) {
            /** ピクセルデータの作成 */
            val mapCanvas = PixelImageMapBuffer()
            /** ピクセルデータに描画 */
            drawMode.getDraw(startUv, endUv, thickness).draw(mapCanvas)

            // ループでピクセルデータをJPanelに描画していく
            val x1 = x1
            val y1 = y1
            for (x in 0 until mapCanvas.width) {
                for (y in 0 until mapCanvas.height) {
                    // とりあえず基本色だけ判定
                    g.color = when (mapCanvas[x, y]) {
                        MapColors.white -> Color.LIGHT_GRAY
                        black -> Color.BLACK
                        MapColors.red -> Color.BLACK
                        MapColors.blue -> Color.BLACK
                        MapColors.green -> Color.RED
                        MapColors.transparent -> Color.WHITE
                        else -> Color.MAGENTA
                    }
                    // 四角形でピクセルを描画
                    g.fillRect(x1 + x * pixelSize, y1 + y * pixelSize, pixelSize, pixelSize)
                }
            }

            // 開始地点と終了地点を描画
            g.color = Color.RED
            g.fillRect(
                (x1 + startUv.x * pixelSize).roundToInt(),
                (y1 + startUv.y * pixelSize).roundToInt(),
                pixelSize,
                pixelSize
            )
            g.color = Color.BLUE
            g.fillRect(
                (x1 + endUv.x * pixelSize).roundToInt(),
                (y1 + endUv.y * pixelSize).roundToInt(),
                pixelSize,
                pixelSize
            )
        }
    }
}
