package com.kamesuta.paintcraft.util

import org.bukkit.Bukkit
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * リフレクションを使用して、NMSにアクセスするクラス
 */
object ReflectionAccessor {
    /** NMSクラス名 */
    private val NMS: String

    init {
        // Bukkitクラスのパッケージ名からNMSのパッケージ名を取得する
        val version = Bukkit.getServer().javaClass.getPackage().name
        NMS = version.replace("org.bukkit.craftbukkit", "net.minecraft.server")
    }

    /**
     * フィールドの値を取得する
     * @param obj 対象のオブジェクト
     * @param fieldName フィールド名
     * @return フィールドの値
     */
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun getField(obj: Any, fieldName: String): Any {
        val field: Field
        try {
            field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
        } catch (e: NoSuchFieldException) {
            // 指定したフィールドが見つからない
            throw NoSuchFieldException(
                String.format(
                    "Field '%s' could not be found in '%s'. Fields found: {%s}",
                    fieldName, obj.javaClass.name, listOf(*obj.javaClass.declaredFields)
                )
            )
        }
        return field[obj]
    }

    /**
     * 親クラスのフィールドの値を取得する
     * @param obj 対象のオブジェクト
     * @param fieldName フィールド名
     * @return フィールドの値
     */
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun getSuperField(obj: Any, fieldName: String): Any {
        val field: Field
        try {
            field = obj.javaClass.superclass.getDeclaredField(fieldName)
            field.isAccessible = true
        } catch (e: NoSuchFieldException) {
            // 指定したフィールドが見つからない
            throw NoSuchFieldException(
                String.format(
                    "Field '%s' could not be found in '%s'. Fields found: {%s}",
                    fieldName, obj.javaClass.name, listOf(*obj.javaClass.declaredFields)
                )
            )
        }
        return field[obj]
    }

    /**
     * フィールドの値を設定する
     * @param obj 対象のオブジェクト
     * @param fieldName フィールド名
     * @param value 設定する値
     */
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun setField(obj: Any, fieldName: String, value: Any?) {
        val field: Field
        try {
            field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
        } catch (e: NoSuchFieldException) {
            // 指定したフィールドが見つからない
            throw NoSuchFieldException(
                String.format(
                    "Field '%s' could not be found in '%s'. Fields found: [%s]",
                    fieldName, obj.javaClass.name, listOf(*obj.javaClass.declaredFields)
                )
            )
        }
        field[obj] = value
    }

    /**
     * メソッドを実行する
     * @param obj 対象のオブジェクト
     * @param methodName メソッド名
     * @param params 引数
     * @return メソッドの戻り値
     */
    @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
    fun invokeMethod(obj: Any, methodName: String, vararg params: Any): Any {
        val method: Method
        val paramTypes: Array<Class<*>> = params.map { it.javaClass }.toTypedArray()
        try {
            method = obj.javaClass.getDeclaredMethod(methodName, *paramTypes)
            method.isAccessible = true
        } catch (e: NoSuchMethodException) {
            // 指定したメソッドが見つからない
            throw NoSuchMethodException(
                String.format(
                    "Method '%s' could not be found in '%s'. Methods found: [%s]",
                    methodName, obj.javaClass.name, listOf(*obj.javaClass.declaredMethods)
                )
            )
        }
        return method.invoke(obj, *params)
    }

    /**
     * 静的NMSメソッドを実行する
     * @param className クラス名(NMSパッケージ名は含まない)
     * @param methodName メソッド名
     * @param params 引数
     * @return メソッドの戻り値
     */
    @Throws(Exception::class)
    fun invokeStaticMethod(className: String, methodName: String, vararg params: Any): Any {
        val obj = Class.forName("$NMS.$className")
        val paramTypes: Array<Class<*>> = params.map { it.javaClass }.toTypedArray()
        val method: Method
        try {
            method = obj.getDeclaredMethod(methodName, *paramTypes)
            method.isAccessible = true
        } catch (e: NoSuchMethodException) {
            // 指定したメソッドが見つからない
            throw Exception(
                String.format(
                    "Method '%s' could not be found in '%s'. Methods found: [%s]",
                    methodName, obj.name, listOf(*obj.methods)
                ), e
            )
        }
        return method.invoke(null, *params)
    }
}
