package com.xinzy.lib.service.serviceloader

import org.junit.Test

import org.junit.Assert.*
import java.util.function.Consumer

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val clazz = Action::class.java
        println(clazz.isAssignableFrom(Consumer::class.java))

        println(Consumer::class.java.isAssignableFrom(Action::class.java))
    }




    class Action : Consumer<Boolean> {
        override fun accept(t: Boolean) {
        }
    }
}