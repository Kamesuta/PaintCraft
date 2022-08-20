package com.kamesuta.paintcraft

import org.bukkit.Bukkit
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object ReflectionAccessor {
    private val NMS: String

    init {
        val version = Bukkit.getServer().javaClass.getPackage().name
        NMS = version.replace("org.bukkit.craftbukkit", "net.minecraft.server")
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun getField(obj: Any, fieldName: String): Any {
        val field: Field
        try {
            field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
        } catch (e: NoSuchFieldException) {
            throw NoSuchFieldException(
                String.format(
                    "Field '%s' could not be found in '%s'. Fields found: {%s}",
                    fieldName, obj.javaClass.name, listOf(*obj.javaClass.declaredFields)
                )
            )
        }
        return field[obj]
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun getSuperField(obj: Any, fieldName: String): Any {
        val field: Field
        try {
            field = obj.javaClass.superclass.getDeclaredField(fieldName)
            field.isAccessible = true
        } catch (e: NoSuchFieldException) {
            throw NoSuchFieldException(
                String.format(
                    "Field '%s' could not be found in '%s'. Fields found: {%s}",
                    fieldName, obj.javaClass.name, listOf(*obj.javaClass.declaredFields)
                )
            )
        }
        return field[obj]
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun setField(obj: Any, fieldName: String, value: Any?) {
        val field: Field
        try {
            field = obj.javaClass.getDeclaredField(fieldName)
            field.isAccessible = true
        } catch (e: NoSuchFieldException) {
            throw NoSuchFieldException(
                String.format(
                    "Field '%s' could not be found in '%s'. Fields found: [%s]",
                    fieldName, obj.javaClass.name, listOf(*obj.javaClass.declaredFields)
                )
            )
        }
        field[obj] = value
    }

    @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
    fun invokeMethod(obj: Any, methodName: String): Any {
        val method: Method
        try {
            method = obj.javaClass.getDeclaredMethod(methodName)
            method.isAccessible = true
        } catch (e: NoSuchMethodException) {
            throw NoSuchMethodException(
                String.format(
                    "Method '%s' could not be found in '%s'. Methods found: [%s]",
                    methodName, obj.javaClass.name, listOf(*obj.javaClass.declaredMethods)
                )
            )
        }
        return method.invoke(obj)
    }

    @Throws(NoSuchMethodException::class, InvocationTargetException::class, IllegalAccessException::class)
    fun invokeMethod(obj: Any, methodName: String, vararg parameters: Any): Any {
        val method: Method
        val parameterTypes: Array<Class<*>> = parameters.map { it.javaClass }.toTypedArray()
        try {
            method = obj.javaClass.getDeclaredMethod(methodName, *parameterTypes)
            method.isAccessible = true
        } catch (e: NoSuchMethodException) {
            throw NoSuchMethodException(
                String.format(
                    "Method '%s' could not be found in '%s'. Methods found: [%s]",
                    methodName, obj.javaClass.name, listOf(*obj.javaClass.declaredMethods)
                )
            )
        }
        return method.invoke(obj, *parameters)
    }

    @Throws(Exception::class)
    fun invokeStaticMethod(className: String, methodName: String, vararg params: Any): Any {
        val obj = Class.forName("$NMS.$className")
        val paramTypes: Array<Class<*>> = params.map { it.javaClass }.toTypedArray()
        val method: Method
        try {
            method = obj.getDeclaredMethod(methodName, *paramTypes)
            method.isAccessible = true
        } catch (e: NoSuchMethodException) {
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
