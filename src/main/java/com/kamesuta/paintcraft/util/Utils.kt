package com.kamesuta.paintcraft.util

/**
 * 名前に対応する列挙の値を返す
 * 見つからなかった場合はnullを返す
 * @param name 列挙の名前
 * @return 列挙の値
 */
inline fun <reified T : Enum<*>> enumValueOrNull(name: String): T? =
    T::class.java.enumConstants.firstOrNull { it.name == name }