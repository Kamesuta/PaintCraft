package com.kamesuta.paintcraft.util.vec

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Line2dTest : FunSpec({
    test("intersectSegmentExclusive") {
        val line1 = Line2d.fromPoints(Vec2d(0.0, 0.0), Vec2d(1.0, 0.0))
        val line2 = Line2d.fromPoints(Vec2d(2.0, 0.0), Vec2d(3.0, 0.0))
        line1.intersectSegment(line2) shouldBe null
        line2.intersectSegment(line1) shouldBe null
    }

    test("intersectSegmentInclusive") {
        val lineLong = Line2d.fromPoints(Vec2d(0.0, 0.0), Vec2d(3.0, 0.0))
        val lineShort = Line2d.fromPoints(Vec2d(1.0, 0.0), Vec2d(2.0, 0.0))
        lineLong.intersectSegment(lineShort) shouldBe near(lineShort)
        lineShort.intersectSegment(lineLong) shouldBe near(lineShort)
    }

    test("intersectSegmentOverlap") {
        val line1 = Line2d.fromPoints(Vec2d(0.0, 0.0), Vec2d(2.0, 0.0))
        val line2 = Line2d.fromPoints(Vec2d(1.0, 0.0), Vec2d(3.0, 0.0))
        val lineResult = Line2d.fromPoints(Vec2d(1.0, 0.0), Vec2d(2.0, 0.0))
        line1.intersectSegment(line2) shouldBe near(lineResult)
        line2.intersectSegment(line1) shouldBe near(lineResult)
    }
})