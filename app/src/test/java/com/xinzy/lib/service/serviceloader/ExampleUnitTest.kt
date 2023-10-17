package com.xinzy.lib.service.serviceloader

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test1() {
        val clazz = ICalculate.Stub::class.java
        val fields = clazz.declaredFields

        fields.forEach {
            println(it)
        }

        println(clazz.interfaces[0])

        val parentFields = clazz.interfaces[0].declaredFields
        parentFields.forEach {
            println(it)
        }
    }
}