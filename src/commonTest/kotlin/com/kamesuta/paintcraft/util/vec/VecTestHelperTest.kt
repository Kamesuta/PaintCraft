package com.kamesuta.paintcraft.util.vec

import com.kamesuta.paintcraft.util.EPSILON
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class VecTestHelperTest : FunSpec({
    test("近似値比較テスト (Vec3d)") {
        Vec3d(1.0, 0.0, EPSILON) shouldBe near(Vec3d(1.0, 0.0, 0.0))
        Vec3d(1.0, 0.0, EPSILON) shouldNotBe near(Vec3d(1.0, 0.0, 1.0))
    }

    test("近似値比較テスト (Line3d)") {
        // 誤差を考慮して線分の位置と向きが同一
        Line3d(Vec3d(1.0, 0.0, EPSILON), Vec3d(0.0, 1.0, EPSILON)) shouldBe
                near(Line3d(Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 1.0, 0.0)))
        Line3d(Vec3d(1.0, 0.0, EPSILON), Vec3d(0.0, 1.0, EPSILON)) shouldNotBe
                near(Line3d(Vec3d(1.0, 0.0, 1.0), Vec3d(0.0, 1.0, 1.0)))

        // 方向のみ比較
        Line3d(Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 1.0, 0.0)) shouldBe
                near(Line3d(Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 1.0, 0.0)))
        Line3d(Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 1.0, 0.0)) shouldNotBe
                near(Line3d(Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 2.0, 0.0)))
        Line3d(Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 1.0, 0.0)) shouldBe
                nearDirection(Line3d(Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 2.0, 0.0)))
    }

    test("近似値比較テスト (Quaternion3d)") {
        // 誤差を考慮して線分の位置と向きが同一
        Quaternion3d(EPSILON, 0.0, 0.0, 1.0) shouldBe near(Quaternion3d.Identity)

        // 反対のクォータニオンはnearOrientationのみ一致することを確認
        val q2 = Quaternion3d(3.0, 4.0, 5.0, 6.0).normalized
        val q2Negated = q2.negate
        q2 shouldNotBe near(q2Negated)
        q2 shouldBe nearOrientation(q2Negated)
    }
})
