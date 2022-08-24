package com.kamesuta.paintcraft.util

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * リフレクションを使用して、NMSにアクセスするクラス
 */
object ReflectionAccessor {
    /**
     * フィールドの値を取得する
     * @param obj 対象のオブジェクト
     * @param fieldName フィールド名
     * @return フィールドの値
     */
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun getField(obj: Any, fieldName: String): Any? {
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
    fun getSuperField(obj: Any, fieldName: String): Any? {
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
     * @param params 引数 (Class型が指定された場合、次の引数の型として使用される。詳しくは {@link #getParamTypes(Object...)} を参照)
     * @return メソッドの戻り値
     */
    @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
    fun invokeMethod(obj: Any, methodName: String, vararg params: Any): Any? {
        val method: Method
        val paramTypes = getParamTypes(*params)
        val paramValues = getParamValues(*params)
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
        return method.invoke(obj, *paramValues)
    }

    /**
     * 静的メソッドを実行する
     * @param clazz クラス (クラスはProtocolLibのMinecraftReflectionクラスから取得できる)
     * @param methodName メソッド名
     * @param params 引数 (Class型が指定された場合、次の引数の型として使用される。詳しくは {@link #getParamTypes(Object...)} を参照)
     * @return メソッドの戻り値
     */
    @Throws(ReflectiveOperationException::class)
    fun invokeStaticMethod(clazz: Class<*>, methodName: String, vararg params: Any): Any? {
        val paramTypes = getParamTypes(*params)
        val paramValues = getParamValues(*params)
        val method: Method
        try {
            method = clazz.getDeclaredMethod(methodName, *paramTypes)
            method.isAccessible = true
        } catch (e: NoSuchMethodException) {
            // 指定したメソッドが見つからない
            throw ReflectiveOperationException(
                String.format(
                    "Method '%s' could not be found in '%s'. Methods found: [%s]",
                    methodName, clazz.name, listOf(*clazz.methods)
                ), e
            )
        }
        return method.invoke(null, *paramValues)
    }

    /**
     * クラスのインスタンスを作成する
     * @param clazz クラス (クラスはProtocolLibのMinecraftReflectionクラスから取得できる)
     * @param params 引数 (Class型が指定された場合、次の引数の型として使用される。詳しくは {@link #getParamTypes(Object...)} を参照)
     * @return インスタンス
     */
    @Throws(ReflectiveOperationException::class)
    fun newInstance(clazz: Class<*>, vararg params: Any): Any? {
        val paramTypes = getParamTypes(*params)
        val paramValues = getParamValues(*params)
        val constructor: Constructor<*>
        try {
            constructor = clazz.getDeclaredConstructor(*paramTypes)
            constructor.isAccessible = true
        } catch (e: NoSuchMethodException) {
            // 指定したコンストラクターが見つからない
            throw ReflectiveOperationException(
                String.format(
                    "Constructor could not be found in '%s'. Constructors found: [%s]",
                    clazz.name, listOf(*clazz.constructors)
                ), e
            )
        }
        return constructor.newInstance(*paramValues)
    }

    /**
     * パラメータのクラスを取得する
     * Class型が指定された場合は、その次の引数の型の代わりにClass型を使用する
     * <code>
     * getParamTypes(true, 1, "str", java.util.Collection::class.java, mutableListOf())
     * </code>
     * この場合、mutableListOf()はCollection型として扱われる
     * @param params パラメータ
     */
    private fun getParamTypes(vararg params: Any): Array<Class<*>> {
        val result = mutableListOf<Class<*>>()
        var skip: Class<*>? = null
        for (param in params) {
            if (param.javaClass == Class::class.java) {
                skip = param as Class<*>
                continue
            }
            result.add(skip ?: param.javaClass.kotlin.javaPrimitiveType ?: param.javaClass)
            skip = null
        }
        return result.toTypedArray()
    }

    /**
     * パラメータを取得する
     * Class型が指定された場合は、スキップする
     * @param params パラメータ
     */
    private fun getParamValues(vararg params: Any): Array<Any> {
        return params.filter { it.javaClass != Class::class.java }.toTypedArray()
    }
}
