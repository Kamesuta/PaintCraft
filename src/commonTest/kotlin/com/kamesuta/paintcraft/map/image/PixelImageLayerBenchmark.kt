package com.kamesuta.paintcraft.map.image

import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.black
import com.kamesuta.paintcraft.util.color.RGBColor.MapColors.transparent
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Warmup(iterations = 1)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
class PixelImageLayerBenchmark {
    private lateinit var layer: PixelImageLayer<Int>

    @Setup
    fun init() {
        layer = PixelImageLayer<Int>().apply {
            for (layerNum in 1..9) {
                this[layerNum].apply {
                    for (i in pixels.indices) {
                        pixels[i] = if (i / layerNum % 2 == 0) transparent else black
                    }
                    dirty.flagDirty(0, 0)
                    dirty.flagDirty(mapSize - 1, mapSize - 1)
                }
            }
        }
    }

    private val layers = mutableListOf<Pair<Int, PixelImageMapBuffer>>()

    @Benchmark
    fun benchmark0Compose() {
        layers.forEach { (_, layer) ->
            layer.dirty
        }
    }

    @Benchmark
    fun benchmark1ComposeFast() {
        for ((_, layer) in layers) {
            layer.dirty
        }
    }

    @Benchmark
    fun benchmark2ComposeFast2() {
        for (it in layers) {
            it.second.dirty
        }
    }

    @Benchmark
    fun benchmark3Compose2() {
        layers.forEach {
            it.second.dirty
        }
    }
}