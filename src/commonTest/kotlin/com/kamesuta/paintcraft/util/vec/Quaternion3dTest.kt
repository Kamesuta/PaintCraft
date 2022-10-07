package com.kamesuta.paintcraft.util.vec

import com.kamesuta.paintcraft.util.EPSILON
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.math.cos
import kotlin.math.sin

class Quaternion3dTest : FunSpec({
    test("complex") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        q.complex shouldBe Vec3d(1.0, 2.0, 3.0)
    }

    test("conjugate") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        q.conjugate shouldBe Quaternion3d(-1.0, -2.0, -3.0, 4.0)
    }

    test("negate") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        -q shouldBe near(Quaternion3d(-1.0, -2.0, -3.0, -4.0))
        q.negate shouldBe near(Quaternion3d(-1.0, -2.0, -3.0, -4.0))
    }

    test("length") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        q.length shouldBe near(5.477225575051661)
    }

    test("lengthSquared") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        q.lengthSquared shouldBe near(30.0)
    }

    test("normalized") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        q.normalized shouldBe near(
            Quaternion3d(
                0.18257418583505536,
                0.3651483716701107,
                0.5477225575051661,
                0.7302967433402214
            )
        )
    }

    test("inverse") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        q.inverse shouldBe near(
            Quaternion3d(
                -0.18257418583505536,
                -0.3651483716701107,
                -0.5477225575051661,
                0.7302967433402214
            )
        )
    }

    test("product") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        val r = Quaternion3d(5.0, 6.0, 7.0, 8.0)
        val qr = Quaternion3d(24.0, 48.0, 48.0, -6.0)
        val rq = Quaternion3d(32.0, 32.0, 56.0, -6.0)
        q * r shouldBe near(qr)
        q.product(r) shouldBe near(qr)
        r * q shouldBe near(rq)
        r.product(q) shouldBe near(rq)
    }

    test("dot") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        val r = Quaternion3d(5.0, 6.0, 7.0, 8.0)
        q.dot(r) shouldBe near(70.0)
        r.dot(q) shouldBe near(70.0)
    }

    test("times") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        q * 2.0 shouldBe near(Quaternion3d(2.0, 4.0, 6.0, 8.0))
    }

    test("div") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        q / 2.0 shouldBe near(Quaternion3d(0.5, 1.0, 1.5, 2.0))
    }

    test("plus") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        val r = Quaternion3d(5.0, 6.0, 7.0, 8.0)
        q + r shouldBe near(Quaternion3d(6.0, 8.0, 10.0, 12.0))
    }

    test("minus") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        val r = Quaternion3d(5.0, 6.0, 7.0, 8.0)
        q - r shouldBe near(Quaternion3d(-4.0, -4.0, -4.0, -4.0))
    }

    test("scaleRotation") {
        val q = Quaternion3d(1.0, 2.0, 3.0, 4.0)
        q.scaleRotation(2.0) shouldBe near(
            Quaternion3d(
                0.26666666666666666,
                0.5333333333333333,
                0.8,
                0.06666666666666647
            )
        )

        // 回転が2倍
        val axis = Vec3d(1.0, 2.0, 3.0)
        val angle = Math.toRadians(30.0)
        val q2 = Quaternion3d.axisAngle(axis, angle)
        val (axis2, angle2) = q2.scaleRotation(2.0).toAxisAngle()
        axis2 shouldBe near(axis.normalized)
        angle2 shouldBe near(angle * 2.0)
    }

    test("transform") {
        // Y軸周りに90度回転
        val angle = Math.toRadians(90.0)
        val q = Quaternion3d(0.0, sin(angle / 2.0), 0.0, cos(angle / 2.0))
        val v = Vec3d(5.0, 6.0, 7.0)
        q.transform(v) shouldBe near(Vec3d(7.0, 6.0, -5.0))
    }

    test("axisAngle") {
        val axis = Vec3d(1.0, 2.0, 3.0)
        val angle = Math.toRadians(30.0)
        val q = Quaternion3d.axisAngle(axis, angle)
        q shouldBe near(Quaternion3d(0.06917229942468747, 0.13834459884937494, 0.20751689827406242, 0.9659258262890683))
        val (axis2, angle2) = q.toAxisAngle()
        axis2 shouldBe near(axis.normalized)
        angle2 shouldBe near(angle)
    }

    test("rotate") {
        Quaternion3d.rotateX(0.3) shouldBe near(Quaternion3d.axisAngle(Vec3d.AxisX, 0.3))
        Quaternion3d.rotateY(0.4) shouldBe near(Quaternion3d.axisAngle(Vec3d.AxisY, 0.4))
        Quaternion3d.rotateZ(0.5) shouldBe near(Quaternion3d.axisAngle(Vec3d.AxisZ, 0.5))
    }

    test("transform axis") {
        val angle = Math.toRadians(45.0)
        val q = Quaternion3d.axisAngle(Vec3d(1.0, 1.0, 1.0), angle)
        val v = Vec3d(5.0, 6.0, 7.0)
        q.transform(v) shouldBe near(Vec3d(5.701141509277316, 5.183503419072273, 7.115355071650411))
    }

    test("multiplication") {
        val axis = Vec3d(4.3, 7.6, 1.2).normalized
        val angle1 = 1.2
        val angle2 = 0.7
        val angle3 = angle2 + EPSILON * 10
        val qaa1 = Quaternion3d.axisAngle(axis, angle1)
        val qaa2 = Quaternion3d.axisAngle(axis, angle2)
        val qaa3 = Quaternion3d.axisAngle(axis, angle3)

        // 2つのクォータニオンの積は回転の和に等しい
        run {
            val (convertedAxis1, convertedAngle1) = (qaa1 * qaa2).toAxisAngle()
            (angle1 + angle2) shouldBe near(convertedAngle1)
            convertedAxis1 shouldBe near(axis)
            // クォータニオンにスカラーを掛けると回転がスケーリングされる
            val (convertedAxis2, convertedAngle2) = qaa1.scaleRotation(2.0).toAxisAngle()
            (angle1 * 2.0) shouldBe near(convertedAngle2)
            convertedAxis2 shouldBe near(axis)
        }

        // 2つのクォータニオンを線形補間すると回転の線形補間に等しい
        run {
            val slerp1 = Quaternion3d.slerp(qaa1, qaa2, 0.5)
            val (convertedAxis1, convertedAngle1) = slerp1.toAxisAngle()
            (0.5 * (angle1 + angle2)) shouldBe near(convertedAngle1)
            convertedAxis1 shouldBe near(axis)
            val slerp2 = Quaternion3d.slerp(qaa2, qaa3, 0.5)
            val (convertedAxis2, convertedAngle2) = slerp2.toAxisAngle()
            (0.5 * (angle2 + angle3)) shouldBe near(convertedAngle2)
            convertedAxis2 shouldBe near(axis)
            val slerp3 = Quaternion3d.slerp(qaa2, qaa2, 0.5)
            val (convertedAxis3, convertedAngle3) = slerp3.toAxisAngle()
            angle2 shouldBe near(convertedAngle3)
            convertedAxis3 shouldBe near(axis)
        }
    }

    // slerp()とoperator times()は
    test("slerp") {
        // 簡単で明確なケース
        checkSlerp(+160.0, 0.375, +60.0)
        checkSlerp(-160.0, 0.375, -60.0)

        // 長い回転を短くする (180度以上の回転)
        // NOTE: これらの結果は数学的なクォータニオンのslerpとは異なる
        checkSlerp(+320.0, 0.375, -15.0)  // 数学的には+120
        checkSlerp(-320.0, 0.375, +15.0)  // 数学的には-120

        // 長い回転を長くする
        checkSlerp(320.0, 1.5, -60.0)  // 数学的には480(つまり-240)

        // 長い回転を長くする (180度以上の回転)
        checkSlerp(+70.0, 3.0, +210.0)
        checkSlerp(-70.0, 3.0, -210.0)

        // しばしばNaNを引き起こすエッジケース
        checkSlerp(0.0, 0.5, 0.0)

        // この直感的なエッジケースは不明瞭であり、テストできない
        checkSlerp(180.0, 0.25, 45.0)

        // 逆に、この直感的なエッジケースは明確である
        // 数学的なslerpでは、軸は不明瞭であり、多くの値を取ることができる
        checkSlerp(360.0, 0.25, 0.0)
    }

    test("euler") {

    }

}) {
    companion object {
        fun checkSlerp(angle: Double, t: Double, expectedAngle: Double) {
            val original = Quaternion3d.axisAngle(Vec3d.AxisY, Math.toRadians(angle))
            val expected = Quaternion3d.axisAngle(Vec3d.AxisY, Math.toRadians(expectedAngle))

            // これらは、Slerp()が四元数を姿勢として扱うため、厳密なnearチェックよりも緩いnearOrientationチェックを使用しています。
            // 数学的なSlerp()をチェックするには、nearに戻す必要があります。
            val slerpResult = Quaternion3d.slerp(Quaternion3d.Identity, original, t)
            slerpResult shouldBe nearOrientation(expected)

            // slerp(a, b, t) == slerp(b, a, 1-t)という条件を満たしていることを確認します。
            val slerpBackwardsResult = Quaternion3d.slerp(original, Quaternion3d.Identity, 1.0 - t)
            slerpBackwardsResult shouldBe nearOrientation(expected)

            // 元の回転にtを書けたときに得られる回転は、期待される回転と等しいことを確認します。
            val mulResult = original.scaleRotation(t)
            mulResult shouldBe nearOrientation(expected)
        }
    }
}