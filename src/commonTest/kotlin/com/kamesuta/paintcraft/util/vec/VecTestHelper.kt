package com.kamesuta.paintcraft.util.vec

import com.kamesuta.paintcraft.util.EPSILON
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.doubles.plusOrMinus

/** 誤差を考慮して数値が同一 */
fun near(value: Double) = value plusOrMinus EPSILON

/** 誤差を考慮してベクトルが同一 */
fun near(value: Vec3d) = Matcher<Vec3d> {
    return@Matcher MatcherResult(
        near(value.x).test(it.x).passed() &&
                near(value.y).test(it.y).passed() &&
                near(value.z).test(it.z).passed(),
        "Vec3d $it should be near $value",
        "Vec3d $it should not be near $value"
    )
}

/** 誤差を考慮して線分の数値が同一 */
fun near(value: Line3d) = Matcher<Line3d> {
    return@Matcher MatcherResult(
        near(value.origin).test(it.origin).passed() &&
                near(value.direction).test(it.direction).passed(),
        "Line3d $it should be near $value",
        "Line3d $it should not be near $value"
    )
}

/** 誤差を考慮して半直線の位置と向きが同一 */
fun nearDirection(value: Line3d) = Matcher<Line3d> {
    return@Matcher MatcherResult(
        near(value.origin).test(it.origin).passed() &&
                near(value.direction.normalized).test(it.direction.normalized).passed(),
        "Line3d $it should be near $value",
        "Line3d $it should not be near $value"
    )
}

/** 誤差を考慮して平面の数値が同一 */
fun near(value: Plane3d) = Matcher<Plane3d> {
    return@Matcher MatcherResult(
        near(value.a).test(it.a).passed() &&
                near(value.b).test(it.b).passed() &&
                near(value.c).test(it.c).passed() &&
                near(value.d).test(it.d).passed(),
        "Plane3d $it should be near $value",
        "Plane3d $it should not be near $value"
    )
}

/** 誤差を考慮して数学的にクォータニオンの数値が同一 */
fun near(value: Quaternion3d) = Matcher<Quaternion3d> {
    return@Matcher MatcherResult(
        near(value.x).test(it.x).passed() &&
                near(value.y).test(it.y).passed() &&
                near(value.z).test(it.z).passed() &&
                near(value.w).test(it.w).passed(),
        "Quaternion3d $it should be near $value",
        "Quaternion3d $it should not be near $value"
    )
}

/** 誤差を考慮してクォータニオンの回転が同一 */
fun nearOrientation(value: Quaternion3d) = Matcher<Quaternion3d> {
    val q = if (value.dot(it) < 0) -value else value
    near(q).test(it)
}
