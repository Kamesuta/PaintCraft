package com.kamesuta.paintcraft.map.image.debug

import com.kamesuta.paintcraft.map.image.mapSize
import com.kamesuta.paintcraft.util.color.RGBColor
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.transparent
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.unchanged
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

/**
 * ピクセルデータの中身を可視化するためのテストツール
 */
class PixelImageManualTest : JFrame() {
    /** 描画するテストキャンバス */
    private var canvas = TestCanvas()

    init {
        // キャンバスの追加
        add("Center", canvas)
        // 調整用のパネルの追加
        add("North", JPanel(BorderLayout()).apply {
            add(JButton("Update").apply {
                addActionListener {
                    // アプリ画面全体を再描画
                    this@PixelImageManualTest.repaint()
                }
            })
        })

        // ウィンドウの設定
        setSize(600, 600)
        title = "PixelImageManualTest"

        // ピクセルが更新されたときに更新
        onDebugPixelImageChanged = {
            repaint()
        }
        // 終了
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onDebugPixelImageChanged = null
            }
        })
    }

    companion object {
        /** 1ピクセルの大きさ */
        const val pixelSize = 4

        /** テストツールのメイン関数 */
        fun startTool() {
            // ウィンドウの作成
            val app = PixelImageManualTest()
            // ウィンドウの挙動の設定
            app.defaultCloseOperation = DISPOSE_ON_CLOSE
            // ウィンドウの表示
            app.isVisible = true
        }
    }

    /** キャンバス */
    private class TestCanvas : JPanel() {
        /** キャンバスの左上のX座標 */
        val x1 get() = size.width / 2 - mapSize * pixelSize / 2

        /** キャンバスの左上のY座標 */
        val y1 get() = size.height / 2 - mapSize * pixelSize / 2

        override fun paint(g: Graphics) {
            // ピクセルデータの取得
            val mapCanvas = debugPixelImage
                ?: return

            // ループでピクセルデータをJPanelに描画していく
            val x1 = x1
            val y1 = y1
            for (x in 0 until mapCanvas.width) {
                for (y in 0 until mapCanvas.height) {
                    // 色を変換
                    val mapColor = mapCanvas[x, y]
                    g.color = when (mapColor) {
                        transparent -> Color(128, 128, 128)
                        unchanged -> Color(255, 0, 255)
                        else -> RGBColor.fromMapColor(mapColor).color
                    }
                    // 四角形でピクセルを描画
                    g.fillRect(x1 + x * pixelSize, y1 + y * pixelSize, pixelSize, pixelSize)
                }
            }
        }
    }
}
