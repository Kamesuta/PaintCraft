package com.kamesuta.paintcraft.util

import kotlin.math.abs

/** 限りなく0に近い数 */
const val EPSILON = 1.0E-6

/** ほぼ同じ数 */
infix fun Double.fuzzyEq(other: Double) = abs(this - other) < EPSILON
