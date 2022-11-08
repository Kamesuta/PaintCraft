package com.kamesuta.paintcraft.map.image

import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.black
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.transparent
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Warmup(iterations = 1)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
class PixelImageCacheBufferBenchmark {
    private lateinit var base: PixelImageMapBuffer
    private lateinit var buffer: PixelImageMapBuffer

    @Setup
    fun init() {
        base = PixelImageMapBuffer()
        buffer = PixelImageMapBuffer().apply {
            for (i in pixels.indices) {
                pixels[i] = if (i % 2 == 0) transparent else black
            }
        }
    }

    @Benchmark
    fun benchmarkCopyLocalVariable() {
        base.drawPixelImageFast(0, 0, buffer)
    }

    @Benchmark
    fun benchmarkCopyMemberAccess() {
        base.drawPixelImageFastDirect(0, 0, buffer)
    }

    companion object {
        private fun PixelImage.drawPixelImageFast(x: Int, y: Int, src: PixelImage) {
            val dstPixels = pixels
            val dstWidth = width
            val dstHeight = height
            val srcPixels = src.pixels
            val srcWidth = src.width
            for (iy in 0 until dstHeight) {
                System.arraycopy(srcPixels, (x + (y + iy) * srcWidth), dstPixels, iy * dstWidth, dstWidth)
            }
        }

        private fun PixelImage.drawPixelImageFastDirect(x: Int, y: Int, src: PixelImage) {
            for (iy in 0 until height) {
                System.arraycopy(src.pixels, (x + (y + iy) * src.width), pixels, iy * width, width)
            }
        }
    }
}