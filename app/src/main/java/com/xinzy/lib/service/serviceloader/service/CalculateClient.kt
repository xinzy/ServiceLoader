package com.xinzy.lib.service.serviceloader.service

import android.util.Log
import com.xinzy.lib.service.serviceloader.ICalculate
import com.xinzy.lib.service.serviceloader.ICallback
import com.xinzy.lib.service.serviceloader.entity.Param

class CalculateClient : ICalculate.Stub() {

    private var callback: ICallback? = null

    override fun add(first: Int, second: Int): Int {
        Log.d("CalculateClient", "add: ")
        callback?.callback("add", first + second)
        return first + second
    }

    override fun sub(first: Int, second: Int): Int {
        Log.d("CalculateClient", "sub: ")
        return first - second
    }

//    override fun multi(param: Param?): Int {
//        Log.d("CalculateClient", "param=$param")
//        return if (param == null) 0 else param.first * param.second
//    }

    override fun setCallback(callback: ICallback?) {
        this.callback = callback
        Log.d("CalculateClient", "callback=$callback")
    }
}