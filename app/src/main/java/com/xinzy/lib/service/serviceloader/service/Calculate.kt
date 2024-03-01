package com.xinzy.lib.service.serviceloader.service

import com.xinzy.lib.service.serviceloader.ICallback
import com.xinzy.lib.service.serviceloader.entity.Param

interface Calculate {
    fun add(first: Int, second: Int): Int

    fun sub(first: Int, second: Int): Int

    fun multi(param: Param?): Int

    fun setCallback(callback: ICallback)
}